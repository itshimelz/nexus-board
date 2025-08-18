package com.himelz.nexusboard.model.pieces;

import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a knight chess piece.
 * Moves in an L-shape: 2 squares in one direction and 1 square perpendicular.
 */
public class Knight extends ChessPiece {

    public Knight(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'N';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♘" : "♞";
    }

    @Override
    public int getValue() {
        return 300; // Standard knight value in centipawns
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // All possible knight moves (L-shapes)
        int[][] knightMoves = {
            {-2, -1}, {-2, 1}, {-1, -2}, {-1, 2},
            {1, -2}, {1, 2}, {2, -1}, {2, 1}
        };

        for (int[] move : knightMoves) {
            Position target = position.add(move[0], move[1]);

            if (isValidPosition(target)) {
                ChessPiece targetPiece = board.getPiece(target);

                if (targetPiece == null) {
                    // Empty square
                    moves.add(new Move(position, target, this));
                } else if (targetPiece.getColor() != this.color) {
                    // Enemy piece - capture
                    moves.add(new Move(position, target, this, targetPiece));
                }
                // If friendly piece, skip (can't move there)
            }
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Position target, Board board) {
        int rowDiff = Math.abs(target.getRow() - position.getRow());
        int colDiff = Math.abs(target.getCol() - position.getCol());

        // Must move in L-shape
        boolean isLShape = (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2);
        if (!isLShape) {
            return false;
        }

        // Target square must be empty or contain enemy piece
        ChessPiece targetPiece = board.getPiece(target);
        return targetPiece == null || targetPiece.getColor() != this.color;
    }

    @Override
    public ChessPiece copy() {
        Knight copy = new Knight(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}