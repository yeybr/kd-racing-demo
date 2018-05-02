package com.solace.troubleflipper.model;

public abstract class Character {
    private CharacterType type;
    private int superPower;
    private int maxSuperPower;

    Character(int maxSuperPower) {
        this(maxSuperPower, null);
    }

    Character(int maxSuperPower, CharacterType type) {
        this.superPower = maxSuperPower;
        this.maxSuperPower = maxSuperPower;
        this.type = type;
    }

    public CharacterType getType() {
        return type;
    }

    public void setType(CharacterType type) {
        this.type = type;
    }

    public int getSuperPower() {
        return superPower;
    }

    public void useSuperPower() {
        superPower--;
    }

    public void heal() {
        this.superPower = this.maxSuperPower;
    }
}
