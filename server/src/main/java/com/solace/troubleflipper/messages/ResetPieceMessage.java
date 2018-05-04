package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.PuzzlePiece;

public class ResetPieceMessage {

    private PuzzlePiece piece;

    public PuzzlePiece getPiece() {
        return piece;
    }

    public void setPiece(PuzzlePiece piece) {
        this.piece = piece;
    }

}
