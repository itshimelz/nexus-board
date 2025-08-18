package com.himelz.nexusboard.model.pieces;
import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a queen chess piece.
 * Combines the movement of rook and bishop - moves horizontally, vertically, and diagonally.
 */
public class Queen extends ChessPiece {

    public Queen(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'Q';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♕" : "♛";
    }

    @Override
    public int getValue() {
        return 900; // Standard queen value in centipawns
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // All 8 directions: horizontal, vertical, and diagonal
        int[][] directions = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0},    // Rook moves
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}   // Bishop moves
        };

        for (int[] direction : directions) {
            addMovesInDirection(moves, board, direction[0], direction[1]);
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Position target, Board board) {
        int rowDiff = Math.abs(target.getRow() - position.getRow());
        int colDiff = Math.abs(target.getCol() - position.getCol());

        // Must move in straight line (horizontal, vertical, or diagonal)
        boolean isHorizontal = rowDiff == 0 && colDiff > 0;
        boolean isVertical = colDiff == 0 && rowDiff > 0;
        boolean isDiagonal = rowDiff == colDiff && rowDiff > 0;

        if (!isHorizontal && !isVertical && !isDiagonal) {
            return false;
        }

        // Check if path is clear
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
        Queen copy = new Queen(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
