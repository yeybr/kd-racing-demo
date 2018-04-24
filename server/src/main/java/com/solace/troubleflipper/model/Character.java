package com.solace.troubleflipper.model;

public abstract class Character {
    private CharacterType type;

    Character() {}

    Character(CharacterType type) {
        this.type = type;
    }

    public CharacterType getType() {
        return type;
    }

    public void setType(CharacterType type) {
        this.type = type;
    }

    public abstract void heal();
}
