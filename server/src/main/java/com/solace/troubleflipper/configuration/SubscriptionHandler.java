package com.solace.troubleflipper.configuration;

public @interface SubscriptionHandler {

    String topic();

    Class<?> messageType();

    /**
     * Valid types:
     * equals
     * startsWith
     */
    String type() default "equals";

}
