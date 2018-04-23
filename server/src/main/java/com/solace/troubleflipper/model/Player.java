package com.solace.troubleflipper.model;

import java.util.UUID;

public abstract class Player {
    private String id;
    private String gamerTag;
    private String clientName;
    private Team team;
    private int rightMoves;
    private int wrongMoves;

    public Player() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getGamerTag() {
        return gamerTag;
    }

    public void setGamerTag(String gamerTag) {
        this.gamerTag = gamerTag;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) { this.clientName = clientName; }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void rightMove() {
        rightMoves++;
    }

    public void wrongMove() {
        wrongMoves++;
    }

    public int getRightMoves() {
        return rightMoves;
    }

    public void setRightMoves(int rightMoves) {
        this.rightMoves = rightMoves;
    }

    public int getWrongMoves() {
        return wrongMoves;
    }

    public void setWrongMoves(int wrongMoves) {
        this.wrongMoves = wrongMoves;
    }

    public abstract Character getCharacter();

    public abstract void heal();
}
