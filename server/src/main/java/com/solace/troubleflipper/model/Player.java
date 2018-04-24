package com.solace.troubleflipper.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Player extends User {
    @JsonIgnore
    private Team team;
    private int rightMoves;
    private int wrongMoves;
    private Character character;
    private Map<CharacterType, Character> bonusCharacters = new EnumMap<>(CharacterType.class);

    public Player() {
        super();
    }

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

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public Map<CharacterType, Character> getBonusCharacters() {
        return bonusCharacters;
    }

    public void reset() {
        super.reset();
        team = null;
        rightMoves = 0;
        wrongMoves = 0;
        character = null;
        bonusCharacters.clear();
    }

}
