package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.Team;

import java.util.List;
import java.util.Map;

public class PlayerListMessage {

    private List<Player> players;
    private List<Map<String, String>> teams;

    public List<Map<String, String>> getTeams() {
        return teams;
    }

    public void setTeams(List<Map<String, String>> teams) {
        this.teams = teams;

    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }
}
