package com.himelz.nexusboard.viewController;

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
    
    // Chess piece Unicode symbols (kept for fallback)
    private static final String[] WHITE_PIECES = {"♔", "♕", "♖", "♗", "♘", "♙"};
    private static final String[] BLACK_PIECES = {"♚", "♛", "♜", "♝", "♞", "♟"};
    
    // Initial chess position with piece identifiers
    private static final String[][] INITIAL_POSITION = {
        {"br", "bn", "bb", "bq", "bk", "bb", "bn", "br"}, // Rank 8
        {"bp", "bp", "bp", "bp", "bp", "bp", "bp", "bp"}, // Rank 7
        {"", "", "", "", "", "", "", ""},              // Rank 6
        {"", "", "", "", "", "", "", ""},              // Rank 5
        {"", "", "", "", "", "", "", ""},              // Rank 4
        {"", "", "", "", "", "", "", ""},              // Rank 3
        {"wp", "wp", "wp", "wp", "wp", "wp", "wp", "wp"}, // Rank 2
        {"wr", "wn", "wb", "wq", "wk", "wb", "wn", "wr"}  // Rank 1
    };
    
    public GameScreen(Stage stage) {
        this.primaryStage = stage;
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
     * Initialize the 8x8 chess board with pieces
     */
    private void initializeChessBoard() {
        // Clear any existing content
        chessBoard.getChildren().clear();
        
        // Create 8x8 grid of squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createChessSquare(row, col);
                chessBoard.add(square, col, row);
            }
        }
    }
    
    /**
     * Create a chess square with appropriate styling and piece
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
        
        // Add chess piece if present in initial position
        String pieceCode = INITIAL_POSITION[row][col];
        if (!pieceCode.isEmpty()) {
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
                String unicodePiece = getUnicodePiece(pieceCode);
                Label pieceLabel = new Label(unicodePiece);
                pieceLabel.getStyleClass().add("chess-piece");
                
                // Style white and black pieces differently
                if (pieceCode.startsWith("w")) {
                    pieceLabel.getStyleClass().addAll("white-piece", "piece-shadow");
                } else {
                    pieceLabel.getStyleClass().addAll("black-piece", "piece-shadow");
                }
                
                square.getChildren().add(pieceLabel);
            }
        }
        
        // Add hover effect and click handler (for future use)
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
     * Handle chess square click (placeholder for future game logic)
     */
    private void handleSquareClick(int row, int col, StackPane square) {
        // Remove previous selections
        chessBoard.getChildren().forEach(node -> {
            node.getStyleClass().remove("selected-square");
        });
        
        // Highlight clicked square
        square.getStyleClass().add("selected-square");
        
        // Update status (for demonstration)
        char file = (char)('a' + col);
        int rank = 8 - row;
        statusLabel.setText("Selected: " + file + rank);
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
        // Player names
        blackPlayerName.setText("Black Player");
        whitePlayerName.setText("You");
        
        // Timers
        blackTimer.setText("⏱ 15:00");
        whiteTimer.setText("⏱ 15:00");
        
        // Player status
        blackStatus.setText("Waiting...");
        whiteStatus.setText("Your turn");
        
        // Game status
        gameStatusLabel.setText("White to move");
        turnLabel.setText("Move: 1");
        gameModeLabel.setText("Mode: Single Player");
        
        // Analysis (placeholder)
        evaluationLabel.setText("Evaluation: +0.0");
        bestMoveLabel.setText("Best: e4");
        
        // Status bar
        statusLabel.setText("Game started - White to move");
        connectionStatus.setText("🟢 Local Game");
        
        // Setup captured pieces display with sample pieces
        setupCapturedPiecesDisplay();
    }
    
    /**
     * Setup captured pieces display with sample captured pieces
     */
    private void setupCapturedPiecesDisplay() {
        // Clear existing captured pieces
        if (capturedWhitePieces != null) {
            capturedWhitePieces.getChildren().clear();
            // Add some sample captured white pieces
            addCapturedPiece(capturedWhitePieces, "wq", 24);
            addCapturedPiece(capturedWhitePieces, "wb", 24);
            addCapturedPiece(capturedWhitePieces, "wp", 24);
            addCapturedPiece(capturedWhitePieces, "wp", 24);
        }
        
        if (capturedBlackPieces != null) {
            capturedBlackPieces.getChildren().clear();
            // Add some sample captured black pieces
            addCapturedPiece(capturedBlackPieces, "bq", 24);
            addCapturedPiece(capturedBlackPieces, "bn", 24);
            addCapturedPiece(capturedBlackPieces, "bp", 24);
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
        
        // Add some sample moves for demonstration
        moveHistoryList.getItems().addAll(
            "1. e4 e5",
            "2. Nf3 Nc6", 
            "3. Bb5 a6"
        );
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
    
    // Event handler methods (placeholders for future game logic)
    
    private void handleNewGame() {
        statusLabel.setText("New game started");
        // Reset board to initial position
        initializeChessBoard();
        setupInitialGameState();
        moveHistoryList.getItems().clear();
    }
    
    private void handleUndo() {
        statusLabel.setText("Undo move requested");
    }
    
    private void handleResign() {
        statusLabel.setText("Game resigned");
        gameStatusLabel.setText("Game Over - Resignation");
    }
    
    private void handleOfferDraw() {
        statusLabel.setText("Draw offered");
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