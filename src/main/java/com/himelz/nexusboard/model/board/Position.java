package com.himelz.nexusboard.model.board;

import java.util.Objects;


 // Represents a position on the chess board using row and column coordinates.
 // Row 0 is the top of the board (black's back rank), Row 7 is the bottom (white's back rank).

public class Position {
    private final int row;
    private final int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    // Convert to algebraic notation (a1, e4, h8)
    public String toAlgebraic() {
        char file = (char) ('a' + col);
        int rank = 8 - row; // Convert from 0-based row to 1-based rank
        return "" + file + rank;
    }


    // Create position from algebraic notation
    public static Position fromAlgebraic(String algebraic) {
        if (algebraic.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        char file = algebraic.charAt(0);
        char rank = algebraic.charAt(1);

        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Invalid algebraic notation: " + algebraic);
        }

        int col = file - 'a';
        int row = 8 - (rank - '0'); // Convert from 1-based rank to 0-based row

        return new Position(row, col);
    }

    //Add offset to current position
    public Position add(int rowOffset, int colOffset) {
        return new Position(row + rowOffset, col + colOffset);
    }

    //Calculate Manhattan distance to another position
    public int distanceTo(Position other) {
        return Math.abs(this.row - other.row) + Math.abs(this.col - other.col);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return row == position.row && col == position.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return toAlgebraic();
    }
}
