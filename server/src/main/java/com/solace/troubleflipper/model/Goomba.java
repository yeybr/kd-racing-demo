package com.solace.troubleflipper.model;

public class Goomba extends Character {

    private int greenShells = 5;

    Goomba() {
        super(CharacterType.goomba);
    }

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

}
