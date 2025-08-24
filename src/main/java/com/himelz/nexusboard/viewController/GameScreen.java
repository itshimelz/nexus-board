package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Game Screen Controller for the chess game UI.
 * Handles only UI presentation without game logic.
 */
public class GameScreen implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    
    // Player Information
    @FXML private Label blackPlayerName;
    @FXML private Label blackTimer;
    @FXML private Label blackStatus;
    @FXML private Label whitePlayerName;
    @FXML private Label whiteTimer;
    @FXML private Label whiteStatus;
    
    // Chess Board
    @FXML private GridPane chessBoard;
    
    // Game Status
    @FXML private Label gameStatusLabel;
    @FXML private Label turnLabel;
    @FXML private Label gameModeLabel;
    @FXML private Label evaluationLabel;
    @FXML private Label bestMoveLabel;
    
    // Controls
    @FXML private Button newGameButton;
    @FXML private Button undoButton;
    @FXML private Button resignButton;
    @FXML private Button drawButton;
    
    // Move History
    @FXML private ListView<String> moveHistoryList;
    
    // Captured Pieces
    @FXML private FlowPane capturedWhitePieces;
    @FXML private FlowPane capturedBlackPieces;
    
    // Status
    @FXML private Label statusLabel;
    @FXML private Label connectionStatus;
    
    // Chess piece images
    private Map<String, Image> pieceImages;
    
    // Game State
    private GameState gameState;
    
    // Move selection state
    private Position selectedSquare;
    private StackPane selectedSquarePane;
    
    // Chess piece Unicode symbols (kept for fallback)
    private static final String[] WHITE_PIECES = {"♔", "♕", "♖", "♗", "♘", "♙"};
    private static final String[] BLACK_PIECES = {"♚", "♛", "♜", "♝", "♞", "♟"};
    
    public GameScreen(Stage stage) {
        this.primaryStage = stage;
        this.gameState = new GameState(); // Initialize game with starting position
    }
    
    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/Game.fxml"));
            loader.setController(this);
            
            // Create scene
            scene = new Scene(loader.load(), 1400, 900);
            
            // Add CSS stylesheet
            String cssPath = getClass().getResource("/com/himelz/nexusboard/nexusboard/styles/game.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            // Configure stage
            primaryStage.setTitle("Nexus Board - Chess Game");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            createFallbackScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load chess piece images
        loadPieceImages();
        
        // Initialize the chess board
        initializeChessBoard();
        
        // Set up initial game state display
        setupInitialGameState();
        
        // Configure UI components
        configureUIComponents();
        
        // Set up event handlers
        setupEventHandlers();
    }
    
    /**
     * Load all chess piece images from resources
     */
    private void loadPieceImages() {
        pieceImages = new HashMap<>();
        
        try {
            // Load white pieces
            pieceImages.put("wk", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-king-36.png")));
            pieceImages.put("wq", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-qween-36.png")));
            pieceImages.put("wr", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-rock-36.png")));
            pieceImages.put("wb", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-bishop-36.png")));
            pieceImages.put("wn", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-knight-36.png")));
            pieceImages.put("wp", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/white-pawn-36.png")));
            
            // Load black pieces
            pieceImages.put("bk", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-king-36.png")));
            pieceImages.put("bq", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-qween-36.png")));
            pieceImages.put("br", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-rock-36.png")));
            pieceImages.put("bb", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-bishop-36.png")));
            pieceImages.put("bn", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-knight-36.png")));
            pieceImages.put("bp", new Image(getClass().getResourceAsStream("/com/himelz/nexusboard/nexusboard/images/black-pawn-36.png")));
            
            System.out.println("Successfully loaded " + pieceImages.size() + " chess piece images");
        } catch (Exception e) {
            System.err.println("Error loading chess piece images: " + e.getMessage());
            e.printStackTrace();
            // Fallback to Unicode symbols if images fail to load
            pieceImages = null;
        }
    }
    
    /**
     * Initialize the 8x8 chess board with current game state
     */
    private void initializeChessBoard() {
        // Clear any existing content
        chessBoard.getChildren().clear();
        
        // Create 8x8 grid of squares and populate with pieces from game state
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createChessSquare(row, col);
                chessBoard.add(square, col, row);
            }
        }
    }
    
    /**
     * Create a chess square with appropriate styling and piece from game state
     */
    private StackPane createChessSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(70, 70);
        square.setMinSize(70, 70);
        square.setMaxSize(70, 70);
        
        // Determine square color (chess board pattern)
        boolean isLightSquare = (row + col) % 2 == 0;
        if (isLightSquare) {
            square.getStyleClass().add("light-square");
        } else {
            square.getStyleClass().add("dark-square");
        }
        
        // Get piece from game state and add to square
        Position position = new Position(row, col);
        ChessPiece piece = gameState.getBoard().getPiece(position);
        
        if (piece != null) {
            addPieceToSquare(square, piece);
        }
        
        // Add hover effect and click handler

        square.setOnMouseEntered(e -> {
            if (!square.getStyleClass().contains("selected-square")) {
                square.getStyleClass().add("highlighted-square");
            }
        });
        
        square.setOnMouseExited(e -> {
            square.getStyleClass().remove("highlighted-square");
        });
        
        square.setOnMouseClicked(e -> handleSquareClick(row, col, square));
        
        return square;
    }
    
    /**
     * Add a chess piece to a square based on the piece object
     */
    private void addPieceToSquare(StackPane square, ChessPiece piece) {
        String pieceCode = getPieceCode(piece);
        
        if (pieceImages != null && pieceImages.containsKey(pieceCode)) {
            // Use piece images
            ImageView pieceImageView = new ImageView(pieceImages.get(pieceCode));
            pieceImageView.setFitWidth(50);
            pieceImageView.setFitHeight(50);
            pieceImageView.setPreserveRatio(true);
            pieceImageView.setSmooth(true);
            pieceImageView.getStyleClass().add("chess-piece-image");
            
            // Add hover effect
            pieceImageView.setOnMouseEntered(e -> {
                pieceImageView.setScaleX(1.1);
                pieceImageView.setScaleY(1.1);
            });
            
            pieceImageView.setOnMouseExited(e -> {
                pieceImageView.setScaleX(1.0);
                pieceImageView.setScaleY(1.0);
            });
            
            square.getChildren().add(pieceImageView);
        } else {
            // Fallback to Unicode symbols
            String unicodePiece = piece.getUnicodeSymbol();
            Label pieceLabel = new Label(unicodePiece);
            pieceLabel.getStyleClass().add("chess-piece");
            
            // Style white and black pieces differently
            if (piece.getColor() == com.himelz.nexusboard.model.Color.WHITE) {
                pieceLabel.getStyleClass().addAll("white-piece", "piece-shadow");
            } else {
                pieceLabel.getStyleClass().addAll("black-piece", "piece-shadow");
            }
            
            square.getChildren().add(pieceLabel);
        }
    }
    
    /**
     * Get piece code for image lookup based on piece type and color
     */
    private String getPieceCode(ChessPiece piece) {
        String colorPrefix = (piece.getColor() == com.himelz.nexusboard.model.Color.WHITE) ? "w" : "b";
        String pieceType;
        
        switch (piece.getClass().getSimpleName().toLowerCase()) {
            case "king": pieceType = "k"; break;
            case "queen": pieceType = "q"; break;
            case "rook": pieceType = "r"; break;
            case "bishop": pieceType = "b"; break;
            case "knight": pieceType = "n"; break;
            case "pawn": pieceType = "p"; break;
            default: pieceType = "p"; break;
        }
        
        return colorPrefix + pieceType;
    }
    
    /**
     * Get Unicode symbol for piece code (fallback)
     */
    private String getUnicodePiece(String pieceCode) {
        switch (pieceCode) {
            case "wk": return "♔";
            case "wq": return "♕";
            case "wr": return "♖";
            case "wb": return "♗";
            case "wn": return "♘";
            case "wp": return "♙";
            case "bk": return "♚";
            case "bq": return "♛";
            case "br": return "♜";
            case "bb": return "♝";
            case "bn": return "♞";
            case "bp": return "♟";
            default: return "";
        }
    }

    
    /**
     * Handle chess square click - implements piece selection and move logic
     */
    private void handleSquareClick(int row, int col, StackPane square) {
        Position clickedPosition = new Position(row, col);
        ChessPiece clickedPiece = gameState.getBoard().getPiece(clickedPosition);
        
        if (selectedSquare == null) {
            // No piece selected - try to select a piece
            if (clickedPiece != null && clickedPiece.getColor() == gameState.getCurrentPlayer()) {
                selectSquare(clickedPosition, square);
                if (statusLabel != null) {
                    char file = (char)('a' + col);
                    int rank = 8 - row;
                    statusLabel.setText("Selected: " + clickedPiece.getClass().getSimpleName() + " at " + file + rank);
                }
            } else {
                if (statusLabel != null) {
                    statusLabel.setText("Select a " + gameState.getCurrentPlayer().toString().toLowerCase() + " piece to move");
                }
            }
        } else {
            // Piece already selected
            if (clickedPosition.equals(selectedSquare)) {
                // Clicked same square - deselect
                clearSelection();
                if (statusLabel != null) statusLabel.setText("Selection cleared");
            } else if (clickedPiece != null && clickedPiece.getColor() == gameState.getCurrentPlayer()) {
                // Clicked another own piece - select it instead
                clearSelection();
                selectSquare(clickedPosition, square);
                if (statusLabel != null) {
                    char file = (char)('a' + col);
                    int rank = 8 - row;
                    statusLabel.setText("Selected: " + clickedPiece.getClass().getSimpleName() + " at " + file + rank);
                }
            } else {
                // Try to make a move
                attemptMove(selectedSquare, clickedPosition);
            }
        }
    }
    
    /**
     * Select a square and highlight it
     */
    private void selectSquare(Position position, StackPane square) {
        selectedSquare = position;
        selectedSquarePane = square;
        square.getStyleClass().add("selected-square");
        
        // Highlight possible moves
        highlightPossibleMoves(position);
    }
    
    /**
     * Clear current selection and highlights
     */
    private void clearSelection() {
        if (selectedSquarePane != null) {
            selectedSquarePane.getStyleClass().remove("selected-square");
        }
        selectedSquare = null;
        selectedSquarePane = null;
        
        // Clear all move highlights
        clearMoveHighlights();
    }
    
    /**
     * Highlight possible moves for the selected piece
     */
    private void highlightPossibleMoves(Position position) {
        ChessPiece piece = gameState.getBoard().getPiece(position);
        if (piece == null) return;
        
        try {
            // Get legal moves from GameState (this ensures they are properly validated)
            var legalMoves = gameState.getLegalMovesForPiece(piece);
            
            for (var move : legalMoves) {
                if (move.getFrom().equals(position)) {
                    Position to = move.getTo();
                    StackPane targetSquare = getSquareAt(to.getRow(), to.getCol());
                    if (targetSquare != null) {
                        targetSquare.getStyleClass().add("possible-move");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error highlighting moves: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Clear all move highlights
     */
    private void clearMoveHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = getSquareAt(row, col);
                if (square != null) {
                    square.getStyleClass().remove("possible-move");
                }
            }
        }
    }
    
    /**
     * Get square at specific position
     */
    private StackPane getSquareAt(int row, int col) {
        for (var node : chessBoard.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                    return (StackPane) node;
                }
            }
        }
        return null;
    }
    
    /**
     * Attempt to make a move from selected square to target square
     */
    private void attemptMove(Position from, Position to) {
        boolean moveSuccessful = gameState.makeMove(from, to);
        
        if (moveSuccessful) {
            // Move was successful
            handleSuccessfulMove(from, to);
        } else {
            // Invalid move
            handleInvalidMove(from, to);
        }
        
        clearSelection();
    }
    

    
    /**
     * Handle successful move
     */
    private void handleSuccessfulMove(Position from, Position to) {
        // Refresh the board display
        refreshBoard();
        
        // Add move to history
        String moveNotation = createMoveNotation(from, to);
        addMoveToHistory(moveNotation);
        
        // Update status
        if (statusLabel != null) {
            char fromFile = (char)('a' + from.getCol());
            int fromRank = 8 - from.getRow();
            char toFile = (char)('a' + to.getCol());
            int toRank = 8 - to.getRow();
            statusLabel.setText("Move: " + fromFile + fromRank + " to " + toFile + toRank);
        }
        
        // Check for game end conditions
        checkGameEndConditions();
    }
    
    /**
     * Handle invalid move
     */
    private void handleInvalidMove(Position from, Position to) {
        if (statusLabel != null) {
            char fromFile = (char)('a' + from.getCol());
            int fromRank = 8 - from.getRow();
            char toFile = (char)('a' + to.getCol());
            int toRank = 8 - to.getRow();
            statusLabel.setText("Invalid move from " + fromFile + fromRank + " to " + toFile + toRank);
        }
    }
    
    /**
     * Create move notation for display
     */
    private String createMoveNotation(Position from, Position to) {
        int moveNumber = gameState.getMoveHistory().size();
        boolean isWhiteMove = (moveNumber % 2) == 0;
        
        char fromFile = (char)('a' + from.getCol());
        int fromRank = 8 - from.getRow();
        char toFile = (char)('a' + to.getCol());
        int toRank = 8 - to.getRow();
        
        String move = fromFile + Integer.toString(fromRank) + "-" + toFile + Integer.toString(toRank);
        
        if (isWhiteMove) {
            return (moveNumber/2 + 1) + ". " + move;
        } else {
            return move;
        }
    }
    
    /**
     * Check for game end conditions and update UI accordingly
     */
    private void checkGameEndConditions() {
        var gameStatus = gameState.getGameStatus();
        
        switch (gameStatus) {
            case CHECKMATE:
                var winner = gameState.getCurrentPlayer().opposite(); // Winner is opposite of current player
                if (statusLabel != null) statusLabel.setText("Checkmate! " + winner + " wins!");
                if (gameStatusLabel != null) gameStatusLabel.setText("Game Over - " + winner + " wins");
                disableGameControls();
                break;
            case STALEMATE:
                if (statusLabel != null) statusLabel.setText("Stalemate! Game is a draw.");
                if (gameStatusLabel != null) gameStatusLabel.setText("Game Over - Stalemate");
                disableGameControls();
                break;
            case CHECK:
                if (statusLabel != null) statusLabel.setText(gameState.getCurrentPlayer() + " is in check!");
                break;
            case DRAW:
                if (statusLabel != null) statusLabel.setText("Game is a draw.");
                if (gameStatusLabel != null) gameStatusLabel.setText("Game Over - Draw");
                disableGameControls();
                break;
            default:
                // Game continues - update status
                updateGameStatusDisplay();
                break;
        }
    }
    
    /**
     * Disable game controls when game is over
     */
    private void disableGameControls() {
        if (undoButton != null) undoButton.setDisable(true);
        if (resignButton != null) resignButton.setDisable(true);
        if (drawButton != null) drawButton.setDisable(true);
    }
    
    /**
     * Enable game controls for active game
     */
    private void enableGameControls() {
        if (undoButton != null) undoButton.setDisable(false);
        if (resignButton != null) resignButton.setDisable(false);
        if (drawButton != null) drawButton.setDisable(false);
    }
    
    /**
     * Create an ImageView for a chess piece
     */
    private ImageView createPieceImageView(String pieceCode, double size) {
        if (pieceImages != null && pieceImages.containsKey(pieceCode)) {
            ImageView imageView = new ImageView(pieceImages.get(pieceCode));
            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            return imageView;
        }
        return null;
    }
    
    /**
     * Set up initial game state display
     */
    private void setupInitialGameState() {
        // Set up player information based on game state
        if (blackPlayerName != null) blackPlayerName.setText("Black Player");
        if (whitePlayerName != null) whitePlayerName.setText("White Player");
        
        if (blackTimer != null) blackTimer.setText("⏱ 15:00");
        if (whiteTimer != null) whiteTimer.setText("⏱ 15:00");
        
        // Set initial status based on game state
        updateGameStatusDisplay();
        
        if (statusLabel != null) statusLabel.setText("Game started - White to move");
        if (connectionStatus != null) connectionStatus.setText("🟢 Local Game");
        
        // Clear captured pieces displays (will be populated as game progresses)
        if (capturedWhitePieces != null) capturedWhitePieces.getChildren().clear();
        if (capturedBlackPieces != null) capturedBlackPieces.getChildren().clear();
    }
    
    /**
     * Update game status display based on current game state
     */
    private void updateGameStatusDisplay() {
        if (gameState != null) {
            // Update current player
            String currentPlayerText = gameState.getCurrentPlayer() == com.himelz.nexusboard.model.Color.WHITE ? 
                "White to move" : "Black to move";
            if (gameStatusLabel != null) gameStatusLabel.setText(currentPlayerText);
            
            // Update turn number
            int moveNumber = gameState.getMoveHistory().size() / 2 + 1;
            if (turnLabel != null) turnLabel.setText("Move: " + moveNumber);
            
            // Set game mode
            if (gameModeLabel != null) gameModeLabel.setText("Mode: Single Player");
            
            // Update player status
            if (gameState.getCurrentPlayer() == com.himelz.nexusboard.model.Color.WHITE) {
                if (whiteStatus != null) whiteStatus.setText("Your turn");
                if (blackStatus != null) blackStatus.setText("Waiting...");
            } else {
                if (blackStatus != null) blackStatus.setText("Your turn");
                if (whiteStatus != null) whiteStatus.setText("Waiting...");
            }
        }
    }
    

    
    /**
     * Add a captured piece to the display
     */
    private void addCapturedPiece(FlowPane container, String pieceCode, double size) {
        ImageView pieceImage = createPieceImageView(pieceCode, size);
        if (pieceImage != null) {
            pieceImage.getStyleClass().add("captured-piece");
            container.getChildren().add(pieceImage);
        } else {
            // Fallback to Unicode
            Label pieceLabel = new Label(getUnicodePiece(pieceCode));
            pieceLabel.getStyleClass().add("captured-piece");
            container.getChildren().add(pieceLabel);
        }
    }
    
    /**
     * Configure UI component properties
     */
    private void configureUIComponents() {
        // Set button tooltips
        newGameButton.setTooltip(new Tooltip("Start a new chess game"));
        undoButton.setTooltip(new Tooltip("Undo the last move"));
        resignButton.setTooltip(new Tooltip("Resign the current game"));
        drawButton.setTooltip(new Tooltip("Offer a draw to your opponent"));
        
        // Configure move history
        moveHistoryList.setCellFactory(listView -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });
        
        // Move history starts empty
    }
    
    /**
     * Set up event handlers for UI components
     */
    private void setupEventHandlers() {
        newGameButton.setOnAction(e -> handleNewGame());
        undoButton.setOnAction(e -> handleUndo());
        resignButton.setOnAction(e -> handleResign());
        drawButton.setOnAction(e -> handleOfferDraw());
    }
    
    // Event handler methods - fully functional game logic
    
    private void handleNewGame() {
        if (statusLabel != null) statusLabel.setText("New game started");
        // Create new game state and refresh board
        gameState = new GameState();
        clearSelection();
        initializeChessBoard();
        setupInitialGameState();
        if (moveHistoryList != null) moveHistoryList.getItems().clear();
        enableGameControls();
    }
    
    private void handleUndo() {
        // TODO: Implement undo functionality when GameState supports it
        if (statusLabel != null) statusLabel.setText("Undo not yet implemented");
    }
    
    private void handleResign() {
        if (gameState.getGameStatus() == GameState.GameStatus.ACTIVE || 
            gameState.getGameStatus() == GameState.GameStatus.CHECK) {
            
            var currentPlayer = gameState.getCurrentPlayer();
            var winner = currentPlayer.opposite();
            
            if (statusLabel != null) statusLabel.setText(currentPlayer + " resigned. " + winner + " wins!");
            if (gameStatusLabel != null) gameStatusLabel.setText("Game Over - " + winner + " wins by resignation");
            
            disableGameControls();
        }
    }
    
    private void handleOfferDraw() {
        if (gameState.getGameStatus() == GameState.GameStatus.ACTIVE || 
            gameState.getGameStatus() == GameState.GameStatus.CHECK) {
            
            if (statusLabel != null) statusLabel.setText("Draw offered (auto-accepted in single player)");
            if (gameStatusLabel != null) gameStatusLabel.setText("Game Over - Draw by agreement");
            
            disableGameControls();
        }
    }
    
    /**
     * Create a fallback scene if FXML loading fails
     */
    private void createFallbackScene() {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #f8f9fa;");
        
        Label titleLabel = new Label("Chess Game");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold;");
        
        Label errorLabel = new Label("Failed to load game interface");
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: red;");
        
        Button backButton = new Button("Back to Menu");
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30;");
        backButton.setOnAction(e -> primaryStage.close());
        
        root.getChildren().addAll(titleLabel, errorLabel, backButton);
        
        scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Nexus Board - Error");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Update game status display (for future use)
     */
    public void updateGameStatus(String status) {
        if (gameStatusLabel != null) {
            gameStatusLabel.setText(status);
        }
    }
    
    /**
     * Refresh the board display with current game state
     */
    public void refreshBoard() {
        initializeChessBoard();
        updateGameStatusDisplay();
    }
    
    /**
     * Get the current game state
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Add move to history display (for future use)
     */
    public void addMoveToHistory(String move) {
        if (moveHistoryList != null) {
            moveHistoryList.getItems().add(move);
            // Auto-scroll to latest move
            moveHistoryList.scrollTo(moveHistoryList.getItems().size() - 1);
        }
    }
    
    /**
     * Update player timers (for future use)
     */
    public void updateTimer(boolean isWhite, String time) {
        if (isWhite && whiteTimer != null) {
            whiteTimer.setText("⏱ " + time);
        } else if (!isWhite && blackTimer != null) {
            blackTimer.setText("⏱ " + time);
        }
    }
}