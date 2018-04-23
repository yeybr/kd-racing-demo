package com.solace.troubleflipper;

import com.solace.troubleflipper.configuration.SubscriptionHandler;
import com.solace.troubleflipper.messages.*;
import com.solace.troubleflipper.model.*;
import com.solace.troubleflipper.model.Character;
import com.solace.troubleflipper.properties.BadGuyActionHandler;
import com.solace.troubleflipper.properties.TournamentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Tournament implements GameOverListener, BadGuyActionHandler {

    private Logger log = LoggerFactory.getLogger("tournament");

    private List<Player> players = new ArrayList<>();
    private Map<String, Team> teams = new HashMap<>();
    private Map<String, Game> activeGames = new HashMap<>();
    private Map<String, Collection<Game>> completedGames = new HashMap<>();
    private final LinkedList<Team> teamRankings = new LinkedList<>();
    private final LinkedList<Player> playerRankings = new LinkedList<>();

    private final Subscriber subscriber;
    private final Publisher publisher;

    private final TournamentProperties tournamentProperties;

    private Timer timer = new Timer("TournamentTimer");

    @Autowired
    public Tournament(TournamentProperties tournamentProperties,
                      Subscriber subscriber, Publisher publisher) {
        this.tournamentProperties = tournamentProperties;
        this.subscriber = subscriber;
        this.publisher = publisher;
        subscriber.registerHandler(AddUserMessage.class, "users", this::addUser);
        subscriber.registerHandler(TournamentMessage.class, "tournaments", this::startTournament);
    }

    @SubscriptionHandler(topic = "users", messageType = AddUserMessage.class)
    private void addUser(AddUserMessage addUserMessage) {
        Optional<Player> firstMatch = players.stream().filter(item ->
                addUserMessage.getClientId().equals(item.getClientName())).findFirst();
        Player player;
        if (firstMatch.isPresent()) {
            player = firstMatch.get();
            log.trace("Player " + player.getClientName() + " is already registered");
        } else {
            // TODO we need to distinguish people in the queue vs players in a game
            player = new Mario();
            player.setGamerTag(addUserMessage.getUsername());
            player.setClientName(addUserMessage.getClientId());
            players.add(player);
        }
        AddUserAckMessage addUserAckMessage = new AddUserAckMessage(addUserMessage, AddUserAckMessage.RESULT_SUCCESS);
        try {
            publisher.publish("user/" + player.getClientName(), addUserAckMessage);
            log.info("Player " + player.getClientName() + " has been registered in the tournament");
        } catch (PublisherException ex) {
            log.error("Unable to acknowledge player " + player.getClientName(), ex);
            players.remove(player);
        }
    }

    @SubscriptionHandler(topic = "tournaments", messageType = TournamentMessage.class)
    private void startTournament(TournamentMessage tournamentMessage) {
        if ((tournamentMessage.getAction().equals("buildTeams")) && (players.size() > 0)) {
            prepareTeams();
            for (Player player : players) {
                playerRankings.add(player);
                try {
                    subscriber.subscribeForClient("team/" + player.getTeam().getId(), player.getClientName());
                } catch (SubscriberException ex) {
                    log.error("Unable to register subscription for " + player.getClientName() + " on team " + player.getTeam().getId(), ex);
                }
            }
            for (Game game : activeGames.values()) {
                game.addGameOverListener(this);
                game.start();
                game.updatePuzzleForTeam();
            }
        }
    }

    private void prepareTeams() {
        // TODO implement an algorithm to create teams, name them, and assign players to them
        teams.clear();
        activeGames.clear();
        completedGames.clear();
        teamRankings.clear();
        playerRankings.clear();
        timer.cancel();
        timer.purge();
        timer = new Timer("TournamentTimer");
        tournamentProperties.setPlayersPerTeam(players.size());

        if (players.size() >= 4) {
            for (int i = 0; i < Math.round(players.size() / 2); ++i) {
                int start = i * 2;
                List<Player> teamPlayers = new ArrayList<>();
                teamPlayers.add(players.get(start));
                teamPlayers.add(players.get(start + 1));
                String newName = tournamentProperties.getNewTeamName();
                addTeam(newName, teamPlayers);
            }
        } else {
            String newName = tournamentProperties.getNewTeamName();
            addTeam(newName, players);
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                playerRankings.sort((player1, player2) -> {
                    int player1Score = player1.getRightMoves() - player1.getWrongMoves();
                    int player2Score = player2.getRightMoves() - player2.getWrongMoves();
                    return player2Score - player1Score;
                });
                for (int i = 0; i < playerRankings.size(); ++i) {
                    Player player = playerRankings.get(i);
                    PlayerRankMessage playerRankMessage = new PlayerRankMessage();
                    playerRankMessage.setRank(i + 1);
                    playerRankMessage.setId(player.getClientName());
                    playerRankMessage.setTotalPlayers(playerRankings.size());
                    try {
                        publisher.publish("score/" + player.getClientName(), playerRankMessage);
                    } catch (PublisherException ex) {
                        log.error("Unable to update the scores for players", ex);
                    }
                }
            }
        }, 0 , 5000);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (teamRankings) {
                    teamRankings.sort((team1, team2) -> {
                        int team1Winning = team2.getCompletedGames() - team1.getCompletedGames();
                        if (team1Winning == 0) {
                            team1Winning = team2.getGame().getCorrectPieces() - team1.getGame().getCorrectPieces();
                        }
                        return team1Winning;
                    });
                    for (int i = 0; i < teamRankings.size(); ++i) {
                        Team team = teamRankings.get(i);
                        TeamRankMessage teamRankMessage = new TeamRankMessage();
                        teamRankMessage.setRank(i + 1);
                        teamRankMessage.setTeamId(team.getId());
                        teamRankMessage.setTotalTeams(teamRankings.size());
                        try {
                            publisher.publish("score/" + team.getId(), teamRankMessage);
                        } catch (PublisherException ex) {
                            log.error("Unable to update the scores", ex);
                        }
                    }
                }
            }
        }, 0 , 3000);
    }

    private void addTeam(String teamName, Collection<Player> players) {
        Team team = new Team();

        team.setName(teamName);
        completedGames.put(team.getId(), new ArrayList<>());
        Game game = new Game(team, subscriber, publisher, timer, tournamentProperties, this);
        team.setGame(game);
        for (Player player : players) {
            team.addPlayer(player);
            player.setTeam(team);
            // TODO need to move this logic to character selection message handling
            team.chooseCharacter(Character.mario, player);
        }

        teams.put(team.getId(), team);
        teamRankings.add(team);

        activeGames.put(team.getId(), game);

        for (Player player : players) {
            playerRankings.add(player);
            try {
                subscriber.subscribeForClient("team/" + player.getTeam().getId(), player.getClientName());
                subscriber.subscribeForClient("score/" + player.getTeam().getId(), player.getClientName());
                subscriber.subscribeForClient("score/" + player.getClientName(), player.getClientName());
            } catch (SubscriberException ex) {
                log.error("Unable to register subscription for " + player.getClientName() + " on team " + player.getTeam().getId(), ex);
            }
        }
    }

    public void gameOver(Game game) {
        String teamId = game.getTeam().getId();
        Team team = teams.get(teamId);
        activeGames.remove(teamId);
        completedGames.get(teamId).add(game);
        team.addCompletedGame();
        Game newGame = new Game(team, subscriber, publisher, timer, tournamentProperties, this);
        team.setGame(newGame);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                activeGames.put(teamId, newGame);
                newGame.addGameOverListener(Tournament.this);
                newGame.start();
                newGame.updatePuzzleForTeam();
            }
        }, 3000);
    }

    @Override
    public void troubleFlipper(Bowser bowser) {
        synchronized (teamRankings) {
            int rank = teamRankings.indexOf(bowser.getTeam());
            if (rank == 0) {
                log.info("Bowser from team " + bowser.getTeam().getName() + " used trouble flipper, but they are in first place");
            } else {
                int indexOfTeamToAttack = rank - 1;
                Team teamToAttack = teamRankings.get(indexOfTeamToAttack);
                teamToAttack.getGame().troubleFlipper();
            }
        }
    }

    @Override
    public void greenShell(Goomba goomba) {
        synchronized (teamRankings) {
            int rank = teamRankings.indexOf(goomba.getTeam());
            if (rank == 0) {
                log.info("Goomba from team " + goomba.getTeam().getName() + " used a green shell, but they are in first place");
            } else {
                int indexOfTeamToAttack = rank - 1;
                Team teamToAttack = teamRankings.get(indexOfTeamToAttack);
                teamToAttack.getGame().greenShell();
            }
        }
    }
}
