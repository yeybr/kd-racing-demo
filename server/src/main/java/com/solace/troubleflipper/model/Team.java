package com.solace.troubleflipper.model;

import java.util.*;

public class Team {

    private String id;
    private String name;
    private Map<Character, Player> characters = new EnumMap<>(Character.class);
    private Map<String, Player> players = new HashMap<>();
    private Game game;
    private int completedGames = 0;

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

    public void setGame (Game game) {
        this.game=game;
    }

    public Game getGame() {
        return game;
    }

    public void addPlayer(Player player) {
        players.put(player.getClientName(), player);
        player.setTeam(this);
    }

    public void chooseCharacter(Character character, Player player) {
        characters.put(character, player);
        player.setCharacter(character);
    }

    public Player getPlayer(String clientName) {
        return players.get(clientName);
    }

    public Player getPlayer(Character character) {
        return characters.get(character);
    }

    public void addCompletedGame() {
        completedGames++;
    }

    public int getCompletedGames() {
        return completedGames;
    }
}
