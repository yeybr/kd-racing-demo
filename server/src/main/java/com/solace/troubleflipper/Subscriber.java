package com.solace.troubleflipper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.model.SubscriberException;
import com.solacesystems.jcsmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Subscriber {

    private Logger log = LoggerFactory.getLogger("message");
    private ObjectMapper objectMapper;
    private JCSMPSession session;

    private Map<String, Object> handlers = new HashMap<>();
    private Map<String, Class<?>> mappers = new HashMap<>();

    public Subscriber(JCSMPSession session, ObjectMapper objectMapper) throws JCSMPException {
        this.objectMapper = objectMapper;
        this.session = session;
        XMLMessageConsumer consumer = session.getMessageConsumer(new XMLMessageListener() {

            @Override
            public void onReceive(BytesXMLMessage msg) {
                handleMessage(msg);
            }

            @Override
            public void onException(JCSMPException ex) {
                log.error("Consumer received exception", ex);
            }
        });
        consumer.start();
    }


    @SuppressWarnings("unchecked")
    private void handleMessage(BytesXMLMessage message) {
        String topic = message.getDestination().getName();
        Class<?> messageType = mappers.get(topic);
        if (messageType != null) {
            Consumer consumer = (Consumer) handlers.get(topic);
            if (consumer != null) {
                byte[] buf = new byte[message.getAttachmentContentLength()];
                message.readAttachmentBytes(buf);
                String messageText = new String(buf);
                try {
                    Object m = objectMapper.readValue(buf, mappers.get(topic));
                    log.trace("Message received: " + messageText);
                    consumer.accept(m);
                } catch (IOException ex) {
                    log.error("Unable to handle message from topic " + topic + ". Message: " + messageText, ex);
                }
            }
        } else {
            log.info("Start new thread for " + topic);
            Runnable runnable = (Runnable) handlers.get(topic);
            if (runnable == null) {
                log.error("Null handler for topic " + topic);
            } else {
                runnable.run();
            }
        }
    }

    public <T> void registerHandler(Class<T> clazz, String topic, Consumer<T> handler) {
        handlers.put(topic, handler);
        mappers.put(topic, clazz);
    }

    public void registerHandler(String topic, Runnable runnable) {
        if (runnable == null) {
            throw new NullPointerException("The runnable cannot be null for topic " + topic);
        }
        handlers.put(topic, runnable);
        mappers.put(topic, null);
    }

    public void deregisterHandler(String topic) {
        handlers.remove(topic);
        mappers.remove(topic);
    }

    public void subscribeForClient(String topicName, String clientName) throws SubscriberException {
        Topic topic = JCSMPFactory.onlyInstance().createTopic(topicName);
        try {
            session.addSubscription(JCSMPFactory.onlyInstance().createClientName(clientName), topic, JCSMPSession.WAIT_FOR_CONFIRM);
        } catch (JCSMPException ex) {
            if (ex instanceof JCSMPErrorResponseException) {
                JCSMPErrorResponseException re = (JCSMPErrorResponseException) ex;
                if (re.getResponseCode() == 400 && re.getSubcodeEx() == JCSMPErrorResponseSubcodeEx.SUBSCRIPTION_ALREADY_PRESENT) {
                    return;
                }
            }
            String message = "Unable to add subscription for client " + clientName + " on topic " + topicName;
            log.error(message, ex);
            throw new SubscriberException(message, ex);
        }
    }
}
