package com.solace.troubleflipper.configuration;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PuzzleProducer {

    private final JCSMPSession jcsmpSession;

    @Autowired
    public PuzzleProducer(JCSMPSession jcsmpSession) {
        this.jcsmpSession = jcsmpSession;
        doSomething();

    }
    public void doSomething()  {
        try {
            jcsmpSession.createNonDurableTopicEndpoint();
            jcsmpSession.createTemporaryQueue("test");
        } catch (Exception e) {
            e.printStackTrace();

        }

    }
}
