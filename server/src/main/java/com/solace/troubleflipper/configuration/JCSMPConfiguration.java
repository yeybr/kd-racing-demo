package com.solace.troubleflipper.configuration;

import com.solace.troubleflipper.properties.SolaceCloudProperties;
import com.solacesystems.jcsmp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JCSMPConfiguration {

    @Bean
    public JCSMPSession getJCSMPConnector(SolaceCloudProperties solaceCloudProperties) throws JCSMPException {
        JCSMPProperties props = new JCSMPProperties();
        props.setProperty(JCSMPProperties.VPN_NAME, solaceCloudProperties.getVpn());
        props.setProperty(JCSMPProperties.USERNAME, solaceCloudProperties.getUsername());
        props.setProperty(JCSMPProperties.PASSWORD, solaceCloudProperties.getPassword());
        props.setProperty(JCSMPProperties.HOST, solaceCloudProperties.getUrl());
        props.setProperty(JCSMPProperties.TOPIC_DISPATCH, true);
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
        session.getMessageConsumer((XMLMessageListener) null);
        return session;
    }

    @Bean
    public XMLMessageProducer getProducer(JCSMPSession session) throws JCSMPException {
        XMLMessageProducer producer = session.getMessageProducer(new JCSMPStreamingPublishCorrelatingEventHandler() {

            @Override
            public void handleError(String s, JCSMPException e, long l) {

            }

            @Override
            public void responseReceived(String s) {

            }

            @Override
            public void responseReceivedEx(Object o) {

            }

            @Override
            public void handleErrorEx(Object o, JCSMPException e, long l) {

            }
        });
        return producer;
    }
}
