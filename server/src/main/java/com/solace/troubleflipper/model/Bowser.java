package com.solace.troubleflipper.model;

public class Bowser extends Character {

    private boolean troubleFlipperUsed = false;

    Bowser() {
        super(CharacterType.bowser);
    }

    public boolean isTroubleFlipperUsed() {
        return troubleFlipperUsed;
    }

    public void useTroubleFlipper() {
        troubleFlipperUsed = true;
    }

    @Override
    public void heal() {
        troubleFlipperUsed = false;
    }

}
