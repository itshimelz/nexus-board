package com.himelz.nexusboard.viewmodel;

import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.Color;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class HomeScreenViewModel {
    
    private GameState gameState;
    
    // Observable properties for UI binding
    private final StringProperty gameStatusText;
    private final StringProperty currentPlayerText;
    private final StringProperty statusMessage;
    private final BooleanProperty isGameActive;
    private final BooleanProperty canUndo;
    private final BooleanProperty canResign;
    private final ObservableList<String> moveHistory;
    
    public HomeScreenViewModel() {
        // Initialize game state
        this.gameState = new GameState();
        
        // Initialize observable properties
        this.gameStatusText = new SimpleStringProperty();
        this.currentPlayerText = new SimpleStringProperty();
        this.statusMessage = new SimpleStringProperty("Ready to play");
        this.isGameActive = new SimpleBooleanProperty(true);
        this.canUndo = new SimpleBooleanProperty(false);
        this.canResign = new SimpleBooleanProperty(true);
        this.moveHistory = FXCollections.observableArrayList();
        
        // Initialize UI state
        updateGameStatus();
    }
    
    // Property getters for UI binding
    public StringProperty gameStatusTextProperty() {
        return gameStatusText;
    }
    
    public StringProperty currentPlayerTextProperty() {
        return currentPlayerText;
    }
    
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
    
    public BooleanProperty isGameActiveProperty() {
        return isGameActive;
    }
    
    public BooleanProperty canUndoProperty() {
        return canUndo;
    }
    
    public BooleanProperty canResignProperty() {
        return canResign;
    }
    
    public ObservableList<String> getMoveHistory() {
        return moveHistory;
    }
    
    // Getters
    public GameState getGameState() {
        return gameState;
    }
    
    public String getGameStatusText() {
        return gameStatusText.get();
    }
    
    public String getCurrentPlayerText() {
        return currentPlayerText.get();
    }
    
    public String getStatusMessage() {
        return statusMessage.get();
    }
    
    public boolean isGameActive() {
        return isGameActive.get();
    }
    
    public boolean canUndo() {
        return canUndo.get();
    }
    
    public boolean canResign() {
        return canResign.get();
    }
    
    // Command methods
    
    /**
     * Start a new game
     */
    public void startNewGame() {
        gameState = new GameState();
        moveHistory.clear();
        updateGameStatus();
        statusMessage.set("New game started");
    }
    
    /**
     * Handle game resignation
     */
    public void resignGame() {
        // Determine winner (opposite of current player)
        Color winner = gameState.getCurrentPlayer().opposite();
        statusMessage.set(winner + " wins by resignation");
        isGameActive.set(false);
        canResign.set(false);
    }
    
    /**
     * Undo last move (placeholder for future implementation)
     */
    public void undoLastMove() {
        // TODO: Implement undo functionality
        statusMessage.set("Undo functionality not yet implemented");
    }
    
    /**
     * Update game status display
     */
    public void updateGameStatus() {
        Color currentPlayer = gameState.getCurrentPlayer();
        GameState.GameStatus status = gameState.getGameStatus();
        
        // Update current player
        currentPlayerText.set(currentPlayer == Color.WHITE ? "White to move" : "Black to move");
        
        // Update game status
        switch (status) {
            case ACTIVE:
                gameStatusText.set("Game in progress");
                isGameActive.set(true);
                break;
            case CHECK:
                gameStatusText.set("Check!");
                isGameActive.set(true);
                break;
            case CHECKMATE:
                Color winner = gameState.getWinner();
                gameStatusText.set(winner + " wins by checkmate!");
                isGameActive.set(false);
                canResign.set(false);
                break;
            case STALEMATE:
                gameStatusText.set("Game is a stalemate - Draw!");
                isGameActive.set(false);
                canResign.set(false);
                break;
            case DRAW:
                gameStatusText.set("Game is a draw");
                isGameActive.set(false);
                canResign.set(false);
                break;
            default:
                gameStatusText.set("Unknown status");
                break;
        }
        
        // Update undo availability
        canUndo.set(!gameState.getMoveHistory().isEmpty() && isGameActive.get());
    }
    
    /**
     * Add move to history display
     */
    public void addMoveToHistory(String moveNotation) {
        moveHistory.add(moveNotation);
    }
    
    /**
     * Set status message
     */
    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }
}
