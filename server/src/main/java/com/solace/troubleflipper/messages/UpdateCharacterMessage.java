package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.CharacterType;
import com.solace.troubleflipper.model.Player;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateCharacterMessage {

    private String teamId;

    private String teamName;

    private List<CharacterType> availableCharacters;

    private List<Player> players;

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public List<CharacterType> getAvailableCharacters() {
        return availableCharacters;
    }

    public void setAvailableCharacters(List<CharacterType> availableCharacters) {
        this.availableCharacters = availableCharacters;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Team Name: ").append(teamName).append(", ");
        sb.append("Available Characters: [");
        if (availableCharacters != null) {
            sb.append(availableCharacters.stream().map(Object::toString).collect(Collectors.joining(",")));
        }
        sb.append("], ");
        sb.append("Players: [");
        if (players != null) {
            sb.append(players.stream().map(Player::getClientName).collect(Collectors.joining(",")));
        }
        sb.append("]");
        return sb.toString();
    }

}
