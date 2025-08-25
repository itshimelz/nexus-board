package com.himelz.nexusboard.model.pieces;

import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.utils.MoveValidator;
import java.util.List;

/**
 * Abstract base class for all chess pieces.
 * Defines common properties and methods for piece movement and behavior.
 */
public abstract class ChessPiece {
    protected Color color;
    protected Position position;
    protected boolean hasMoved;

    public ChessPiece(Color color, Position position) {
        this.color = color;
        this.position = position;
        this.hasMoved = false;
    }

    // Getters and setters
    public Color getColor() { return color; }
    public Position getPosition() { return position; }
    public boolean hasMoved() { return hasMoved; }

    public void setPosition(Position position) {
        this.position = position;
    }

    public void setMoved(boolean moved) {
        this.hasMoved = moved;
    }

    /**
     * Get the symbol representing this piece (for algebraic notation)
     */
    public abstract char getSymbol();

    /**
     * Get the Unicode symbol for this piece (for display)
     */
    public abstract String getUnicodeSymbol();

    /**
     * Get all possible moves for this piece from its current position
     */
    public abstract List<Move> getPossibleMoves(Board board);

    /**
     * Check if a move to the target position is valid for this piece
     */
    public abstract boolean isValidMove(Position target, Board board);

    /**
     * Get the point value of this piece (for AI evaluation)
     */
    public abstract int getValue();

    /**
     * Create a copy of this piece
     */
    public abstract ChessPiece copy();

    /**
     * Helper method to check if a position is within board bounds
     */
    protected boolean isValidPosition(Position pos) {
        return MoveValidator.isValidPosition(pos);
    }

    /**
     * Helper method to add moves in a direction until blocked
     */
    protected void addMovesInDirection(List<Move> moves, Board board,
                                       int rowDirection, int colDirection) {
        Position current = position.add(rowDirection, colDirection);

        while (isValidPosition(current)) {
            ChessPiece pieceAtTarget = board.getPiece(current);

            if (pieceAtTarget == null) {
                // Empty square - add move
                moves.add(new Move(position, current, this));
            } else if (pieceAtTarget.getColor() != this.color) {
                // Enemy piece - add capture move and stop
                moves.add(new Move(position, current, this, pieceAtTarget));
                break;
            } else {
                // Friendly piece - stop
                break;
            }

            current = current.add(rowDirection, colDirection);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " (" + color + ") at " + position;
    }
}
