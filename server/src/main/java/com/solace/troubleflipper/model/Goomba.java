package com.solace.troubleflipper.model;

public class Goomba extends Player {

    private int greenShells = 5;

    public void useGreenShell() {
        this.greenShells--;
    }

    public int getGreenShells() {
        return greenShells;
    }

    @Override
    public void heal() {
        this.greenShells = 5;
    }

    @Override
    public Character getCharacter() {
        return Character.goomba;
    }
}
