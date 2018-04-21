package com.solace.troubleflipper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import org.apache.commons.lang.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.LogManager;

public class Game {

    private Logger log = LoggerFactory.getLogger("game");

    private final List<PuzzlePiece> puzzleBoard = new ArrayList<>();
    private String puzzleName;
    private int columns;
    private int rows;
    private Team team;
    private boolean immune = false;

    public List<PuzzlePiece> getPuzzleBoard() {
        return puzzleBoard;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
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

    public void starPower(PuzzlePiece selectedPuzzlePiece) {
        int correctIndexForPuzzlePiece = selectedPuzzlePiece.getIndex();
        swapPieces(selectedPuzzlePiece, puzzleBoard.get(correctIndexForPuzzlePiece));
    }

    public boolean troubleFlipper() {
        if (!immune) {
            synchronized (puzzleBoard) {
                Collections.shuffle(puzzleBoard);
            }
            return true;
        }
        return false;
    }

    public boolean greenShell() {
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
                return true;
            } else if (correctPieces.size() == 1) {
                // TODO should probably swap a single piece with a random piece here
            }
        }
        return false;
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