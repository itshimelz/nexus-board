package com.himelz.nexusboard.model.pieces;

import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a bishop chess piece.
 * Moves diagonally any number of squares.
 */
public class Bishop extends ChessPiece {

    public Bishop(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'B';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♗" : "♝";
    }

    @Override
    public int getValue() {
        return 300; // Standard bishop value in centipawns
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Diagonal directions
        int[][] directions = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] direction : directions) {
            addMovesInDirection(moves, board, direction[0], direction[1]);
        }

        return moves;
    }


    @Override
    public boolean isValidMove(Position target, Board board) {
        int rowDiff = Math.abs(target.getRow() - position.getRow());
        int colDiff = Math.abs(target.getCol() - position.getCol());

        // Must move diagonally (equal row and column differences)
        if (rowDiff != colDiff || rowDiff == 0) {
            return false;
        }

        // Check if diagonal path is clear
        int rowDirection = Integer.compare(target.getRow(), position.getRow());
        int colDirection = Integer.compare(target.getCol(), position.getCol());

        Position current = position.add(rowDirection, colDirection);
        while (!current.equals(target)) {
            if (!board.isEmpty(current)) {
                return false; // Path blocked
            }
            current = current.add(rowDirection, colDirection);
        }

        // Target square must be empty or contain enemy piece
        ChessPiece targetPiece = board.getPiece(target);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }

    @Override
    public ChessPiece copy() {
        Bishop copy = new Bishop(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
