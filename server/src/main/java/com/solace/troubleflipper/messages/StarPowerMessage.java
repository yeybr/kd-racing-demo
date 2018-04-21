package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.PuzzlePiece;

public class StarPowerMessage {

    private PuzzlePiece puzzlePiece;

    public PuzzlePiece getPuzzlePiece() {
        return puzzlePiece;
    }

    public void setPuzzlePiece(PuzzlePiece puzzlePiece) {
        this.puzzlePiece = puzzlePiece;
    }
}
