package com.solace.troubleflipper;

import com.solace.troubleflipper.model.Character;
import com.solace.troubleflipper.model.Game;
import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.Team;
import com.solace.troubleflipper.properties.TournamentProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;

@Component
public class Tournament {

    private List<Player> players = new ArrayList<>();
    private boolean started = false;
    private Map<String, Team> teams = new HashMap<>();
    private Map<String, Game> activeGames = new HashMap<>();
    private Map<String, Collection<Game>> completedGames = new HashMap<>();

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

        if (players.size() >= 4) {
            for (int i = 0; i < Math.round(players.size() / 2); ++i) {
                int start = i * 2;
                List<Player> teamPlayers = new ArrayList<>();
                teamPlayers.add(players.get(start));
                teamPlayers.add(players.get(start + 1));
                addTeam("Team " + (i + 1), teamPlayers);
            }
        } else {
            addTeam("Team", players);
        }
    }

    private void addTeam(String teamName, Collection<Player> players) {
        Team team = new Team();
        team.setName(teamName);
        completedGames.put(team.getId(), new ArrayList<>());
        Game game = new Game();
        game.setTeam(team);
        team.setGame(game);
        for (Player player : players) {
            team.addPlayer(player);
            player.setTeam(team);
            // TODO need to move this logic to character selection message handling
            team.chooseCharacter(Character.mario, player);
        }

        teams.put(team.getId(), team);

        activeGames.put(team.getId(), game);
    }

    public Game getGame(String teamId) {
        return activeGames.get(teamId);
    }

    public Collection<Game> getGames() {
        return activeGames.values();
    }

    public Game nextGame(String teamId) {
        Team team = teams.get(teamId);
        Game game = activeGames.remove(teamId);
        completedGames.get(teamId).add(game);
        Game newGame = new Game();
        newGame.setTeam(teams.get(teamId));
        team.setGame(newGame);
        activeGames.put(teamId, newGame);
        return newGame;
    }
}
