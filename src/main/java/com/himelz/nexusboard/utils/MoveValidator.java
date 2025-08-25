package com.himelz.nexusboard.utils;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Board;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;

/**
 * Utility class for validating chess moves according to game rules.
 * Centralizes all move validation logic to reduce code duplication across the application.
 */
public class MoveValidator {
    
    // ============ Position Validation ============
    
    /**
     * Check if a position is within the chess board bounds (0-7)
     * @param position The position to validate
     * @return true if position is valid, false otherwise
     */
    public static boolean isValidPosition(Position position) {
        if (position == null) return false;
        int row = position.getRow();
        int col = position.getCol();
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }
    
    /**
     * Check if both positions are within board bounds
     * @param from Source position
     * @param to Destination position
     * @return true if both positions are valid, false otherwise
     */
    public static boolean areValidPositions(Position from, Position to) {
        return isValidPosition(from) && isValidPosition(to);
    }
    
    // ============ Move Object Validation ============
    
    /**
     * Validate that a Move object has all required data
     * @param move The move to validate
     * @return true if move is properly formed, false otherwise
     */
    public static boolean isValidMoveObject(Move move) {
        return move != null && 
               move.getFrom() != null && 
               move.getTo() != null &&
               areValidPositions(move.getFrom(), move.getTo());
    }
    
    // ============ Piece Ownership Validation ============
    
    /**
     * Check if a player owns the piece at the given position
     * @param board The game board
     * @param position The position to check
     * @param playerColor The color of the player
     * @return true if player owns the piece, false otherwise
     */
    public static boolean playerOwnsPiece(Board board, Position position, Color playerColor) {
        ChessPiece piece = board.getPiece(position);
        return piece != null && piece.getColor() == playerColor;
    }
    
    /**
     * Check if there is a piece at the given position
     * @param board The game board
     * @param position The position to check
     * @return true if a piece exists at the position, false otherwise
     */
    public static boolean hasPieceAt(Board board, Position position) {
        return board.getPiece(position) != null;
    }
    
    // ============ Turn Validation ============
    
    /**
     * Check if it's the specified player's turn
     * @param gameState The current game state
     * @param playerColor The color of the player attempting to move
     * @return true if it's the player's turn, false otherwise
     */
    public static boolean isPlayerTurn(GameState gameState, Color playerColor) {
        return gameState.getCurrentPlayer() == playerColor;
    }
    
    /**
     * Validate turn for network games with player IDs
     * @param gameState The current game state
     * @param playerId The ID of the player attempting to move
     * @param hostPlayerId The host player's ID
     * @param guestPlayerId The guest player's ID
     * @return true if it's the player's turn, false otherwise
     */
    public static boolean isValidNetworkPlayerTurn(GameState gameState, String playerId, 
                                                   String hostPlayerId, String guestPlayerId) {
        if (gameState == null || playerId == null) return false;
        
        boolean isHostTurn = gameState.getCurrentPlayer() == gameState.getHostColor();
        return (isHostTurn && playerId.equals(hostPlayerId)) || 
               (!isHostTurn && playerId.equals(guestPlayerId));
    }
    
    // ============ Game State Validation ============
    
    /**
     * Check if the game is in a state where moves can be made
     * @param gameState The current game state
     * @return true if game is active, false otherwise
     */
    public static boolean isGameActive(GameState gameState) {
        if (gameState == null) return false;
        GameState.GameStatus status = gameState.getGameStatus();
        return status == GameState.GameStatus.ACTIVE || status == GameState.GameStatus.CHECK;
    }
    
    /**
     * Check if a player is valid in a network game
     * @param playerId The player ID to validate
     * @param hostPlayerId The host player's ID
     * @param guestPlayerId The guest player's ID
     * @return true if player is in the game, false otherwise
     */
    public static boolean isValidNetworkPlayer(String playerId, String hostPlayerId, String guestPlayerId) {
        return playerId != null && 
               (playerId.equals(hostPlayerId) || playerId.equals(guestPlayerId));
    }
    
    // ============ Comprehensive Move Validation ============
    
    /**
     * Comprehensive validation for local game moves
     * @param gameState The current game state
     * @param move The move to validate
     * @param playerColor The color of the player attempting the move
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateLocalMove(GameState gameState, Move move, Color playerColor) {
        // Check game state
        if (!isGameActive(gameState)) {
            return new ValidationResult(false, "Game is not active");
        }
        
        // Check move object
        if (!isValidMoveObject(move)) {
            return new ValidationResult(false, "Invalid move data");
        }
        
        // Check turn
        if (!isPlayerTurn(gameState, playerColor)) {
            return new ValidationResult(false, "Not your turn");
        }
        
        // Check piece ownership
        if (!hasPieceAt(gameState.getBoard(), move.getFrom())) {
            return new ValidationResult(false, "No piece at source position");
        }
        
        if (!playerOwnsPiece(gameState.getBoard(), move.getFrom(), playerColor)) {
            return new ValidationResult(false, "Cannot move opponent's piece");
        }
        
        return new ValidationResult(true, "Move validation passed");
    }
    
    /**
     * Comprehensive validation for network game moves
     * @param gameState The current game state
     * @param move The move to validate
     * @param playerId The ID of the player attempting the move
     * @param hostPlayerId The host player's ID
     * @param guestPlayerId The guest player's ID
     * @return ValidationResult with success status and error message
     */
    public static ValidationResult validateNetworkMove(GameState gameState, Move move, String playerId,
                                                       String hostPlayerId, String guestPlayerId) {
        System.out.println("DEBUG: Validating network move - playerId: " + playerId + 
                          ", hostPlayerId: " + hostPlayerId + ", guestPlayerId: " + guestPlayerId);
        
        // Check game state
        if (!isGameActive(gameState)) {
            return new ValidationResult(false, "Game is not active");
        }
        
        // Check player validity
        if (!isValidNetworkPlayer(playerId, hostPlayerId, guestPlayerId)) {
            return new ValidationResult(false, "Player not in this game");
        }
        
        // Check move object
        if (!isValidMoveObject(move)) {
            return new ValidationResult(false, "Invalid move data");
        }
        
        // Check turn
        if (!isValidNetworkPlayerTurn(gameState, playerId, hostPlayerId, guestPlayerId)) {
            boolean isHostTurn = gameState.getCurrentPlayer() == gameState.getHostColor();
            String expectedPlayer = isHostTurn ? "Host" : "Guest";
            return new ValidationResult(false, "Not your turn (Expected: " + expectedPlayer + ")");
        }
        
        // Check piece ownership
        if (!hasPieceAt(gameState.getBoard(), move.getFrom())) {
            return new ValidationResult(false, "No piece at source position");
        }
        
        // Determine player color
        Color playerColor;
        if (playerId.equals(hostPlayerId)) {
            playerColor = gameState.getHostColor(); // Host = White
            System.out.println("DEBUG: Player is host, color: " + playerColor);
        } else if (playerId.equals(guestPlayerId)) {
            playerColor = gameState.getGuestColor(); // Guest = Black
            System.out.println("DEBUG: Player is guest, color: " + playerColor);
        } else {
            return new ValidationResult(false, "Unknown player");
        }

        ChessPiece piece = gameState.getBoard().getPiece(move.getFrom());
        System.out.println("DEBUG: Piece at source position: " + (piece != null ? piece.getClass().getSimpleName() + " (" + piece.getColor() + ")" : "null"));
        System.out.println("DEBUG: Player color: " + playerColor);

        if (!playerOwnsPiece(gameState.getBoard(), move.getFrom(), playerColor)) {
            return new ValidationResult(false, "Cannot move opponent's piece");
        }
        
        System.out.println("DEBUG: Move validation passed");
        return new ValidationResult(true, "Move validation passed");
    }
    
    // ============ Helper Classes ============
    
    /**
     * Result of move validation containing success status and error message
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;
        
        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "ValidationResult{valid=" + valid + ", message='" + message + "'}";
        }
    }
}
