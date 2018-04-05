package com.solace.troubleflipper.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.AddUserMessage;
import com.solace.troubleflipper.messages.TournamentMessage;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
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
                if (msg instanceof TextMessage) {
                    System.out.printf("TextMessage received: '%s'%n",
                            ((TextMessage)msg).getText());

                    try {
                        if ((msg.getDestination()).toString().equals("users")) {
                            AddUserMessage addUserMessage = mapper.readValue(((TextMessage) msg).getText(), AddUserMessage.class);

                            if (!tournament.getPlayers().stream().anyMatch(item -> addUserMessage.getClient().equals(item.getClientName()))) {
                                Player player = new Player();
                                player.setGamerTag(addUserMessage.getUsername());
                                player.setClientName(addUserMessage.getClient());
                                tournament.addPlayer(player);
                            }
                        } else if ((msg.getDestination()).toString().equals("tournaments")) {
                            TournamentMessage tournamentMessage = mapper.readValue(((TextMessage) msg).getText(), TournamentMessage.class);

                            if ((tournamentMessage.getAction().equals("buildTeams")) && (tournament.getPlayers().size() > 0)) {
                                tournament.prepareTeams();
                                for (Player player : tournament.getPlayers()) {
                                    Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + player.getTeam().getId());
                                    ClientName clientName = JCSMPFactory.onlyInstance().createClientName(player.getGamerTag());
                                    try {
                                        session.addSubscription(clientName, topic, JCSMPSession.WAIT_FOR_CONFIRM);
                                    } catch (JCSMPException ex) {
                                        System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                    }
                                }
                                for (Game game : tournament.getGames()) {
                                    game.start();
                                    for (Player player : game.getTeam().getPlayers()) {
                                        String teamId =  player.getTeam().getId();
                                        Topic topic = JCSMPFactory.onlyInstance().createTopic("team/" + teamId);
                                        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
                                        UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
                                        updatePuzzleMessage.setTeamId(teamId);
                                        updatePuzzleMessage.setPuzzle(game.getPuzzleBoard());
                                        textMessage.setText(mapper.writeValueAsString(updatePuzzleMessage));
                                        try {
                                            producer.send(textMessage, topic);
                                        } catch (JCSMPException ex) {
                                            System.err.println("Encountered a JCSMPException, closing consumer channel... " + ex.getMessage());
                                        }
                                    }
                                }
                            }

                        }
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                } else {
                    System.out.println("Message received.");
                }
                System.out.printf("Message Dump:%n%s%n",msg.dump());
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

        cons.start();

        return session;
    }

}
