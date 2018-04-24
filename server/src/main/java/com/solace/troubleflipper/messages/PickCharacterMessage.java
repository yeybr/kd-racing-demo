package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.CharacterType;

public class PickCharacterMessage {
    private CharacterType characterType;
    private String clientId;

    public CharacterType getCharacterType() {
        return characterType;
    }

    public void setCharacterType(CharacterType characterType) {
        this.characterType = characterType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
