package com.solace.troubleflipper.model;

import java.util.*;

public class Team {

    private String id;
    private String name;
    private Map<Character, Player> characters = new HashMap<>();
    private List<Player> players = new ArrayList<>();

    public Team() {
        id = UUID.randomUUID().toString();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.setTeam(this);
    }

    public void chooseCharacter(Character character, Player player) {
        characters.put(character, player);
        player.setCharacter(character);
    }

    public List<Player> getPlayers() {
        return players;
    }
}
