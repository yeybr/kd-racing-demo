package com.solace.troubleflipper.model;

import java.util.UUID;

public abstract class Player {
    private String id;
    private String gamerTag;
    private String clientName;
    private Team team;
    private int rightMoves;
    private int wrongMoves;
    private Character character;

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

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public abstract void heal();
}
