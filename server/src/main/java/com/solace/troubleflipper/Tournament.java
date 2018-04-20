package com.solace.troubleflipper;

import com.solace.troubleflipper.model.Game;
import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.Team;
import com.solace.troubleflipper.properties.TournamentProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class Tournament {

    private Collection<Player> players = new ArrayList<>();
    private boolean started = false;
    private List<Team> teams;
    private List<Game> games;

    private final JCSMPSession jcsmpSession;

    private final TournamentProperties tournamentProperties;

    @Autowired
    public Tournament(TournamentProperties tournamentProperties, JCSMPSession jcsmpSession) {
        this.tournamentProperties = tournamentProperties;
        this.jcsmpSession = jcsmpSession;
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public void prepareTeams() {
        // TODO implement an algorithm to create teams, name them, and assign players to them
        tournamentProperties.setPlayersPerTeam(players.size());
        tournamentProperties.setTeamNames(new String[]{"Team"});

        Team team = new Team();
        team.setName("Team");
        Game game = new Game();
        game.setTeam(team);
        team.setGame(game);

        for (Player player : players) {
            team.addPlayer(player);
        }

        teams = new ArrayList<>();
        teams.add(team);

        games = new ArrayList<>();
        games.add(game);
    }

    public Game getGame(String teamId) throws IOException {
        if (teams == null) {
            return null;
        }
        for(Team team:teams) {
            if(team.getId().equals(teamId)) {
                return team.getGame();
            }
        }
        return null;
    }

    public List<Game> getGames() {
        return games;
    }
}
