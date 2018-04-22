package com.solace.troubleflipper.model;

public class PuzzlePiece {

    private int index;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PuzzlePiece that = (PuzzlePiece) o;

        return index == that.index;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
