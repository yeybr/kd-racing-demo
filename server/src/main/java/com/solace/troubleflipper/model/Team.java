package com.solace.troubleflipper.model;

import java.util.*;

public class Team {

    private String id;
    private String name;
    private Map<CharacterType, Player> characters = new EnumMap<>(CharacterType.class);
    private Map<String, Player> playersMap = new HashMap<>();
    private List<Player> players = new ArrayList<>();
    private Game game;
    private int completedGames = 0;
    private boolean immune = false;

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
        playersMap.put(player.getClientName(), player);
        players.add(player);
        player.setTeam(this);
    }

    public void chooseCharacter(CharacterType characterType, Player player) {
        Character character = createCharacter(characterType);
        player.setCharacter(character);
        characters.put(characterType, player);
    }

    public void addBonusCharacter(CharacterType characterType, Player player) {
        if (!characters.containsKey(characterType) && !player.getBonusCharacters().containsKey(characterType)) {
            Character character = createCharacter(characterType);
            player.getBonusCharacters().put(characterType, character);
            characters.put(characterType, player);
        }
    }

    public Player getPlayer(String clientName) {
        return playersMap.get(clientName);
    }

    public Player getPlayer(CharacterType characterType) {
        return characters.get(characterType);
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void addCompletedGame() {
        completedGames++;
    }

    public int getCompletedGames() {
        return completedGames;
    }

    private Character createCharacter(CharacterType characterType) {
        Character character;
        switch (characterType) {
            case peach:
                character = new Peach();
                break;
            case bowser:
                character = new Bowser();
                break;
            case yoshi:
                character = new Yoshi();
                break;
            case goomba:
                character = new Goomba();
                break;
            default:
                character = new Mario();
                break;
        }
        return character;
    }

    public boolean isImmune() {
        return immune;
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Team team = (Team) o;

        return id.equals(team.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
