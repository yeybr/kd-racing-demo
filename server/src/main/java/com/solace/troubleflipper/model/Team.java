package com.solace.troubleflipper.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Team {

    private String id;
    private String name;
    private Map<Character, Player> characters = new HashMap<>();
    private List<Player> players = new ArrayList<>();

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void chooseCharacter(Character character, Player player) {
        characters.put(character, player);
    }
}
