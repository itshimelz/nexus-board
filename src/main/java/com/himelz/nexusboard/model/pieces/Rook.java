package com.himelz.nexusboard.model.pieces;

import com.himelz.nexusboard.model.*;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rook chess piece.
 * Moves horizontally and vertically any number of squares.
 */
public class Rook extends ChessPiece {

    public Rook(Color color, Position position) {
        super(color, position);
    }

    @Override
    public char getSymbol() {
        return 'R';
    }

    @Override
    public String getUnicodeSymbol() {
        return color == Color.WHITE ? "♖" : "♜";
    }

    @Override
    public int getValue() {
        return 500; // Standard rook value in centipawns
    }

    @Override
    public List<Move> getPossibleMoves(Board board) {
        List<Move> moves = new ArrayList<>();

        // Horizontal and vertical directions
        int[][] directions = {{0, 1}, {0, -1}, {1, 0}, {-1, 0}};

        for (int[] direction : directions) {
            addMovesInDirection(moves, board, direction[0], direction[1]);
        }

        return moves;
    }

    @Override
    public boolean isValidMove(Position target, Board board) {
        // Must move in straight line (horizontal or vertical)
        if (position.getRow() != target.getRow() && position.getCol() != target.getCol()) {
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
        Rook copy = new Rook(color, position);
        copy.hasMoved = this.hasMoved;
        return copy;
    }
}