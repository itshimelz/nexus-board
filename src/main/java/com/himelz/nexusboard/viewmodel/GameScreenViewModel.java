package com.himelz.nexusboard.viewmodel;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.network.Server;
import com.himelz.nexusboard.utils.MoveValidator;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

/**
 * ViewModel for the Game Screen following MVVM pattern.
 * Handles all game logic, state management, and provides observable properties for UI binding.
 * Supports both local and networked multiplayer gameplay.
 */
public class GameScreenViewModel implements Client.ClientListener {
    
    // Game Model
    private GameState gameState;
    
    // Networking Components
    private Client gameClient;
    private Server gameServer;
    private boolean isHost;
    private boolean isNetworkGame;
    private Color localPlayerColor;
    private String localPlayerId;
    
    // Observable Properties for UI Binding
    private final StringProperty gameStatusMessage = new SimpleStringProperty();
    private final StringProperty currentPlayerTurn = new SimpleStringProperty();
    private final StringProperty gameModeText = new SimpleStringProperty();
    private final StringProperty whitePlayerName = new SimpleStringProperty();
    private final StringProperty blackPlayerName = new SimpleStringProperty();
    private final StringProperty whiteTimer = new SimpleStringProperty();
    private final StringProperty blackTimer = new SimpleStringProperty();
    private final StringProperty evaluationText = new SimpleStringProperty();
    private final StringProperty bestMoveText = new SimpleStringProperty();
    private final StringProperty gameProgressText = new SimpleStringProperty();
    private final StringProperty materialBalanceText = new SimpleStringProperty();
    private final StringProperty lastMoveText = new SimpleStringProperty();
    private final StringProperty checkStatusText = new SimpleStringProperty();
    private final BooleanProperty isGameActive = new SimpleBooleanProperty();
    private final BooleanProperty isWhiteTurn = new SimpleBooleanProperty();
    private final BooleanProperty canUndo = new SimpleBooleanProperty();
    private final BooleanProperty canResign = new SimpleBooleanProperty();
    private final BooleanProperty canOfferDraw = new SimpleBooleanProperty();
    private final BooleanProperty isCheckStatusVisible = new SimpleBooleanProperty();
    
    // Move History
    private final ObservableList<String> moveHistory = FXCollections.observableArrayList();
    
    // Current Selection State
    private final ObjectProperty<Position> selectedPosition = new SimpleObjectProperty<>();
    private final ListProperty<Position> validMoves = new SimpleListProperty<>(FXCollections.observableArrayList());
    
    // Board State Observable Property
    private final ObjectProperty<ChessPiece[][]> boardState = new SimpleObjectProperty<>();
    
    public GameScreenViewModel() {
        this.isNetworkGame = false;
        this.isHost = false;
        this.gameState = new GameState();
        initializeProperties();
        initializeGame();
    }
    
    /**
     * Constructor for network games
     * @param client The client connection (for both host and join)
     * @param server The server instance (null if joining a game)
     * @param isHost Whether this player is hosting the game
     * @param localPlayerColor The color assigned to the local player
     * @param localPlayerId The ID of the local player
     */
    public GameScreenViewModel(Client client, Server server, boolean isHost, 
                               Color localPlayerColor, String localPlayerId) {
        this.isNetworkGame = true;
        this.isHost = isHost;
        this.gameClient = client;
        this.gameServer = server;
        this.localPlayerColor = localPlayerColor;
        this.localPlayerId = localPlayerId;
        this.gameState = new GameState();
        
        // Set up client listener for network events
        if (gameClient != null) {
            gameClient.addClientListener(this);
        }
        
        initializeProperties();
        initializeNetworkGame();
    }
    
    /**
     * Initialize all observable properties with default values
     */
    private void initializeProperties() {
        // Set initial values for properties
        gameModeText.set(isNetworkGame ? "Multiplayer" : "Single Player");
        whitePlayerName.set("White Player");
        blackPlayerName.set("Black Player");
        whiteTimer.set("∞");
        blackTimer.set("∞");
        evaluationText.set("0.0");
        bestMoveText.set("...");
        gameProgressText.set("Opening");
        materialBalanceText.set("Equal");
        lastMoveText.set("--");
        checkStatusText.set("Check!");
        isGameActive.set(true);
        isWhiteTurn.set(true);
        canUndo.set(false);
        canResign.set(true);
        canOfferDraw.set(true);
        isCheckStatusVisible.set(false);
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
     * Initialize network game specific setup
     */
    private void initializeNetworkGame() {
        // Set player names based on network role
        if (isHost) {
            if (localPlayerColor == Color.WHITE) {
                whitePlayerName.set("You (Host)");
                blackPlayerName.set("Opponent");
            } else {
                whitePlayerName.set("Opponent");
                blackPlayerName.set("You (Host)");
            }
        } else {
            if (localPlayerColor == Color.WHITE) {
                whitePlayerName.set("You");
                blackPlayerName.set("Host");
            } else {
                whitePlayerName.set("Host");
                blackPlayerName.set("You");
            }
        }
        
        // Start the game
        initializeGame();
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
        System.out.println("DEBUG: Attempting move from " + from + " to " + to);
        System.out.println("DEBUG: Current player: " + gameState.getCurrentPlayer() + ", Local player color: " + localPlayerColor);
        
        // Use MoveValidator for pre-validation
        if (isNetworkGame) {
            // For network games, only validate if it's our turn
            if (!MoveValidator.isPlayerTurn(gameState, localPlayerColor)) {
                System.out.println("Not our turn - current player: " + gameState.getCurrentPlayer() + 
                                 ", local player: " + localPlayerColor);
                return false; // Not our turn
            } else {
                System.out.println("DEBUG: It is our turn, proceeding with move");
            }
        }
        
        // Basic position validation
        if (!MoveValidator.areValidPositions(from, to)) {
            System.out.println("DEBUG: Invalid positions");
            return false;
        }
        
        // Use GameState's makeMove method which handles full chess rule validation
        boolean moveSuccessful = gameState.makeMove(from, to);
        System.out.println("DEBUG: Local move validation result: " + moveSuccessful);
        
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
            
            // Send move over network if this is a network game
            if (isNetworkGame && gameClient != null) {
                ChessPiece finalPiece = gameState.getBoard().getPiece(to); // Piece is now at destination
                Move networkMove = new Move(from, to, finalPiece);
                System.out.println("DEBUG: Sending move to server: " + networkMove);
                gameClient.sendMove(networkMove);
                System.out.println("Sent move to server: " + from.toAlgebraic() + " -> " + to.toAlgebraic());
            }
            
            return true;
        }
        
        return false;
    }

    /**
     * Process a move received from the network (for opponent moves)
     */
    private void processNetworkMove(Move move) {
        Platform.runLater(() -> {
            // Apply the move to our local game state
            boolean moveSuccessful = gameState.makeMove(move);
            
            if (moveSuccessful) {
                // Add move to history
                String moveNotation = formatSimpleMoveNotation(move.getFrom(), move.getTo());
                moveHistory.add(moveNotation);
                
                // Update last move display
                lastMoveText.set(moveNotation);
                
                // Update displays
                updateGameStatusDisplay();
                updateBoardState();
                clearSelection();
                
                System.out.println("Applied network move: " + moveNotation);
            } else {
                System.err.println("Failed to apply network move: " + 
                    move.getFrom().toAlgebraic() + " -> " + move.getTo().toAlgebraic());
            }
        });
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
    public Color getLocalPlayerColor() { return localPlayerColor; }
    public boolean isNetworkGame() { return gameClient != null || gameServer != null; }
    
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

    // ============ Network Game Methods ============
    
    /**
     * Check if the local player can make moves (their turn)
     */
    public boolean canMakeMove() {
        if (!isNetworkGame) {
            return true; // In local games, both players can move
        }
        return MoveValidator.isPlayerTurn(gameState, localPlayerColor);
    }
    
    /**
     * Get information about the network game setup
     */
    public String getNetworkGameInfo() {
        if (!isNetworkGame) {
            return "Local Game";
        }
        
        String role = isHost ? "Host" : "Client";
        String color = localPlayerColor == Color.WHITE ? "White" : "Black";
        return String.format("%s playing as %s", role, color);
    }

    // ============ ClientListener Implementation ============
    
    @Override
    public void onConnected() {
        Platform.runLater(() -> {
            gameStatusMessage.set("Connected to game server");
            System.out.println("Game screen: Connected to server");
        });
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            gameStatusMessage.set("Disconnected from game server");
            isGameActive.set(false);
            System.out.println("Game screen: Disconnected from server");
        });
    }

    @Override
    public void onJoinedGame(String gameId, String playerId) {
        Platform.runLater(() -> {
            gameStatusMessage.set("Joined game: " + gameId);
            System.out.println("Game screen: Joined game " + gameId + " as " + playerId);
        });
    }

    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        Platform.runLater(() -> {
            gameStatusMessage.set("Player joined: " + playerName);
            System.out.println("Game screen: Player joined - " + playerName + " (" + playerId + ")");
        });
    }

    @Override
    public void onPlayerLeft(String playerId) {
        Platform.runLater(() -> {
            gameStatusMessage.set("Player left the game");
            isGameActive.set(false);
            System.out.println("Game screen: Player left - " + playerId);
        });
    }

    @Override
    public void onGameStarted() {
        Platform.runLater(() -> {
            gameStatusMessage.set("Game started! " + getNetworkGameInfo());
            isGameActive.set(true);
            System.out.println("Game screen: Game started");
        });
    }

    @Override
    public void onGameStateUpdated(GameState gameState) {
        Platform.runLater(() -> {
            // Update our local game state with the server's authoritative state
            this.gameState = gameState;
            updateGameStatusDisplay();
            updateBoardState();
            clearSelection(); // Clear any selections when game state updates
            System.out.println("Game screen: Game state updated from server");
        });
    }

    @Override
    public void onMoveReceived(String playerId, Move move) {
        // Only process moves from other players (not our own moves echoed back)
        if (!playerId.equals(localPlayerId)) {
            processNetworkMove(move);
            System.out.println("Game screen: Move received from " + playerId);
        } else {
            // For our own moves, just update the UI to reflect the new turn
            Platform.runLater(() -> {
                updateGameStatusDisplay();
                clearSelection(); // Clear selection after our move
                System.out.println("Game screen: Own move confirmed by server");
            });
        }
    }

    @Override
    public void onChatReceived(String playerId, String playerName, String message) {
        Platform.runLater(() -> {
            // TODO: Display chat message in game UI
            System.out.println("Game screen: Chat from " + playerName + ": " + message);
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            gameStatusMessage.set("Network error: " + error);
            System.err.println("Game screen: Network error - " + error);
        });
    }

    @Override
    public void onMessage(String message) {
        Platform.runLater(() -> {
            gameStatusMessage.set(message);
            System.out.println("Game screen: Server message - " + message);
        });
    }
}