package com.solace.troubleflipper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.AddUserAckMessage;
import com.solace.troubleflipper.model.PublisherException;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class Publisher {

    private Logger log = LoggerFactory.getLogger("message");
    private final XMLMessageProducer producer;
    private final ObjectMapper objectMapper;

    @Autowired
    public Publisher(JCSMPSession session, ObjectMapper objectMapper) throws JCSMPException {
        producer = session.getMessageProducer(new JCSMPStreamingPublishEventHandler() {

            @Override
            public void handleError(String s, JCSMPException e, long l) {
                log.error("An error occurred publishing a message: " + s, e);
            }

            @Override
            public void responseReceived(String s) {
                log.info("Received response: " + s);
            }

        });
        this.objectMapper = objectMapper;
    }

    public void publish(String topicName, Object message) throws PublisherException {
        try {
            Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
            BytesMessage bytesMessage = JCSMPFactory.onlyInstance().createMessage(BytesMessage.class);
            bytesMessage.setData(objectMapper.writeValueAsString(message).getBytes());
            producer.send(bytesMessage, topic);
        } catch (JCSMPException | IOException ex) {
            log.error("An error occurred publishing a message", ex);
            throw new PublisherException("Unable to publish " + message.getClass().getName() + " message to topic " + topicName, ex);
        }
    }
}
