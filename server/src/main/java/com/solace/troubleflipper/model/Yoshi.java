package com.solace.troubleflipper.model;

public class Yoshi extends Player {

    private boolean immuneUsed = false;

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
