package com.himelz.nexusboard.model.pieces;

import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a pawn chess piece.
 * Handles pawn-specific movement rules including initial two-square move,
 * diagonal captures, en passant, and promotion.
 */
public class Pawn extends ChessPiece {

    public Pawn(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'P';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♙" : "♟";
    }

    @Override
    public int getValue() {
        return 100; // Standard pawn value in centipawns
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();
        int direction = color == Color.WHITE ? -1 : 1; // White moves up (-1), Black moves down (+1)

        // Forward moves
        Position oneSquareForward = position.add(direction, 0);
        if (isValidPosition(oneSquareForward) && board.isEmpty(oneSquareForward)) {
            // Check for promotion
            if (isPromotionSquare(oneSquareForward)) {
                addPromotionMoves(moves, position, oneSquareForward);
            } else {
                moves.add(new Move(position, oneSquareForward, this));
            }

            // Two squares forward from starting position
            if (!hasMoved) {
                Position twoSquaresForward = position.add(2 * direction, 0);
                if (isValidPosition(twoSquaresForward) && board.isEmpty(twoSquaresForward)) {
                    moves.add(new Move(position, twoSquaresForward, this));
                }
            }
        }

        // Diagonal captures
        Position[] capturePositions = {
            position.add(direction, -1), // Left diagonal
            position.add(direction, 1)   // Right diagonal
        };

        for (Position capturePos : capturePositions) {
            if (isValidPosition(capturePos)) {
                ChessPiece targetPiece = board.getPiece(capturePos);
                if (targetPiece != null && targetPiece.getColor() != this.color) {
                    // Regular capture
                    if (isPromotionSquare(capturePos)) {
                        addPromotionMoves(moves, position, capturePos, targetPiece);
                    } else {
                        moves.add(new Move(position, capturePos, this, targetPiece));
                    }
                }
                // TODO: Add en passant logic here when GameState class is implemented
            }
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Position target, Board board) {
        int rowDiff = target.getRow() - position.getRow();
        int colDiff = Math.abs(target.getCol() - position.getCol());
        int direction = color == Color.WHITE ? -1 : 1;

        // Forward moves
        if (colDiff == 0) {
            if (rowDiff == direction && board.isEmpty(target)) {
                return true;
            }
            if (rowDiff == 2 * direction && !hasMoved && board.isEmpty(target) && 
                board.isEmpty(position.add(direction, 0))) {
                return true;
            }
        }

        // Diagonal captures
        if (colDiff == 1 && rowDiff == direction) {
            ChessPiece targetPiece = board.getPiece(target);
            return targetPiece != null && targetPiece.getColor() != this.color;
        }

        return false;
    }

    private boolean isPromotionSquare(Position pos) {
        return (color == Color.WHITE && pos.getRow() == 0) || 
               (color == Color.BLACK && pos.getRow() == 7);
    }

    private void addPromotionMoves(List<Move> moves, Position from, Position to) {
        addPromotionMoves(moves, from, to, null);
    }

    private void addPromotionMoves(List<Move> moves, Position from, Position to, ChessPiece captured) {
        // Add promotion options: Queen, Rook, Bishop, Knight
        moves.add(new Move(from, to, this, captured, Move.MoveType.PROMOTION, new Queen(color, to)));
        moves.add(new Move(from, to, this, captured, Move.MoveType.PROMOTION, new Rook(color, to)));
        moves.add(new Move(from, to, this, captured, Move.MoveType.PROMOTION, new Bishop(color, to)));
        moves.add(new Move(from, to, this, captured, Move.MoveType.PROMOTION, new Knight(color, to)));
    }

    @Override
    public ChessPiece copy() {
        Pawn copy = new Pawn(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}
