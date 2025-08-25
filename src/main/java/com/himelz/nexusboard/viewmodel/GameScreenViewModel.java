package com.himelz.nexusboard.viewmodel;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel for the Game Screen following MVVM pattern.
 * Handles all game logic, state management, and provides observable properties for UI binding.
 */
public class GameScreenViewModel {
    
    // Game Model
    private GameState gameState;
    
    // Observable Properties for UI Binding
    private final StringProperty gameStatusMessage;
    private final StringProperty currentPlayerTurn;
    private final StringProperty gameModeText;
    private final StringProperty whitePlayerName;
    private final StringProperty blackPlayerName;
    private final StringProperty whiteTimer;
    private final StringProperty blackTimer;
    private final StringProperty evaluationText;
    private final StringProperty bestMoveText;
    private final StringProperty gameProgressText;
    private final StringProperty materialBalanceText;
    private final StringProperty lastMoveText;
    private final StringProperty checkStatusText;
    private final BooleanProperty isGameActive;
    private final BooleanProperty isWhiteTurn;
    private final BooleanProperty canUndo;
    private final BooleanProperty canResign;
    private final BooleanProperty canOfferDraw;
    private final BooleanProperty isCheckStatusVisible;
    
    // Move History
    private final ObservableList<String> moveHistory;
    
    // Current Selection State
    private final ObjectProperty<Position> selectedPosition;
    private final ListProperty<Position> validMoves;
    
    // Board State Observable Property
    private final ObjectProperty<ChessPiece[][]> boardState;
    
    public GameScreenViewModel() {
        this.gameState = new GameState();
        
        // Initialize observable properties
        this.gameStatusMessage = new SimpleStringProperty();
        this.currentPlayerTurn = new SimpleStringProperty();
        this.gameModeText = new SimpleStringProperty("Single Player");
        this.whitePlayerName = new SimpleStringProperty("White Player");
        this.blackPlayerName = new SimpleStringProperty("Black Player");
        this.whiteTimer = new SimpleStringProperty("∞");
        this.blackTimer = new SimpleStringProperty("∞");
        this.evaluationText = new SimpleStringProperty("0.0");
        this.bestMoveText = new SimpleStringProperty("...");
        this.gameProgressText = new SimpleStringProperty("Opening");
        this.materialBalanceText = new SimpleStringProperty("Equal");
        this.lastMoveText = new SimpleStringProperty("--");
        this.checkStatusText = new SimpleStringProperty("Check!");
        this.isGameActive = new SimpleBooleanProperty(true);
        this.isWhiteTurn = new SimpleBooleanProperty(true);
        this.canUndo = new SimpleBooleanProperty(false);
        this.canResign = new SimpleBooleanProperty(true);
        this.canOfferDraw = new SimpleBooleanProperty(true);
        this.isCheckStatusVisible = new SimpleBooleanProperty(false);
        
        // Initialize collections
        this.moveHistory = FXCollections.observableArrayList();
        this.selectedPosition = new SimpleObjectProperty<>();
        this.validMoves = new SimpleListProperty<>(FXCollections.observableArrayList());
        
        // Initialize board state property
        this.boardState = new SimpleObjectProperty<>();
        
        // Initialize game state
        initializeGame();
    }
    
    /**
     * Initialize the game to starting position
     */
    private void initializeGame() {
        updateGameStatusDisplay();
        updateBoardState();
        clearSelection();
    }
    
    /**
     * Update game status display properties
     */
    private void updateGameStatusDisplay() {
        // Update current player turn
        Color currentPlayer = gameState.getCurrentPlayer();
        currentPlayerTurn.set(currentPlayer == Color.WHITE ? "White to move" : "Black to move");
        isWhiteTurn.set(currentPlayer == Color.WHITE);
        
        // Update game status
        GameState.GameStatus status = gameState.getGameStatus();
        switch (status) {
            case ACTIVE:
                gameStatusMessage.set("Game in progress");
                isGameActive.set(true);
                isCheckStatusVisible.set(false);
                break;
            case CHECK:
                gameStatusMessage.set(currentPlayer + " is in check!");
                checkStatusText.set(currentPlayer + " is in check!");
                isGameActive.set(true);
                isCheckStatusVisible.set(true);
                break;
            case CHECKMATE:
                Color winner = currentPlayer == Color.WHITE ? Color.BLACK : Color.WHITE;
                gameStatusMessage.set("Checkmate! " + winner + " wins!");
                isGameActive.set(false);
                isCheckStatusVisible.set(false);
                break;
            case STALEMATE:
                gameStatusMessage.set("Stalemate! Game is a draw.");
                isGameActive.set(false);
                isCheckStatusVisible.set(false);
                break;
            case DRAW:
                gameStatusMessage.set("Game ended in a draw.");
                isGameActive.set(false);
                isCheckStatusVisible.set(false);
                break;
        }
        
        // Update game progress based on move count
        int moveCount = gameState.getMoveHistory().size();
        if (moveCount < 4) {
            gameProgressText.set("Opening");
        } else if (moveCount < 20) {
            gameProgressText.set("Middlegame");
        } else {
            gameProgressText.set("Endgame");
        }
        
        // Update material balance (simplified)
        materialBalanceText.set("Equal"); // Would be calculated in a full implementation
        
        // Update last move if available
        if (!moveHistory.isEmpty()) {
            lastMoveText.set(moveHistory.get(moveHistory.size() - 1));
        } else {
            lastMoveText.set("--");
        }
        
        // Update button states
        canUndo.set(!gameState.getMoveHistory().isEmpty()); // Simple undo check based on move history
        canResign.set(isGameActive.get());
        canOfferDraw.set(isGameActive.get());
    }
    
    /**
     * Update the board state observable property
     */
    private void updateBoardState() {
        ChessPiece[][] board = new ChessPiece[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                board[row][col] = gameState.getBoard().getPiece(position);
            }
        }
        boardState.set(board);
    }
    
    /**
     * Handle square selection/clicking
     */
    public void handleSquareClick(Position position) {
        if (!isGameActive.get()) {
            return;
        }
        
        Position currentSelection = selectedPosition.get();
        
        // If no square is selected, try to select this square
        if (currentSelection == null) {
            selectSquare(position);
        } 
        // If the same square is clicked, deselect
        else if (currentSelection.equals(position)) {
            clearSelection();
        }
        // If different square is clicked, try to make a move
        else {
            boolean moveMade = attemptMove(currentSelection, position);
            if (!moveMade) {
                // If move failed, try selecting the new square instead
                selectSquare(position);
            }
        }
    }
    
    /**
     * Select a square and show valid moves
     */
    private void selectSquare(Position position) {
        ChessPiece piece = gameState.getBoard().getPiece(position);
        
        // Only allow selection of pieces belonging to current player
        if (piece != null && piece.getColor() == gameState.getCurrentPlayer()) {
            selectedPosition.set(position);
            
            // Get valid moves for this piece from all legal moves
            List<Move> allLegalMoves = gameState.getLegalMoves();
            validMoves.clear();
            for (Move move : allLegalMoves) {
                if (move.getFrom().equals(position)) {
                    validMoves.add(move.getTo());
                }
            }
        }
    }
    
    /**
     * Clear current selection
     */
    private void clearSelection() {
        selectedPosition.set(null);
        validMoves.clear();
    }
    
    /**
     * Attempt to make a move
     */
    private boolean attemptMove(Position from, Position to) {
        // Use GameState's makeMove method which handles validation internally
        boolean moveSuccessful = gameState.makeMove(from, to);
        
        if (moveSuccessful) {
            // Add move to history (simple notation)
            String moveNotation = formatSimpleMoveNotation(from, to);
            moveHistory.add(moveNotation);
            
            // Update last move display
            lastMoveText.set(moveNotation);
            
            // Update displays
            updateGameStatusDisplay();
            updateBoardState();
            clearSelection();
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Format move for display in history (simple notation)
     */
    private String formatSimpleMoveNotation(Position from, Position to) {
        return from.toAlgebraic() + "-" + to.toAlgebraic();
    }
    
    // Game Commands
    
    /**
     * Start a new game
     */
    public void newGame() {
        // Create a new game state (since reset method doesn't exist)
        gameState = new GameState();
        moveHistory.clear();
        clearSelection();
        
        // Reset enhanced status information
        gameProgressText.set("Opening");
        materialBalanceText.set("Equal");
        lastMoveText.set("--");
        isCheckStatusVisible.set(false);
        
        updateGameStatusDisplay();
        updateBoardState();
    }
    
    /**
     * Undo the last move (simplified implementation)
     */
    public void undoMove() {
        // Since GameState doesn't have undo functionality, we'll implement a simple version
        if (!gameState.getMoveHistory().isEmpty()) {
            // For now, just remove last move from display history
            // TODO: Implement proper undo when GameState supports it
            if (!moveHistory.isEmpty()) {
                moveHistory.remove(moveHistory.size() - 1);
            }
            
            // Update last move display
            if (!moveHistory.isEmpty()) {
                lastMoveText.set(moveHistory.get(moveHistory.size() - 1));
            } else {
                lastMoveText.set("--");
            }
            
            clearSelection();
            updateGameStatusDisplay();
        }
    }
    
    /**
     * Resign the current game
     */
    public void resignGame() {
        if (isGameActive.get()) {
            Color winner = gameState.getCurrentPlayer() == Color.WHITE ? Color.BLACK : Color.WHITE;
            gameStatusMessage.set(gameState.getCurrentPlayer() + " resigned. " + winner + " wins!");
            isGameActive.set(false);
            canResign.set(false);
            canOfferDraw.set(false);
        }
    }
    
    /**
     * Offer/Accept a draw
     */
    public void offerDraw() {
        if (isGameActive.get()) {
            gameStatusMessage.set("Game ended in a draw by agreement.");
            isGameActive.set(false);
            canResign.set(false);
            canOfferDraw.set(false);
        }
    }
    
        // Property Getters for UI Binding
    public StringProperty gameStatusMessageProperty() { return gameStatusMessage; }
    public StringProperty currentPlayerTurnProperty() { return currentPlayerTurn; }
    public StringProperty gameModeTextProperty() { return gameModeText; }
    public StringProperty whitePlayerNameProperty() { return whitePlayerName; }
    public StringProperty blackPlayerNameProperty() { return blackPlayerName; }
    public StringProperty whiteTimerProperty() { return whiteTimer; }
    public StringProperty blackTimerProperty() { return blackTimer; }
    public StringProperty evaluationTextProperty() { return evaluationText; }
    public StringProperty bestMoveTextProperty() { return bestMoveText; }
    public StringProperty gameProgressTextProperty() { return gameProgressText; }
    public StringProperty materialBalanceTextProperty() { return materialBalanceText; }
    public StringProperty lastMoveTextProperty() { return lastMoveText; }
    public StringProperty checkStatusTextProperty() { return checkStatusText; }
    public BooleanProperty isGameActiveProperty() { return isGameActive; }
    public BooleanProperty isWhiteTurnProperty() { return isWhiteTurn; }
    public BooleanProperty canUndoProperty() { return canUndo; }
    public BooleanProperty canResignProperty() { return canResign; }
    public BooleanProperty canOfferDrawProperty() { return canOfferDraw; }
    public BooleanProperty isCheckStatusVisibleProperty() { return isCheckStatusVisible; }
    
    public ObservableList<String> getMoveHistory() { return moveHistory; }
    public ObjectProperty<Position> selectedPositionProperty() { return selectedPosition; }
    public ListProperty<Position> validMovesProperty() { return validMoves; }
    public ObjectProperty<ChessPiece[][]> boardStateProperty() { return boardState; }
    
    // Direct accessors for specific use cases
    public ChessPiece getPieceAt(Position position) {
        return gameState.getBoard().getPiece(position);
    }
    
    public boolean isValidMoveTarget(Position position) {
        return validMoves.contains(position);
    }
    
    public boolean isSquareSelected(Position position) {
        Position selected = selectedPosition.get();
        return selected != null && selected.equals(position);
    }
}