package com.solace.troubleflipper.model;

public class Mario extends Character {

    private int starPowerUps = 3;

    Mario() {
        super(CharacterType.mario);
    }

    public void useStarPowerUp() {
        this.starPowerUps--;
    }

    public int getStarPowerUps() {
        return starPowerUps;
    }

    @Override
    public void heal() {
        this.starPowerUps = 3;
    }

}
