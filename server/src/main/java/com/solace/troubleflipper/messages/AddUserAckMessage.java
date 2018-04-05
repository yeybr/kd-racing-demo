package com.solace.troubleflipper.messages;

public class AddUserAckMessage {

    private String username;
    private String clientId;
    private String result;

    public static String RESULT_SUCCESS = "success";
    public static String RESULT_FAILURE = "failure";

    public AddUserAckMessage(AddUserMessage addUserMessage, String result) {
        setUsername(addUserMessage.getUsername());
        setClientId(addUserMessage.getClientId());
        setResult(result);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getResult() {
        return this.result;
    }

    public void setResult(String result) {
        this.result = result;;
    }
}
