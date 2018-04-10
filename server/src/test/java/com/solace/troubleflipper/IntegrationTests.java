package com.solace.troubleflipper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.AddUserMessage;
import com.solace.troubleflipper.messages.SwapPiecesMessage;
import com.solace.troubleflipper.messages.TournamentMessage;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solace.troubleflipper.model.Game;
import com.solace.troubleflipper.model.PuzzlePiece;
import com.solacesystems.jcsmp.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTests {

    private static String vpn;
    private static String username;
    private static String password;
    private static String url;

    private static ObjectMapper mapper = new ObjectMapper();

    @Before
    public void loadSolaceCloudProperties() throws Exception {
//        Properties prop = new Properties();
//        InputStream in = getClass().getResourceAsStream("application.properties");
//        prop.load(in);
//        in.close();
//        vpn = prop.getProperty("solace.cloud.vpn");
//        username = prop.getProperty("solace.cloud.username");
//        password = prop.getProperty("solace.cloud.password");
//        url = prop.getProperty("solace.cloud.url");

        vpn = "msgvpn-91b69335zj";
        username = "solace-cloud-client";
        password = "1a57th4am83cb5ali4rtlu4p55";
        url = "tcp://mr-91b69335zj.messaging.solace.cloud:55555";
    }

    public static JCSMPSession createSession(String clientName) throws JCSMPException {
        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.VPN_NAME, vpn);
        props.setProperty(JCSMPProperties.USERNAME, username);
        props.setProperty(JCSMPProperties.PASSWORD, password);
        props.setProperty(JCSMPProperties.HOST, url);
        props.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
        props.setProperty(JCSMPProperties.CLIENT_NAME, clientName);
        props.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, "Integration tests");

        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(props);
        session.connect();
        return session;
    }

    @Test
    public void testTwoPlayersJoinGameAndGetInitialPuzzlePieces() throws Exception {
        PlayerSimulator player1 = new PlayerSimulator("player1");
        PlayerSimulator player2 = new PlayerSimulator("player2");

        player1.startSimulation();
        player2.startSimulation();

        // Wait 100 milliseconds until the players have added themselves
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            // Nothing can interrupt this thread
            ex.printStackTrace();
        }

        // Simulate the build teams message from player 1 (later should be updated to game master client)
        TournamentMessage startTournamentMessage = new TournamentMessage();
        startTournamentMessage.setAction("buildTeams");
        TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        msg.setText(mapper.writeValueAsString(startTournamentMessage));
        Topic topic = JCSMPFactory.onlyInstance().createTopic("tournaments");
        player1.getProducer().send(msg, topic);

        // Wait 1 second until the players have received the puzzle start messages
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            // Nothing can interrupt this thread
            ex.printStackTrace();
        }

        // Assert both players received a valid puzzle
        assertCurrentMessageIsUpdatePuzzleMessage(player1);
        assertCurrentMessageIsUpdatePuzzleMessage(player2);

        Thread.sleep(1000);

        // Simulate the pieces being switched by player 1
        UpdatePuzzleMessage playerUpdatePuzzleMessage = (UpdatePuzzleMessage) player1.getCurrentMessage();
        List<PuzzlePiece> puzzle = new ArrayList(playerUpdatePuzzleMessage.getPuzzle());
        player1.swapPieces(3, 5);
    }

    private static void assertCurrentMessageIsUpdatePuzzleMessage(PlayerSimulator player) {
        assertNotNull("Puzzle not received", player.getCurrentMessage());
        assertEquals("Unexpected message type", player.getCurrentMessage().getClass(), UpdatePuzzleMessage.class);
        UpdatePuzzleMessage playerUpdatePuzzleMessage = (UpdatePuzzleMessage) player.getCurrentMessage();
        assertNotNull("Puzzle message had no pieces", playerUpdatePuzzleMessage.getPuzzle());
        int pieces = playerUpdatePuzzleMessage.getPuzzle().size();
        assertTrue("Wrong number of pieces: " + pieces, pieces == 9 || pieces == 16 || pieces == 25);
        for (PuzzlePiece puzzlePiece : playerUpdatePuzzleMessage.getPuzzle()) {
            int index = puzzlePiece.getIndex();
            assertTrue("Invalid index for puzzle piece: " + index, index >= 0 && index < 25);
        }
    }

    private static  void assertPiecesAreSwapped(PlayerSimulator player, List<PuzzlePiece> puzzzleSnap) {
        assertNotNull("Puzzle not received", player.getCurrentMessage());
        // Verify message is received for topic game/teamId
        // Compare new message puzzle with puzzleSnap, two pieced should be different and the value should be 3 and 5
    }

    public static class PlayerSimulator {
        private String username;
        private JCSMPSession session;
        private XMLMessageProducer producer;
        private Thread thread;
        private Object currentMessage;
        private String failureReason;
        private String teamId;

        public PlayerSimulator(String username) throws JCSMPException {
            this.username = username;
            JCSMPSession session = createSession(username);
            XMLMessageConsumer consumer = session.getMessageConsumer(new XMLMessageListener() {

                @Override
                public void onReceive(BytesXMLMessage message) {
                    try {
                        System.out.printf("Message received: '%s'%n", message.dump());
                        String msgStr = getMessageStr(message);
                        UpdatePuzzleMessage updatePuzzleMessage = mapper.readValue(msgStr, UpdatePuzzleMessage.class);
                        System.out.println(updatePuzzleMessage);
                        teamId = updatePuzzleMessage.getTeamId();
                        currentMessage = updatePuzzleMessage;
                        synchronized (PlayerSimulator.this) {
                            PlayerSimulator.this.notify();
                        }
                    } catch (IOException ex) {
                        currentMessage = null;
                        ex.printStackTrace();
                    }
                }

                @Override
                public void onException(JCSMPException e) {
                    System.out.printf("Consumer received exception: %s%n",e);
                }
            });

            consumer.start();

            this.producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {

                @Override
                public void handleError(String s, JCSMPException e, long l) {
                }

                @Override
                public void responseReceived(String s) {
                }

            });
        }

        public XMLMessageProducer getProducer() {
            return producer;
        }

        public void startSimulation() {
            String usernameThread = username + "-Thread";
            this.thread = new Thread(() -> {
                // Send the add player
                try {
                    addPlayer();
                } catch (IOException | JCSMPException ex) {
                    ex.printStackTrace();
                    return;
                }

                // Wait for the notification which means the first message is received
                try {
                    synchronized (PlayerSimulator.this) {
                        PlayerSimulator.this.wait();
                    }
                } catch (InterruptedException ex) {
                    System.out.println(usernameThread + " was interrupted while waiting for notification");
                    ex.printStackTrace();
                }

                // Assert that the first message received was a puzzle pieces event
                if (!(currentMessage instanceof UpdatePuzzleMessage)) {
                    failureReason = "Received message that was not an update puzzle message: " + currentMessage.toString();
                }

            }, usernameThread);
            thread.start();
        }

        private void addPlayer() throws IOException, JCSMPException {
            AddUserMessage addUserMessage = new AddUserMessage();
            addUserMessage.setUsername(username);
            addUserMessage.setClientId(username);
            TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            msg.setText(mapper.writeValueAsString(addUserMessage));
            Topic topic = JCSMPFactory.onlyInstance().createTopic("users");
            this.producer.send(msg, topic);
        }

        private String getMessageStr(BytesXMLMessage message) throws IOException {
            if (message == null) {
                return null;
            }
            if (message instanceof TextMessage) {
                return ((TextMessage) message).getText();
            }
            if (message.hasAttachment()) {
                byte[] buf = new byte[message.getAttachmentContentLength()];
                message.readAttachmentBytes(buf);
                String msgStr = new String(buf);
                return msgStr;
            }
            return null;
        }

        public void swapPieces(int piece1Index, int piece2Index) throws IOException, JCSMPException {
            PuzzlePiece piece1 = new PuzzlePiece();
            piece1.setIndex(piece1Index);

            PuzzlePiece piece2 = new PuzzlePiece();
            piece2.setIndex(piece2Index);

            SwapPiecesMessage message = new SwapPiecesMessage();
            message.setPiece1(piece1);
            message.setPiece2(piece2);

            TextMessage msg = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
            msg.setText(mapper.writeValueAsString(message));

            Topic topic = JCSMPFactory.onlyInstance().createTopic("games/" + teamId);
            this.producer.send(msg, topic);
        }

        public Object getCurrentMessage() {
            return currentMessage;
        }
    }
}
