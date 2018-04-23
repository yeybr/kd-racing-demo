package com.solace.troubleflipper.model;

public class Bowser extends Player {

    private boolean troubleFlipperUsed = false;

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

    @Override
    public Character getCharacter() {
        return Character.bowser;
    }
}
