package com.solace.troubleflipper.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.*;
import com.solace.troubleflipper.model.*;
import com.solace.troubleflipper.model.Character;
import com.solace.troubleflipper.properties.SolaceCloudProperties;
import com.solace.troubleflipper.properties.TournamentProperties;
import com.solace.troubleflipper.Tournament;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;

@Configuration
public class JCSMPConfiguration {

    private Logger log = LoggerFactory.getLogger("game");
    private Timer immunityTimer = new Timer("ImmunityTimer");

    private XMLMessageProducer producer;
    private ObjectMapper mapper = new ObjectMapper();

    @Bean
    public JCSMPSession getJCSMPConnector(SolaceCloudProperties solaceCloudProperties) throws JCSMPException {
        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.VPN_NAME, solaceCloudProperties.getVpn());
        props.setProperty(JCSMPProperties.USERNAME, solaceCloudProperties.getUsername());
        props.setProperty(JCSMPProperties.PASSWORD, solaceCloudProperties.getPassword());
        props.setProperty(JCSMPProperties.HOST, solaceCloudProperties.getUrl());
        props.setProperty(JCSMPProperties.REAPPLY_SUBSCRIPTIONS, true);
        props.setProperty(JCSMPProperties.CLIENT_NAME, "trouble-flipper");
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
                                // TODO we need to distinguish people in the queue vs players in a game
                                player = new Mario();
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
                                    try {
                                        updatePuzzleForTeam(teamId, game, tournament);
                                    } catch (JCSMPException ex) {
                                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                    }
                                }
                            }
                        }
                    } else if ((msg.getDestination()).toString().startsWith("games")) {
                        String msgStr = getMessageStr(msg);
                        if (msgStr != null) {
                            String topicSplit[] = (msg.getDestination()).toString().split("/");
                            String teamId = topicSplit[1];
                            Game game = tournament.getGame(teamId);
                            if (game == null) {
                                return;
                            }
                            if (topicSplit.length == 2) {
                                SwapPiecesMessage swapPiecesMessage = mapper.readValue(msgStr, SwapPiecesMessage.class);
                                System.out.printf(teamId);
                                game.swapPieces(swapPiecesMessage.getPiece1(), swapPiecesMessage.getPiece2());
                                try {
                                    updatePuzzleForTeam(teamId, game, tournament);
                                } catch (JCSMPException ex) {
                                    System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                }
                            } else if ("starPower".equals(topicSplit[2])) {
                                StarPowerMessage starPowerMessage = mapper.readValue(msgStr, StarPowerMessage.class);
                                Mario mario = (Mario) game.getTeam().getPlayer(Character.mario);
                                if (mario.getStarPowerUps() > 0) {
                                    log.debug("Mario used star power");
                                    mario.useStarPowerUp();
                                    game.starPower(starPowerMessage.getPuzzlePiece());
                                    try {
                                        updatePuzzleForTeam(teamId, game, tournament);
                                    } catch (JCSMPException ex) {
                                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                    }
                                }
                            } else if ("peachHeal".equals(topicSplit[2])) {
                                PeachHealMessage peachHealMessage = mapper.readValue(msgStr, PeachHealMessage.class);
                                // TODO wait until player selection logic before checking
//                                Peach peach = (Peach) game.getTeam().getPlayer(Character.peach);
//                                if (peach.isHealUsed()) {
//                                    peach.useHeal();
                                    Character character = peachHealMessage.getCharacter();
                                    Player player = game.getTeam().getPlayer(character);
                                    player.heal();
//                                }
                            } else if ("greenShell".equals(topicSplit[2])) {
                            } else if ("troubleFlipper".equals(topicSplit[2])) {
                            } else if ("immune".equals(topicSplit[2])) {
                                Yoshi yoshi = (Yoshi) game.getTeam().getPlayer(Character.yoshi);
                                if (yoshi.isImmuneUsed()) {
                                    yoshi.useImmune();
                                    game.setImmune(true);
                                    immunityTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            game.setImmune(false);
                                        }
                                    }, 10000);
                                }
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

    private void updatePuzzleForTeam(String teamId, Game game, Tournament tournament) throws JCSMPException, IOException {
        Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + teamId);
        BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
        UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
        updatePuzzleMessage.setTeamId(teamId);
        updatePuzzleMessage.setPuzzle(game.getPuzzleBoard());
        updatePuzzleMessage.setGameWon(game.isGameWon());
        bytesMessage.setData(mapper.writeValueAsString(updatePuzzleMessage).getBytes());
        producer.send(bytesMessage, topic);
        if (game.isGameWon()) {
            immunityTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Game newGame = tournament.nextGame(teamId);
                    newGame.start();
                    try {
                        updatePuzzleForTeam(teamId, newGame, tournament);
                    } catch (JCSMPException | IOException ex) {
                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                    }
                }
            }, 3000);

        }
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

}
