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
import java.util.stream.Collectors;

@Component
public class Tournament implements GameOverListener, BadGuyActionHandler {

    private static List<String> PUZZLE_NAMES = Arrays.asList("puzzle1", "puzzle2", "puzzle3", "puzzle4");

    private Logger log = LoggerFactory.getLogger("tournament");

    private final List<Player> players = new ArrayList<>();
    private volatile boolean started = false;
    private int waitCounter = 10;
    private Map<String, Team> teams = new HashMap<>();
    private Map<String, Game> activeGames = new HashMap<>();
    private Map<String, Collection<Game>> completedGames = new HashMap<>();
    private final LinkedList<Team> teamRankings = new LinkedList<>();
    private final LinkedList<Player> playerRankings = new LinkedList<>();

    private final Subscriber subscriber;
    private final Publisher publisher;

    private boolean stopGame;

    private final TournamentProperties tournamentProperties;

    private Timer timer = new Timer("TournamentTimer");
    private Random randomGen = new Random();

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
        synchronized (players) {
            Optional<Player> firstMatch = players.stream().filter(item ->
                    addUserMessage.getClientId().equals(item.getClientName())).findFirst();
            Player player;
            boolean present = false;
            if (firstMatch.isPresent()) {
                present = true;
                player = firstMatch.get();
                player.setGamerTag(addUserMessage.getUsername());
                log.trace("Player " + player.getClientName() + " is already registered");
            } else {
                player = new Player();
                player.setGamerTag(addUserMessage.getUsername());
                player.setClientName(addUserMessage.getClientId());
                players.add(player);
            }
            AddUserAckMessage addUserAckMessage = new AddUserAckMessage(addUserMessage, AddUserAckMessage.RESULT_SUCCESS);
            try {
                publisher.publish("user/" + player.getClientName(), addUserAckMessage);
                log.info("Player " + player.getGamerTag() + " with id " + player.getClientName() + " has been registered in the tournament");
                if (present) {
                    // check if there is active game and send out team information
                    Optional<Game> firstMatchGame = activeGames.values().stream().filter(item ->
                            item.getTeam().getPlayer(player.getClientName()) != null).findFirst();
                    if (firstMatchGame.isPresent()) {
                        Game activeGame = firstMatchGame.get();
                        log.info("Player is part of a game");
                        try {
                            subscriber.subscribeForClient("team/" + player.getTeam().getId(), player.getClientName());
                            subscriber.subscribeForClient("score/" + player.getTeam().getId(), player.getClientName());
                            subscriber.subscribeForClient("score/" + player.getClientName(), player.getClientName());
                            if (player.getCharacter() == null || !started) {
                                activeGame.updateCharactersForTeam(false);
                            } else {
                                activeGame.updatePuzzleForTeam();
                            }
                        } catch (SubscriberException ex) {
                            log.error("Unable to register subscription for " + player.getClientName() + " on team " + player.getTeam().getId(), ex);
                        }

                    }
                }
            } catch (PublisherException ex) {
                log.error("Unable to acknowledge player " + player.getClientName(), ex);
                players.remove(player);
                // TODO also update game related information for this player
            }
        }
    }

    @SubscriptionHandler(topic = "tournaments", messageType = TournamentMessage.class)
    private void startTournament(TournamentMessage tournamentMessage) {
        if ((tournamentMessage.getAction().equals("buildTeams")) && (players.size() > 0)) {
            if (activeGames.size() > 0) {
                log.info("Tournament is already in progress");
                return;
            }
            prepareTeams();
            for (Game game : activeGames.values()) {
                game.addGameOverListener(this);
                game.updateCharactersForTeam(false);
            }
        } else if (tournamentMessage.getAction().equals("stopGame")) {
            stopGame = true;
        }
    }

    private String getPuzzleName(Team team) {
        String puzzleName = null;
        if (team != null) {
            puzzleName = team.getNextPuzzleName();
        }
        if (puzzleName == null) {
            int index = randomGen.nextInt(PUZZLE_NAMES.size());
            puzzleName = PUZZLE_NAMES.get(index);
            log.info("puzzle name " + puzzleName);
        }
        return puzzleName;
    }

    private void prepareTeams() {
        waitCounter = 10;
        teams.clear();
        activeGames.clear();
        completedGames.clear();
        teamRankings.clear();
        playerRankings.clear();
        tournamentProperties.resetTeamNamesUsed();

        timer.cancel();
        timer.purge();
        timer = new Timer("TournamentTimer");
        synchronized (players) {
            if (players.size() == 0) {
                return;
            }
            int numberOfPlayers = players.size();
            int playersPerTeam = tournamentProperties.getPlayersPerTeam();
            int numberOfTeams = (int)Math.ceil(players.size() / playersPerTeam);
            log.debug("numberOfTeams " + numberOfTeams);
            List<Player> teamPlayers = null;
            String teamName = null;
            Player player;
            for (int i = 0; i < numberOfPlayers; i++) {
                if (teamPlayers == null) {
                    teamPlayers = new ArrayList<>();
                }
                player =  players.get(i);
                player.reset();
                teamPlayers.add(player);
                if (teamPlayers.size() == playersPerTeam || i == numberOfPlayers - 1) {
                    teamName = tournamentProperties.getNewTeamName();
                    addTeam(teamName, teamPlayers);
                    teamPlayers = null;
                }
            }
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                log.info("check character selection readiness");
                waitCounter--;
                if (waitCounter > 0) {
                    int totalGames = activeGames.size();
                    int readyGames = 0;
                    for (Game game : activeGames.values()) {
                        if (game.isCharacterReadyForTeam()) {
                            readyGames++;
                        }
                    }
                    if (readyGames == totalGames) {
                        this.cancel();
                        log.info("All games have selected characters");
                        startGames();
                    }
                } else {
                    this.cancel();
                    try {
                        log.info("force assign characters");
                        for (Game game : activeGames.values()) {
                            // send special pickCharacter message to avoid synchronization on team object
                            PickCharacterMessage pickCharacterMessage = new PickCharacterMessage();
                            pickCharacterMessage.setClientId("");
                            pickCharacterMessage.setCharacterType(CharacterType.mario);
                            publisher.publish("games/" + game.getTeam().getId() + "/pickCharacter", pickCharacterMessage);
                        }
                        startGames();
                    } catch (PublisherException ex) {
                        log.error("Unable to publish pick character message for game", ex);
                    }
                }
            }
        }, 0, 2000);

    }

    public void startGames() {
        started = true;
        for (Game game : activeGames.values()) {
            game.start();
            game.updatePuzzleForTeam();
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
                PlayerListMessage playersMessage = new PlayerListMessage();
                playersMessage.setPlayers(playerRankings);

                List<Map<String, String>> teams =
                        teamRankings.stream().map(team -> {
                            Map<String, String>teamMessage = new HashMap<>();
                            teamMessage.put("id", team.getId());
                            teamMessage.put("name", team.getName());
                            teamMessage.put("game", team.getGame().getPuzzleName());
                            return teamMessage;

                        }).collect(Collectors.toList());
                playersMessage.setTeams(teams);
                try {
                    publisher.publish("score/players", playersMessage);
                } catch (PublisherException ex) {
                    log.error("Unable to send player to score/players", ex);
                }


            }
        }, 0, 5000);

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

        if (teamName == null) {
            team.setName(team.getId());
        } else {
            team.setName(teamName);
        }
        team.setPuzzleNames(new ArrayList<String>(PUZZLE_NAMES));
        completedGames.put(team.getId(), new ArrayList<>());
        Game game = new Game(team, subscriber, publisher, timer, tournamentProperties, this);
        game.setPuzzleName(getPuzzleName(team));
        team.setGame(game);
        for (Player player : players) {
            team.addPlayer(player);
            player.setTeam(team);
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
        // remove game over listeners
//        game.clearGameOverListeners();
        team.addCompletedGame();
        Game newGame = new Game(team, subscriber, publisher, timer, tournamentProperties, this);
        newGame.setPuzzleName(getPuzzleName(team));
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
    public void troubleFlipper(Player bowserPlayer) {
        synchronized (teamRankings) {
            int rank = teamRankings.indexOf(bowserPlayer.getTeam());
            if (rank == 0) {
                log.info("Bowser from team " + bowserPlayer.getTeam().getName() + " used trouble flipper, but they are in first place");
            } else {
                int indexOfTeamToAttack = rank - 1;
                Team teamToAttack = teamRankings.get(indexOfTeamToAttack);
                teamToAttack.getGame().troubleFlipper(bowserPlayer);
                Game attackerGame = bowserPlayer.getTeam().getGame();
                if (attackerGame != null) {
                    attackerGame.updatePuzzleForTeam();
                }
            }
        }
    }

    @Override
    public void greenShell(Player goombaPlayer) {
        synchronized (teamRankings) {
            int rank = teamRankings.indexOf(goombaPlayer.getTeam());
            if (rank == 0) {
                log.info("Goomba from team " + goombaPlayer.getTeam().getName() + " used a green shell, but they are in first place");
            } else {
                int indexOfTeamToAttack = rank - 1;
                Team teamToAttack = teamRankings.get(indexOfTeamToAttack);
                teamToAttack.getGame().greenShell();
                Game attackerGame = goombaPlayer.getTeam().getGame();
                if (attackerGame != null) {
                    attackerGame.updatePuzzleForTeam();
                }
            }
        }
    }
}
