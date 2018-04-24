package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.Player;
import com.solace.troubleflipper.model.PuzzlePiece;

import java.util.List;
import java.util.stream.Collectors;

public class UpdatePuzzleMessage {

    private List<PuzzlePiece> puzzle;

    private String teamId;

    private String puzzleName;

    private Boolean gameWon;

    private List<Player> players;

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

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
