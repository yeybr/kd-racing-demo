package com.solace.troubleflipper.model;

public class Mario extends Player {

    private int starPowerUps = 3;

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
