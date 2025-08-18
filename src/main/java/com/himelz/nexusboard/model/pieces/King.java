package com.himelz.nexusboard.model.pieces;


import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a king chess piece.
 * Moves one square in any direction. Cannot move into check.
 * Handles castling when conditions are met.
 */
public class King extends ChessPiece {

    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'K';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♔" : "♚";
    }

    @Override
    public int getValue() {
        return 20000; // King is invaluable - highest value
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // All 8 directions around the king (one square each)
        int[][] directions = {
            {-1, -1}, {-1, 0}, {-1, 1},
            {0, -1},           {0, 1},
            {1, -1},  {1, 0},  {1, 1}
        };

        for (int[] direction : directions) {
            Position target = position.add(direction[0], direction[1]);

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

        // Add castling moves (will be filtered by game state for legality)
        if (!hasMoved) {
            addCastlingMoves(moves, board);
        }

        return moves;
    }

    private void addCastlingMoves(List<Move> moves, Board board) {
        int row = color == Color.WHITE ? 7 : 0;

        // Kingside castling (O-O)
        Position kingsideRookPos = new Position(row, 7);
        ChessPiece kingsideRook = board.getPiece(kingsideRookPos);

        if (kingsideRook instanceof Rook && !kingsideRook.hasMoved() &&
            board.isEmpty(new Position(row, 5)) && board.isEmpty(new Position(row, 6))) {

            Position kingTarget = new Position(row, 6);
            moves.add(new Move(position, kingTarget, this, null, Move.MoveType.CASTLE_KINGSIDE, null));
        }

        // Queenside castling (O-O-O)
        Position queensideRookPos = new Position(row, 0);
        ChessPiece queensideRook = board.getPiece(queensideRookPos);

        if (queensideRook instanceof Rook && !queensideRook.hasMoved() &&
            board.isEmpty(new Position(row, 1)) && board.isEmpty(new Position(row, 2)) && 
            board.isEmpty(new Position(row, 3))) {

            Position kingTarget = new Position(row, 2);
            moves.add(new Move(position, kingTarget, this, null, Move.MoveType.CASTLE_QUEENSIDE, null));
        }
    }

    @Override
    public boolean isValidMove(Position target, Board board) {
        int rowDiff = Math.abs(target.getRow() - position.getRow());
        int colDiff = Math.abs(target.getCol() - position.getCol());

        // Regular king move (one square in any direction)
        if (rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff > 0)) {
            ChessPiece targetPiece = board.getPiece(target);
            return targetPiece == null || targetPiece.getColor() != this.color;
        }

        // Castling moves (handled by game state for full validation)
        if (!hasMoved && rowDiff == 0 && Math.abs(colDiff) == 2) {
            return true; // Basic validation - full validation in GameState
        }

        return false;
    }

    @Override
    public ChessPiece copy() {
        King copy = new King(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}