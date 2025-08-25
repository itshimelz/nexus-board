package com.himelz.nexusboard.model;

import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import com.himelz.nexusboard.model.pieces.King;
import com.himelz.nexusboard.model.pieces.Pawn;
import com.himelz.nexusboard.model.pieces.Rook;

import java.util.ArrayList;
import java.util.List;


 //Manages the complete state of a chess game.
 //Handles move validation, check/checkmate detection, and game rules.
public class GameState {
    private Board board;
    private Color currentPlayer;
    private List<Move> moveHistory;
    private GameStatus gameStatus;
    private Position enPassantTarget;

    public GameState() {
        board = new Board();
        currentPlayer = Color.WHITE; // White always starts
        moveHistory = new ArrayList<>();
        gameStatus = GameStatus.ACTIVE;
        enPassantTarget = null;
    }

    // Getters
    public Board getBoard() {
        return board;
    }

    public Color getCurrentPlayer() {
        return currentPlayer;
    }

    public List<Move> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }


    /**
     * Attempt to make a move. Returns true if successful, false if invalid.
     */
    public boolean makeMove(Position from, Position to) {
        ChessPiece piece = board.getPiece(from);

        // Basic validation
        if (piece == null) {
            return false;
        }
        
        if (piece.getColor() != currentPlayer) {
            return false;
        }

        // Find the specific move (including special moves)
        Move move = findValidMove(from, to, piece);
        if (move == null) {
            return false;
        }
        
        // Execute the move
        executeMove(move);

        // Update game state
        updateGameState();
        
        return true;
    }


    /**
     * Find a valid move that matches the from/to positions
     */
    private Move findValidMove(Position from, Position to, ChessPiece piece) {
        List<Move> possibleMoves = getLegalMovesForPiece(piece);

        for (Move move : possibleMoves) {
            if (move.getFrom().equals(from) && move.getTo().equals(to)) {
                return move;
            }
        }

        return null;
    }


    //Execute a move on the board
    private void executeMove(Move move) {
        ChessPiece movingPiece = move.getMovingPiece();

        // Handle special moves
        switch (move.getMoveType()) {
            case CASTLE_KINGSIDE:
                executeCastling(move, true);
                break;
            case CASTLE_QUEENSIDE:
                executeCastling(move, false);
                break;
            case EN_PASSANT:
                executeEnPassant(move);
                break;
            case PROMOTION:
                executePromotion(move);
                break;
            default:
                // Normal move or capture
                board.removePiece(move.getFrom());
                board.setPiece(move.getTo(), movingPiece);
                break;
        }

        // Mark piece as moved
        movingPiece.setMoved(true);

        // Update en passant target
        updateEnPassantTarget(move);

        // Add to move history
        moveHistory.add(move);

        // Switch players
        currentPlayer = currentPlayer.opposite();
    }

    private void executeCastling(Move move, boolean kingside) {
        King king = (King) move.getMovingPiece();
        int row = king.getColor() == Color.WHITE ? 7 : 0;

        // Move king
        board.removePiece(move.getFrom());
        board.setPiece(move.getTo(), king);

        // Move rook
        if (kingside) {
            Rook rook = (Rook) board.removePiece(new Position(row, 7));
            board.setPiece(new Position(row, 5), rook);
        } else {
            Rook rook = (Rook) board.removePiece(new Position(row, 0));
            board.setPiece(new Position(row, 3), rook);
        }
    }

    private void executeEnPassant(Move move) {
        // Move pawn
        board.removePiece(move.getFrom());
        board.setPiece(move.getTo(), move.getMovingPiece());

        // Remove captured pawn
        int capturedPawnRow = move.getFrom().getRow();
        Position capturedPawnPos = new Position(capturedPawnRow, move.getTo().getCol());
        board.removePiece(capturedPawnPos);
    }

    private void executePromotion(Move move) {
        // Remove original pawn
        board.removePiece(move.getFrom());

        // Place promoted piece
        board.setPiece(move.getTo(), move.getPromotionPiece());
    }

    private void updateEnPassantTarget(Move move) {
        enPassantTarget = null; // Clear previous en passant target

        // Set new en passant target if pawn moved two squares
        if (move.getMovingPiece() instanceof Pawn) {
            int rowDiff = Math.abs(move.getTo().getRow() - move.getFrom().getRow());
            if (rowDiff == 2) {
                int targetRow = (move.getFrom().getRow() + move.getTo().getRow()) / 2;
                enPassantTarget = new Position(targetRow, move.getTo().getCol());
            }
        }
    }


    //Get all legal moves for the current player

    public List<Move> getLegalMoves() {
        List<Move> legalMoves = new ArrayList<>();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = board.getPiece(new Position(row, col));
                if (piece != null && piece.getColor() == currentPlayer) {
                    legalMoves.addAll(getLegalMovesForPiece(piece));
                }
            }
        }

        return legalMoves;
    }

    public List<Move> getLegalMovesForPiece(ChessPiece piece) {
        List<Move> possibleMoves = piece.getPossibleMoves(board);
        List<Move> legalMoves = new ArrayList<>();

        // Filter out moves that would leave king in check
        for (Move move : possibleMoves) {
            if (isMoveLegal(move)) {
                legalMoves.add(move);
            }
        }

        return legalMoves;
    }


//     Check if a move is legal (doesn't leave own king in check)

    private boolean isMoveLegal(Move move) {
        // Make a copy of the game state and test the move
        Board boardCopy = board.copy();

        // Simulate the move
        simulateMove(move, boardCopy);

        // Check if king is in check after the move
        Position kingPos = boardCopy.findKing(move.getMovingPiece().getColor());
        return !isPositionUnderAttack(kingPos, move.getMovingPiece().getColor().opposite(), boardCopy);
    }

    /**
     * Simulate a move on a board copy without changing game state
     */
    private void simulateMove(Move move, Board simulationBoard) {
        // Get the piece from the simulation board (not the original piece!)
        ChessPiece movingPiece = simulationBoard.getPiece(move.getFrom());
        
        if (movingPiece == null) {
            System.err.println("ERROR: No piece found at " + move.getFrom() + " during simulation");
            return;
        }
        
        switch (move.getMoveType()) {
            case CASTLE_KINGSIDE:
            case CASTLE_QUEENSIDE:
                // For castling simulation, just move the king
                simulationBoard.removePiece(move.getFrom());
                simulationBoard.setPiece(move.getTo(), movingPiece);
                break;
            default:
                simulationBoard.removePiece(move.getFrom());
                simulationBoard.setPiece(move.getTo(), movingPiece);
                break;
        }
    }

    /**
     * Check if a position is under attack by the specified color
     */
    public boolean isPositionUnderAttack(Position position, Color attackingColor, Board testBoard) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessPiece piece = testBoard.getPiece(new Position(row, col));
                if (piece != null && piece.getColor() == attackingColor) {
                    if (piece.isValidMove(position, testBoard)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the current player's king is in check
     */
    public boolean isInCheck() {
        Position kingPos = board.findKing(currentPlayer);
        return kingPos != null && isPositionUnderAttack(kingPos, currentPlayer.opposite(), board);
    }

    /**
     * Update the game status after a move
     */
    private void updateGameState() {
        List<Move> legalMoves = getLegalMoves();
        boolean inCheck = isInCheck();

        if (legalMoves.isEmpty()) {
            if (inCheck) {
                gameStatus = GameStatus.CHECKMATE;
            } else {
                gameStatus = GameStatus.STALEMATE;
            }
        } else if (inCheck) {
            gameStatus = GameStatus.CHECK;
        } else {
            gameStatus = GameStatus.ACTIVE;
        }
    }

    /**
     * Check if the game is over
     */
    public boolean isGameOver() {
        return gameStatus == GameStatus.CHECKMATE ||
                gameStatus == GameStatus.STALEMATE ||
                gameStatus == GameStatus.DRAW;
    }

    /**
     * Get the winner of the game (null if game not over or draw)
     */
    public Color getWinner() {
        if (gameStatus == GameStatus.CHECKMATE) {
            return currentPlayer.opposite(); // Previous player wins
        }
        return null; // No winner
    }

    // Additional methods for multiplayer networking

    /**
     * Makes a move using a Move object (overloaded method for networking)
     */
    public boolean makeMove(Move move) {
        return makeMove(move.getFrom(), move.getTo());
    }

    /**
     * Sets the host player information
     */
    public void setHostPlayer(String playerId, String playerName) {
        // Store host player info (could be expanded to include in game state)
        // For now, this is a placeholder for networking integration
        System.out.println("Host player set: " + playerName + " (" + playerId + ")");
    }

    /**
     * Sets the guest player information
     */
    public void setGuestPlayer(String playerId, String playerName) {
        // Store guest player info (could be expanded to include in game state)
        // For now, this is a placeholder for networking integration
        System.out.println("Guest player set: " + playerName + " (" + playerId + ")");
    }

    /**
     * Gets the host player's color (white)
     */
    public Color getHostColor() {
        return Color.WHITE; // Host always plays white
    }

    /**
     * Gets the guest player's color (black)
     */
    public Color getGuestColor() {
        return Color.BLACK; // Guest always plays black
    }

    /**
     * Synchronizes the game state from server data (for multiplayer games)
     */
    public void syncFromServer(Color currentPlayer, GameStatus gameStatus) {
        this.currentPlayer = currentPlayer;
        this.gameStatus = gameStatus;
    }

    /**
     * Sets the current player (for server synchronization)
     */
    public void setCurrentPlayer(Color currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    /**
     * Sets the game status (for server synchronization)
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    public enum GameStatus {
        ACTIVE,
        CHECK,
        CHECKMATE,
        STALEMATE,
        DRAW
    }
}
