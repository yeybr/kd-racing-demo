package com.solace.troubleflipper.model;

public class Peach extends Player {

    private boolean healUsed = false;

    public boolean isHealUsed() {
        return healUsed;
    }

    public void useHeal() {
        healUsed = true;
    }

    @Override
    public void heal() {
        // Do nothing, can't heal Peach
    }
}
