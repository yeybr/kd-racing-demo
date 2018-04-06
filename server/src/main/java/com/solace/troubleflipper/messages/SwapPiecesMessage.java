package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.PuzzlePiece;

public class SwapPiecesMessage {

    private PuzzlePiece piece1;
    private PuzzlePiece piece2;

    public PuzzlePiece getPiece1() {
        return piece1;
    }

    public void setPiece1(PuzzlePiece piece1) {
        this.piece1 = piece1;
    }

    public PuzzlePiece getPiece2() {
        return piece2;
    }

    public void setPiece2(PuzzlePiece piece2) {
        this.piece2 = piece2;
    }
}
