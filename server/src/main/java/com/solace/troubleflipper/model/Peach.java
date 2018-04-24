package com.solace.troubleflipper.model;

public class Peach extends Character {

    private boolean healUsed = false;

    Peach() {
        super(CharacterType.peach);
    }

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
