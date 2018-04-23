package com.solace.troubleflipper.model;

import com.solace.troubleflipper.GameOverListener;
import com.solace.troubleflipper.Publisher;
import com.solace.troubleflipper.Subscriber;
import com.solace.troubleflipper.messages.PeachHealMessage;
import com.solace.troubleflipper.messages.StarPowerMessage;
import com.solace.troubleflipper.messages.SwapPiecesMessage;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solace.troubleflipper.properties.BadGuyActionHandler;
import com.solace.troubleflipper.properties.TournamentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Game {

    private Logger log = LoggerFactory.getLogger("game");

    private final List<PuzzlePiece> puzzleBoard = new ArrayList<>();
    private String puzzleName;
    private Team team;

    private Subscriber subscriber;
    private Publisher publisher;
    private Timer timer;
    private final TournamentProperties tournamentProperties;
    private final BadGuyActionHandler badGuyActionHandler;
    private boolean gameOver = false;

    private int correctPieces;

    private final Collection<GameOverListener> gameOverListeners = new ArrayList<>();

    public Game(Team team, Subscriber subscriber, Publisher publisher, Timer timer,
                TournamentProperties tournamentProperties, BadGuyActionHandler badGuyActionHandler) {
        this.team = team;
        this.subscriber = subscriber;
        this.publisher = publisher;
        this.timer = timer;
        this.tournamentProperties = tournamentProperties;
        this.badGuyActionHandler = badGuyActionHandler;
        subscriber.registerHandler(SwapPiecesMessage.class, "games/" + team.getId(), this::swapPieces);
        subscriber.registerHandler(StarPowerMessage.class, "games/" + team.getId() + "/starPower", this::starPowerHandler);
        subscriber.registerHandler(PeachHealMessage.class, "games/" + team.getId() + "/peachHeal", this::peachHealHandler);
        subscriber.registerHandler("games/" + team.getId() + "/yoshiGuard", this::yoshiGuardHandler);
        subscriber.registerHandler("games/" + team.getId() + "/troubleFlipper", this::troubleFlipperHandler);
        subscriber.registerHandler("games/" + team.getId() + "/greenShell", this::greenShellHandler);
    }

    public void addGameOverListener(GameOverListener gameOverListener) {
        this.gameOverListeners.add(gameOverListener);
    }


    public Team getTeam() {
        return team;
    }

    private void swapPieces(PuzzlePiece piece1, PuzzlePiece piece2, Player player) {
        synchronized (puzzleBoard) {
            try {
                PuzzlePiece bPiece1 = findPuzzlePiece(piece1.getIndex());
                PuzzlePiece bPiece2 = findPuzzlePiece(piece2.getIndex());
                bPiece1.setIndex(piece2.getIndex());
                bPiece2.setIndex(piece1.getIndex());
            } catch (NoPieceFoundException ex) {
                log.error("Unable to swap pieces " + piece1.getIndex() + " and " + piece2.getIndex(), ex);
            }
        }
        if (player != null) {
            int piece1Position = puzzleBoard.indexOf(piece1);
            int piece2Position = puzzleBoard.indexOf(piece2);
            if (piece1Position == piece1.getIndex()) {
                player.wrongMove();
            }
            if (piece2Position == piece2.getIndex()) {
                player.wrongMove();
            }
            if (piece1Position == piece2.getIndex()) {
                player.rightMove();
            }
            if (piece2Position == piece1.getIndex()) {
                player.rightMove();
            }
        }
    }

    private PuzzlePiece findPuzzlePiece(int index) throws NoPieceFoundException {
        for (PuzzlePiece puzzlePiece : puzzleBoard) {
            if (puzzlePiece.getIndex() == index) {
                return puzzlePiece;
            }
        }
        throw new NoPieceFoundException("No pieces found for piece index value: " + index + " for team " + team.getId());
    }

    public void start() {
        synchronized (puzzleBoard) {
            int puzzleLength = tournamentProperties.getPuzzleSize() * tournamentProperties.getPuzzleSize();
            for (int i = 0; i < puzzleLength ; i++) {
                PuzzlePiece puzzlePiece = new PuzzlePiece();
                puzzlePiece.setIndex(i);
                puzzleBoard.add(puzzlePiece);
            }
            Collections.shuffle(puzzleBoard);
        }
    }

    public void updatePuzzleForTeam() {
        boolean won = isGameWon();
        UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
        updatePuzzleMessage.setTeamId(team.getId());
        updatePuzzleMessage.setPuzzle(puzzleBoard);
        updatePuzzleMessage.setGameWon(won);
        try {
            publisher.publish("team/" + team.getId(), updatePuzzleMessage);
        } catch (PublisherException ex) {
            log.error("Unable to update puzzle for team " + team.getId(), ex);
        }
        if (won) {
            log.info("Team " + team.getId() + " won the game!");
            subscriber.deregisterHandler("games/" + team.getId());
            subscriber.deregisterHandler("games/" + team.getId() + "/starPower");
            subscriber.deregisterHandler("games/" + team.getId() + "/peachHeal");
            subscriber.deregisterHandler("games/" + team.getId() + "/yoshiGuard");
            subscriber.deregisterHandler("games/" + team.getId() + "/troubleFlipper");
            subscriber.deregisterHandler("games/" + team.getId() + "/greenShell");
            gameOverListeners.forEach(l -> l.gameOver(this));
        }
    }

    private boolean isGameWon() {
        synchronized (puzzleBoard) {
            boolean result = true;
            correctPieces = 0;
            for (int i = 0; i < puzzleBoard.size(); ++i) {
                if (puzzleBoard.get(i).getIndex() == i) {
                    correctPieces++;
                } else {
                    result = false;
                }
            }
            if (result) {
                gameOver = true;
            }
            return result;
        }
    }

    public int getCorrectPieces() {
        return correctPieces;
    }

    private void swapPieces(SwapPiecesMessage swapPiecesMessage) {
        Player player = team.getPlayer(swapPiecesMessage.getClientName());
        swapPieces(swapPiecesMessage.getPiece1(), swapPiecesMessage.getPiece2(), player);
        updatePuzzleForTeam();
    }

    private void starPowerHandler(StarPowerMessage starPowerMessage) {
        Mario mario = (Mario) getTeam().getPlayer(Character.mario);
        if (mario.getStarPowerUps() > 0) {
            log.debug("Mario used star power");
            mario.useStarPowerUp();
            starPower(starPowerMessage.getPuzzlePiece());
            updatePuzzleForTeam();
        }
    }

    private void peachHealHandler(PeachHealMessage peachHealMessage) {
        // TODO wait until player selection logic before checking
//                                Peach peach = (Peach) game.getTeam().getPlayer(Character.peach);
//                                if (peach.isHealUsed()) {
//                                    peach.useHeal();
        Character character = peachHealMessage.getCharacter();
        Player player = getTeam().getPlayer(character);
        player.heal();
//    }
    }

    private void yoshiGuardHandler() {
        Yoshi yoshi = (Yoshi) getTeam().getPlayer(Character.yoshi);
        if (!yoshi.isImmuneUsed()) {
            yoshi.useImmune();
            team.setImmune(true);
            log.info("Team " + team.getName() + " is protected by Yoshi Guard for the next 10 seconds");
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    team.setImmune(false);
                    log.info("Team " + team.getName() + " is no longer protected by Yoshi Guard");
                }
            }, 10000);
        }
    }

    private void troubleFlipperHandler() {
        Bowser bowser = (Bowser) getTeam().getPlayer(Character.bowser);
        if (bowser.isTroubleFlipperUsed()) {
            bowser.useTroubleFlipper();
            badGuyActionHandler.troubleFlipper(bowser);
        }
    }

    private void greenShellHandler() {
        Goomba goomba = (Goomba) getTeam().getPlayer(Character.goomba);
        if (goomba.getGreenShells() > 0) {
            goomba.useGreenShell();
            badGuyActionHandler.greenShell(goomba);
        }
    }

    private void starPower(PuzzlePiece selectedPuzzlePiece) {
        int correctIndexForPuzzlePiece = selectedPuzzlePiece.getIndex();
        Player mario = team.getPlayer(Character.mario);
        swapPieces(selectedPuzzlePiece, puzzleBoard.get(correctIndexForPuzzlePiece), mario);
    }

    public void troubleFlipper() {
        if (!gameOver && !team.isImmune()) {
            synchronized (puzzleBoard) {
                Collections.shuffle(puzzleBoard);
            }
            updatePuzzleForTeam();
        } else if (team.isImmune()) {
            log.info("Team " + team.getName() + " has yoshi guarded a trouble flipper attack!!!");
        }
    }

    public void greenShell() {
        if (!gameOver && !team.isImmune()) {
            List<PuzzlePiece> correctPieces = new ArrayList<>();
            synchronized (puzzleBoard) {
                for (int i = 0; i < puzzleBoard.size(); ++i) {
                    PuzzlePiece puzzlePiece = puzzleBoard.get(i);
                    if (puzzlePiece.getIndex() == i) {
                        correctPieces.add(puzzlePiece);
                    }
                }
                Collections.shuffle(correctPieces);
            }
            if (correctPieces.size() >= 2) {
                swapPieces(correctPieces.get(0), correctPieces.get(1), null);
            } else if (correctPieces.size() == 1) {
                // TODO should probably swap a single piece with a random piece here
            }
            updatePuzzleForTeam();
        } else if (team.isImmune()) {
            log.info("Team " + team.getName() + " has yoshi guarded a green shell attack!");
        }
    }

}