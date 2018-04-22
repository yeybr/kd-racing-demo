package com.solace.troubleflipper.model;

public class SubscriberException extends Exception {

    public SubscriberException(String message, Exception cause) {
        super(message, cause);
    }
}
