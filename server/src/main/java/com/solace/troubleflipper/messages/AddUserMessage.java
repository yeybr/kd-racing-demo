package com.solace.troubleflipper.messages;

public class AddUserMessage {

    private String username;
    private String client;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }
}
