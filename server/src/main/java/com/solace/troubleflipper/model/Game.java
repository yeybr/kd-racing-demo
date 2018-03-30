package com.solace.troubleflipper.model;

import java.util.ArrayList;
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

    public void swapPieces(PuzzlePiece piece1, PuzzlePiece piece2) {
        // TODO Abhi swap the pieces and publish the puzzle array
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
}
