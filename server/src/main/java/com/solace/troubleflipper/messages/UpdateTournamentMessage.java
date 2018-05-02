package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.PuzzlePiece;
import com.solace.troubleflipper.model.Team;

import java.util.List;
import java.util.stream.Collectors;

public class UpdateTournamentMessage {

    private List<Player> waitingPlayers;

    private boolean started;

    private List<Team> teams;

    public List<Player> getWaitingPlayers() {
        return waitingPlayers;
    }

    public void setWaitingPlayers(List<Player> waitingPlayers) {
        this.waitingPlayers = waitingPlayers;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }
}
