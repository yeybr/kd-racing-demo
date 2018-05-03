package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.PuzzlePiece;

import java.util.List;
import java.util.stream.Collectors;

public class UpdatePuzzleMessage {

    private List<PuzzlePiece> puzzle;

    private String teamId;

    private String teamName;

    private String puzzleName;

    private int correctPieces;

    private Boolean gameWon;

    private int completedGames;

    private List<Player> players;

    private boolean gameOver;


    public List<PuzzlePiece> getPuzzle() {
        return puzzle;
    }

    public void setPuzzle(List<PuzzlePiece> puzzle) {
        this.puzzle = puzzle;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getPuzzleName() {
        return puzzleName;
    }

    public void setPuzzleName(String puzzleName) {
        this.puzzleName = puzzleName;
    }

    public Boolean getGameWon() {
        return gameWon;
    }

    public void setGameWon(Boolean gameWon) {
        this.gameWon = gameWon;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public int getCorrectPieces() {
        return correctPieces;
    }

    public void setCorrectPieces(int correctPieces) {
        this.correctPieces = correctPieces;
    }

    public int getCompletedGames() {
        return completedGames;
    }

    public void setCompletedGames(int completedGames) {
        this.completedGames = completedGames;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Puzzle: [");
        if (puzzle != null) {
            sb.append(puzzle.stream().map(PuzzlePiece::getIndex).map(Object::toString).collect(Collectors.joining(",")));
        }
        sb.append("]");
        return sb.toString();
    }

}
