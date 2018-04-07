package com.solace.troubleflipper.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import org.apache.commons.lang.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {

    private List<PuzzlePiece> puzzleBoard = new ArrayList<>();
    private String puzzleName;
    private int columns;
    private int rows;
    private Team team;

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

    public void swapPieces(PuzzlePiece piece1, PuzzlePiece piece2) throws IOException {
        // TODO Abhi swap the pieces and publish the puzzle array
        PuzzlePiece bPiece1 = findPuzzlePiece(piece1.getIndex());
        PuzzlePiece bPiece2 = findPuzzlePiece(piece2.getIndex());
        bPiece1.setIndex(piece2.getIndex());
        bPiece2.setIndex(piece1.getIndex());
    }

    public PuzzlePiece findPuzzlePiece(int index) throws IOException {
        for (int i = 0; i < puzzleBoard.size(); ++i) {
            if (puzzleBoard.get(i).getIndex() == index) {
                return puzzleBoard.get(i);
            }
        }
        throw new IOException("No pieces found for piece index value: " + index);
    }

    public void start() {
        for (int i = 0; i < 25; i++) {
            PuzzlePiece puzzlePiece = new PuzzlePiece();
            puzzlePiece.setIndex(i);
            puzzleBoard.add(puzzlePiece);
        }
        Collections.shuffle(puzzleBoard);
    }

    public boolean isGameWon() {
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

    public TextMessage getTextMessage (ObjectMapper mapper) throws JsonProcessingException {
        TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
        UpdatePuzzleMessage updatePuzzleMessage = new UpdatePuzzleMessage();
        updatePuzzleMessage.setTeamId(team.getId());
        updatePuzzleMessage.setPuzzle(puzzleBoard);
        textMessage.setText(mapper.writeValueAsString(updatePuzzleMessage));
        return textMessage;
    }
}