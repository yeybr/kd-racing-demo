package com.solace.troubleflipper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.GameOverListener;
import com.solace.troubleflipper.Publisher;
import com.solace.troubleflipper.Subscriber;
import com.solace.troubleflipper.messages.PeachHealMessage;
import com.solace.troubleflipper.messages.StarPowerMessage;
import com.solace.troubleflipper.messages.SwapPiecesMessage;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Game {

    private Logger log = LoggerFactory.getLogger("game");

    private final List<PuzzlePiece> puzzleBoard = new ArrayList<>();
    private String puzzleName;
    private Team team;
    private boolean immune = false;

    private Subscriber subscriber;
    private Publisher publisher;
    private Timer timer;

    private final Collection<GameOverListener> gameOverListeners = new ArrayList<>();

    public Game(Team team, Subscriber subscriber, Publisher publisher, Timer timer) {
        this.team = team;
        this.subscriber = subscriber;
        this.publisher = publisher;
        this.timer = timer;
        subscriber.registerHandler(SwapPiecesMessage.class, "games/" + team.getId(), this::swapPieces);
        subscriber.registerHandler(StarPowerMessage.class, "games/" + team.getId() + "/starPower", this::starPowerHandler);
        subscriber.registerHandler(PeachHealMessage.class, "games/" + team.getId() + "/peachHeal", this::peachHealHandler);
        subscriber.registerHandler("games/" + team.getId() + "/yoshiGuard", this::yoshiGuardHandler);
        subscriber.registerHandler("games/" + team.getId() + "/troubleFlipper", this::troubleFlipper);
        subscriber.registerHandler("games/" + team.getId() + "/greenShell", this::greenShell);
    }

    public void addGameOverListener(GameOverListener gameOverListener) {
        this.gameOverListeners.add(gameOverListener);
    }


    public List<PuzzlePiece> getPuzzleBoard() {
        return puzzleBoard;
    }

    public Team getTeam() {
        return team;
    }

    public void swapPieces(PuzzlePiece piece1, PuzzlePiece piece2) {
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
    }

    public PuzzlePiece findPuzzlePiece(int index) throws NoPieceFoundException {
        for (PuzzlePiece puzzlePiece : puzzleBoard) {
            if (puzzlePiece.getIndex() == index) {
                return puzzlePiece;
            }
        }
        throw new NoPieceFoundException("No pieces found for piece index value: " + index + " for team " + team.getId());
    }

    public void start() {
        synchronized (puzzleBoard) {
            for (int i = 0; i < 25; i++) {
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
        updatePuzzleMessage.setPuzzle(getPuzzleBoard());
        updatePuzzleMessage.setGameWon(won);
        try {
            publisher.publish("team/" + team.getId(), updatePuzzleMessage);
        } catch (PublisherException ex) {
            log.error("Unable to update puzzle for team " + team.getId(), ex);
        }
        if (won) {
            gameOverListeners.forEach(l -> l.gameOver(this));
        }
    }

    public boolean isGameWon() {
        synchronized (puzzleBoard) {
            boolean result = true;
            for (int i = 0; i < puzzleBoard.size(); ++i) {
                if (puzzleBoard.get(i).getIndex() == i) {
                    continue;
                }
                result = false;
                break;
            }
            return result;
        }
    }

    private void swapPieces(SwapPiecesMessage swapPiecesMessage) {
        swapPieces(swapPiecesMessage.getPiece1(), swapPiecesMessage.getPiece2());
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
        if (yoshi.isImmuneUsed()) {
            yoshi.useImmune();
            setImmune(true);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    setImmune(false);
                }
            }, 10000);
        }
    }

    public void starPower(PuzzlePiece selectedPuzzlePiece) {
        int correctIndexForPuzzlePiece = selectedPuzzlePiece.getIndex();
        swapPieces(selectedPuzzlePiece, puzzleBoard.get(correctIndexForPuzzlePiece));
    }

    public void troubleFlipper() {
        if (!immune) {
            synchronized (puzzleBoard) {
                Collections.shuffle(puzzleBoard);
            }
        }
    }

    public void greenShell() {
        if (!immune) {
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
                swapPieces(correctPieces.get(0), correctPieces.get(1));
            } else if (correctPieces.size() == 1) {
                // TODO should probably swap a single piece with a random piece here
            }
        }
    }

    public TextMessage getTextMessage (ObjectMapper mapper) throws JsonProcessingException {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
        updatePuzzleMessage.setTeamId(team.getId());
        updatePuzzleMessage.setPuzzle(puzzleBoard);
        updatePuzzleMessage.setGameWon(false);
        textMessage.setText(mapper.writeValueAsString(updatePuzzleMessage));
        return textMessage;
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
    }
}