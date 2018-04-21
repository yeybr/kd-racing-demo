package com.solace.troubleflipper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solacesystems.jcsmp.*;
import com.solacesystems.jcsmp.impl.JCSMPXMLMessageConsumer;
import com.sun.imageio.plugins.jpeg.JPEG;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Subscriber {

    private XMLMessageConsumer consumer;

    private Map<String, Consumer<?>> handlers = new HashMap<>();
    private Map<String, Class<?>> mappers = new HashMap<>();

    public Subscriber(JCSMPSession session) throws JCSMPException {
        consumer = session.getMessageConsumer(new XMLMessageListener() {

            @Override
            public void onReceive(BytesXMLMessage msg) {
                handleMessage(msg);
            }

            @Override
            public void onException(JCSMPException e) {
                System.out.printf("Consumer received exception: %s%n",e);
            }
        });

        consumer.start();
    }


    @SuppressWarnings("unchecked")
    private void handleMessage(BytesXMLMessage message) {
        String topic = message.getDestination().getName();
        Consumer consumer = handlers.get(topic);
        if (consumer != null) {
            mappers.get(topic);
            ObjectMapper mapper = new ObjectMapper();
            try {
                Object m = mapper.readValue(message.getBytes(), mappers.get(topic));
                consumer.accept(m);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public <T> void registerHandler(Class<T> clazz, String topic, Consumer<T> handler) {
        handlers.put(topic, handler);
        mappers.put(topic, clazz);
    }
}
