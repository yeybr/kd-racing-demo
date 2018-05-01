package com.solace.troubleflipper.messages;

import com.solace.troubleflipper.model.PuzzlePiece;

public class SelectPieceMessage {

    private PuzzlePiece piece;
    private String clientId;

    public PuzzlePiece getPiece() {
        return piece;
    }

    public void setPiece(PuzzlePiece piece) {
        this.piece = piece;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
