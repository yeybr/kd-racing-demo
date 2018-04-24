package com.solace.troubleflipper.model;

public class Yoshi extends Character {

    private boolean immuneUsed = false;

    Yoshi() {
        super(CharacterType.yoshi);
    }

    public boolean isImmuneUsed() {
        return immuneUsed;
    }

    public void useImmune() {
        immuneUsed = true;
    }

    @Override
    public void heal() {
        immuneUsed = false;
    }

}
