package com.himelz.nexusboard.model.board;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.pieces.*;


 // Represents the chess board with an 8x8 grid of squares.
 // Manages piece placement and provides board state queries.

public class Board {
    public static final int BOARD_SIZE = 8;
    private ChessPiece[][] board;

    private Board(boolean initialize) {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        if (initialize) {
            initializeBoard();
        }
    }

    public Board() {
        this(true);
    }


    // Initialize the board with starting positions for all pieces
    private void initializeBoard() {
        // Place pawns
        for (int col = 0; col < BOARD_SIZE; col++) {
            board[1][col] = new Pawn(Color.BLACK, new Position(1, col));
            board[6][col] = new Pawn(Color.WHITE, new Position(6, col));
        }

        // Place black pieces
        board[0][0] = new Rook(Color.BLACK, new Position(0, 0));
        board[0][1] = new Knight(Color.BLACK, new Position(0, 1));
        board[0][2] = new Bishop(Color.BLACK, new Position(0, 2));
        board[0][3] = new Queen(Color.BLACK, new Position(0, 3));
        board[0][4] = new King(Color.BLACK, new Position(0, 4));
        board[0][5] = new Bishop(Color.BLACK, new Position(0, 5));
        board[0][6] = new Knight(Color.BLACK, new Position(0, 6));
        board[0][7] = new Rook(Color.BLACK, new Position(0, 7));

        // Place white pieces
        board[7][0] = new Rook(Color.WHITE, new Position(7, 0));
        board[7][1] = new Knight(Color.WHITE, new Position(7, 1));
        board[7][2] = new Bishop(Color.WHITE, new Position(7, 2));
        board[7][3] = new Queen(Color.WHITE, new Position(7, 3));
        board[7][4] = new King(Color.WHITE, new Position(7, 4));
        board[7][5] = new Bishop(Color.WHITE, new Position(7, 5));
        board[7][6] = new Knight(Color.WHITE, new Position(7, 6));
        board[7][7] = new Rook(Color.WHITE, new Position(7, 7));
    }

     // Get the piece at the specified position

    public ChessPiece getPiece(Position position) {
        if (!isValidPosition(position)) {
            return null;
        }
        return board[position.getRow()][position.getCol()];
    }

     // Set a piece at the specified position

    public void setPiece(Position position, ChessPiece piece) {
        if (isValidPosition(position)) {
            board[position.getRow()][position.getCol()] = piece;
            if (piece != null) {
                piece.setPosition(position);
            }
        }
    }


    // Remove piece from the specified position
    public ChessPiece removePiece(Position position) {
        ChessPiece piece = getPiece(position);
        setPiece(position, null);
        return piece;
    }

    // Check if a position is valid (within board bounds)

    public boolean isValidPosition(Position position) {
        return position.getRow() >= 0 && position.getRow() < BOARD_SIZE &&
               position.getCol() >= 0 && position.getCol() < BOARD_SIZE;
    }


     // Check if a square is empty
    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

     // Check if a square contains an enemy piece

    public boolean hasEnemyPiece(Position position, Color friendlyColor) {
        ChessPiece piece = getPiece(position);
        return piece != null && piece.getColor() != friendlyColor;
    }


     // Check if a square contains a friendly piece

    public boolean hasFriendlyPiece(Position position, Color friendlyColor) {
        ChessPiece piece = getPiece(position);
        return piece != null && piece.getColor() == friendlyColor;
    }


     // Find the king of the specified color

    public Position findKing(Color color) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                ChessPiece piece = board[row][col];
                if (piece instanceof King && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        return null;
    }


     // Create a deep copy of the board

    public Board copy() {
        Board copy = new Board(false);

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                if (this.board[row][col] != null) {
                    copy.board[row][col] = this.board[row][col].copy();
                }
            }
        }
        return copy;
    }
}
