package com.solace.troubleflipper.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.*;
import com.solace.troubleflipper.model.Game;
import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.PuzzlePiece;
import com.solace.troubleflipper.properties.SolaceCloudProperties;
import com.solace.troubleflipper.properties.TournamentProperties;
import com.solace.troubleflipper.Tournament;
import com.solacesystems.jcsmp.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.util.Optional;

@Configuration
public class JCSMPConfiguration {

    private XMLMessageProducer producer;

    @Bean
    public JCSMPSession getJCSMPConnector(SolaceCloudProperties solaceCloudProperties) throws JCSMPException {
        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.VPN_NAME, solaceCloudProperties.getVpn());
        props.setProperty(JCSMPProperties.USERNAME, solaceCloudProperties.getUsername());
        props.setProperty(JCSMPProperties.PASSWORD, solaceCloudProperties.getPassword());
        props.setProperty(JCSMPProperties.HOST, solaceCloudProperties.getUrl());
        props.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
        props.setProperty(JCSMPProperties.CLIENT_NAME, "trouble-flipper-monica");
        props.setProperty(JCSMPProperties.APPLICATION_DESCRIPTION, "The Java application running the trouble flipper server");

        // reconnect behaviour
        JCSMPChannelProperties cp = (JCSMPChannelProperties) props
                .getProperty(JCSMPProperties.CLIENT_CHANNEL_PROPERTIES);
        cp.setReconnectRetries(12000); // 1 hr for 0.5 sec timeout
        cp.setConnectRetries(1000);
        cp.setReconnectRetryWaitInMillis(500);

        JCSMPSession session = JCSMPFactory.onlyInstance().createSession(props);

        // connect (connect is blocking in JCSMP)
        session.connect();

        // needed for dispatch

        ObjectMapper mapper = new ObjectMapper();
        TournamentProperties tournamentProperties = new TournamentProperties();
        final Tournament tournament = new Tournament(tournamentProperties, session);

        producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {

            @Override
            public void handleError(String s, JCSMPException e, long l) {
                e.printStackTrace();
            }

            @Override
            public void responseReceived(String s) {
                System.out.println("Received response: " + s);
            }

        });

        final XMLMessageConsumer cons = session.getMessageConsumer(new XMLMessageListener() {

            @Override
            public void onReceive(BytesXMLMessage msg) {
                System.out.printf("Message Received. Dump:%n%s%n",msg.dump());
                try {
                    if ((msg.getDestination()).toString().equals("users")) {
                        String msgStr = getMessageStr(msg);
                        if (msgStr != null) {
                            AddUserMessage addUserMessage = mapper.readValue(msgStr, AddUserMessage.class);
                            Optional<Player> firstMatch = tournament.getPlayers().stream().filter(item ->
                                    addUserMessage.getClientId().equals(item.getClientName())).findFirst();
                            Player player = null;
                            if (firstMatch.isPresent()) {
                                player = firstMatch.get();
                                System.out.println("Found existing player " + player.getClientName());
                            }
                            if (player == null) {
                                player = new Player();
                                player.setGamerTag(addUserMessage.getUsername());
                                player.setClientName(addUserMessage.getClientId());
                                tournament.addPlayer(player);
                            }
                            Topic topic = JCSMPFactory.onlyInstance().createTopic("user/" + player.getClientName());
                            BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
                            AddUserAckMessage addUserAckMessage = new AddUserAckMessage(addUserMessage, AddUserAckMessage.RESULT_SUCCESS);
                            bytesMessage.setData(mapper.writeValueAsString(addUserAckMessage).getBytes());
                            try {
                                producer.send(bytesMessage, topic);
                            } catch (JCSMPException ex) {
                                System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                            }
                        }
                    } else if ((msg.getDestination()).toString().equals("tournaments")) {
                        String msgStr = getMessageStr(msg);
                        if (msgStr != null) {
                            TournamentMessage tournamentMessage = mapper.readValue(msgStr, TournamentMessage.class);

                            if ((tournamentMessage.getAction().equals("buildTeams")) && (tournament.getPlayers().size() > 0)) {
                                tournament.prepareTeams();
                                for (Player player : tournament.getPlayers()) {
                                    Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + player.getTeam().getId());
                                    ClientName clientName = JCSMPFactory.onlyInstance().createClientName(player.getClientName());
                                    try {
                                        session.addSubscription(clientName, topic, JCSMPSession.WAIT_FOR_CONFIRM);
                                    } catch (JCSMPException ex) {
                                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                    }
                                }
                                for (Game game : tournament.getGames()) {
                                    game.start();
                                    String teamId = game.getTeam().getId();
                                    Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + teamId);
                                    BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
                                    UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
                                    updatePuzzleMessage.setTeamId(teamId);
                                    updatePuzzleMessage.setPuzzle(game.getPuzzleBoard());
                                    bytesMessage.setData(mapper.writeValueAsString(updatePuzzleMessage).getBytes());
                                    try {
                                        producer.send(bytesMessage, topic);
                                    } catch (JCSMPException ex) {
                                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                    }
                                }
                            }
                        }
                    } else if ((msg.getDestination()).toString().startsWith("games")) {
                        String msgStr = getMessageStr(msg);
                        if (msgStr != null) {
                            SwapPiecesMessage swapPiecesMessage = mapper.readValue(msgStr, SwapPiecesMessage.class);
                            String topicSplit[] = (msg.getDestination()).toString().split("/");
                            String teamId = topicSplit[1];
                            System.out.printf(teamId);
                            Game game = tournament.getGame(teamId);
                            game.swapPieces(swapPiecesMessage.getPiece1(), swapPiecesMessage.getPiece2());
                            Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + teamId);
                            BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
                            UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
                            updatePuzzleMessage.setTeamId(teamId);
                            updatePuzzleMessage.setPuzzle(game.getPuzzleBoard());
                            bytesMessage.setData(mapper.writeValueAsString(updatePuzzleMessage).getBytes());
                            try {
                                producer.send(bytesMessage, topic);
                            } catch (JCSMPException ex) {
                                System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
            }
        });

        final Topic topic = JCSMPFactory.onlyInstance().createTopic("users");
        session.addSubscription(topic);

        final Topic tournamentsTopic = JCSMPFactory.onlyInstance().createTopic("tournaments");
        session.addSubscription(tournamentsTopic);

        final Topic gamesTopic = JCSMPFactory.onlyInstance().createTopic("games/>");
        session.addSubscription(gamesTopic);

        cons.start();

        return session;
    }

    private String getMessageStr(BytesXMLMessage message) throws IOException {
        if (message == null) {
            return null;
        }
        if (message instanceof TextMessage) {
            return ((TextMessage) message).getText();
        }
        if (message != null && message.hasAttachment()) {
            byte[] buf = new byte[message.getAttachmentContentLength()];
            message.readAttachmentBytes(buf);
            String msgStr = new String(buf);
            return msgStr;
        }
        return null;
    }

}
