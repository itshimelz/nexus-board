package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.network.Server;
import com.himelz.nexusboard.viewmodel.GameScreenViewModel;
import javafx.beans.value.ChangeListener;
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
 * Game Screen Controller following MVVM pattern.
 * Handles only UI presentation and data binding with GameScreenViewModel.
 */
public class GameScreen implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private GameScreenViewModel viewModel;
    
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
    @FXML private Label gameProgressLabel;
    @FXML private Label materialBalanceLabel;
    @FXML private Label lastMoveLabel;
    @FXML private Label checkStatusLabel;
    @FXML private HBox checkStatusBox;
    
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
    
    public GameScreen(Stage stage) {
        this.primaryStage = stage;
        this.viewModel = new GameScreenViewModel();
    }
    
    /**
     * Constructor for network games
     */
    public GameScreen(Stage stage, Client client, Server server, boolean isHost, 
                      Color localPlayerColor, String localPlayerId) {
        this.primaryStage = stage;
        this.viewModel = new GameScreenViewModel(client, server, isHost, localPlayerColor, localPlayerId);
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
        
        // Set up data binding with ViewModel
        setupDataBinding();
        
        // Initialize the chess board
        initializeChessBoard();
        
        // Configure UI components
        configureUIComponents();
        
        // Set up event handlers
        setupEventHandlers();
    }
    
    /**
     * Set up data binding between UI components and ViewModel
     */
    private void setupDataBinding() {
        // Bind game status and information
        if (gameStatusLabel != null) {
            gameStatusLabel.textProperty().bind(viewModel.gameStatusMessageProperty());
        }
        if (turnLabel != null) {
            turnLabel.textProperty().bind(viewModel.currentPlayerTurnProperty());
        }
        if (gameModeLabel != null) {
            gameModeLabel.textProperty().bind(viewModel.gameModeTextProperty());
        }
        
        // Bind enhanced status information
        if (gameProgressLabel != null) {
            gameProgressLabel.textProperty().bind(viewModel.gameProgressTextProperty());
        }
        if (materialBalanceLabel != null) {
            materialBalanceLabel.textProperty().bind(viewModel.materialBalanceTextProperty());
        }
        if (lastMoveLabel != null) {
            lastMoveLabel.textProperty().bind(viewModel.lastMoveTextProperty());
        }
        if (checkStatusLabel != null) {
            checkStatusLabel.textProperty().bind(viewModel.checkStatusTextProperty());
        }
        if (checkStatusBox != null) {
            checkStatusBox.visibleProperty().bind(viewModel.isCheckStatusVisibleProperty());
            checkStatusBox.managedProperty().bind(viewModel.isCheckStatusVisibleProperty());
        }
        
        // Bind player information
        if (whitePlayerName != null) {
            whitePlayerName.textProperty().bind(viewModel.whitePlayerNameProperty());
        }
        if (blackPlayerName != null) {
            blackPlayerName.textProperty().bind(viewModel.blackPlayerNameProperty());
        }
        if (whiteTimer != null) {
            whiteTimer.textProperty().bind(viewModel.whiteTimerProperty());
        }
        if (blackTimer != null) {
            blackTimer.textProperty().bind(viewModel.blackTimerProperty());
        }
        
        // Bind analysis information
        if (evaluationLabel != null) {
            evaluationLabel.textProperty().bind(viewModel.evaluationTextProperty());
        }
        if (bestMoveLabel != null) {
            bestMoveLabel.textProperty().bind(viewModel.bestMoveTextProperty());
        }
        
        // Bind button states
        if (undoButton != null) {
            undoButton.disableProperty().bind(viewModel.canUndoProperty().not());
        }
        if (resignButton != null) {
            resignButton.disableProperty().bind(viewModel.canResignProperty().not());
        }
        if (drawButton != null) {
            drawButton.disableProperty().bind(viewModel.canOfferDrawProperty().not());
        }
        
        // Bind move history
        if (moveHistoryList != null) {
            moveHistoryList.setItems(viewModel.getMoveHistory());
        }
        
        // Listen for board state changes
        viewModel.boardStateProperty().addListener((obs, oldBoard, newBoard) -> {
            refreshBoardDisplay();
        });
        
        // Listen for selection changes
        viewModel.selectedPositionProperty().addListener((obs, oldPos, newPos) -> {
            updateSelectionHighlight(oldPos, newPos);
        });
        
        // Listen for valid moves changes
        viewModel.validMovesProperty().addListener((obs, oldMoves, newMoves) -> {
            updateMoveHighlights();
        });
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
     * Initialize the 8x8 chess board using ViewModel board state
     */
    private void initializeChessBoard() {
        // Clear any existing content
        chessBoard.getChildren().clear();
        
        // Determine board orientation based on player color
        // White player: white pieces at bottom (standard orientation)
        // Black player: black pieces at bottom (flipped orientation)
        boolean isFlipped = viewModel.isNetworkGame() && 
                           viewModel.getLocalPlayerColor() == Color.BLACK;
        
        // Create 8x8 grid of squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                // Calculate actual board position based on orientation
                int displayRow = isFlipped ? (7 - row) : row;
                int displayCol = col; // Columns remain the same
                
                StackPane square = createChessSquare(displayRow, displayCol);
                chessBoard.add(square, col, row); // Add to grid at visual position
            }
        }
        
        // Initial board refresh
        refreshBoardDisplay();
    }
    
    /**
     * Refresh board display based on current ViewModel state
     */
    private void refreshBoardDisplay() {
        ChessPiece[][] boardState = viewModel.boardStateProperty().get();
        if (boardState == null) return;
        
        // Determine board orientation
        boolean isFlipped = viewModel.isNetworkGame() && 
                           viewModel.getLocalPlayerColor() == Color.BLACK;
        
        for (int boardRow = 0; boardRow < 8; boardRow++) {
            for (int boardCol = 0; boardCol < 8; boardCol++) {
                // Convert board position to visual position
                int visualRow = isFlipped ? (7 - boardRow) : boardRow;
                int visualCol = boardCol; // Columns remain the same
                
                StackPane square = getSquareAt(visualRow, visualCol);
                if (square != null) {
                    // Clear existing pieces
                    square.getChildren().removeIf(node -> 
                        node instanceof ImageView || 
                        (node instanceof Label && ((Label) node).getStyleClass().contains("chess-piece")));
                    
                    // Add piece if present
                    ChessPiece piece = boardState[boardRow][boardCol];
                    if (piece != null) {
                        addPieceToSquare(square, piece);
                    }
                }
            }
        }
    }
    
    /**
     * Create a chess square with appropriate styling
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
        
        // Add hover effect and click handler
        square.setOnMouseEntered(e -> {
            if (!isSquareSelected(row, col) && !square.getStyleClass().contains("selected-square")) {
                square.getStyleClass().add("highlighted-square");
            }
        });
        
        square.setOnMouseExited(e -> {
            square.getStyleClass().remove("highlighted-square");
        });
        
        square.setOnMouseClicked(e -> handleSquareClick(row, col));
        
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
     * Handle chess square click - delegates to ViewModel
     */
    private void handleSquareClick(int visualRow, int visualCol) {
        // Convert visual position to board position
        Position boardPosition = visualToBoard(visualRow, visualCol);
        viewModel.handleSquareClick(boardPosition);
        
        // Update status display
        if (statusLabel != null) {
            ChessPiece piece = viewModel.getPieceAt(boardPosition);
            if (piece != null && viewModel.isSquareSelected(boardPosition)) {
                char file = (char)('a' + boardPosition.getCol());
                int rank = 8 - boardPosition.getRow();
                statusLabel.setText("Selected: " + piece.getClass().getSimpleName() + " at " + file + rank);
            }
        }
    }
    
    /**
     * Convert visual position to board position
     */
    private Position visualToBoard(int visualRow, int visualCol) {
        boolean isFlipped = viewModel.isNetworkGame() && 
                           viewModel.getLocalPlayerColor() == Color.BLACK;
        
        int boardRow = isFlipped ? (7 - visualRow) : visualRow;
        int boardCol = visualCol; // Columns remain the same
        
        return new Position(boardRow, boardCol);
    }
    
    /**
     * Convert board position to visual position
     */
    private Position boardToVisual(int boardRow, int boardCol) {
        boolean isFlipped = viewModel.isNetworkGame() && 
                           viewModel.getLocalPlayerColor() == Color.BLACK;
        
        int visualRow = isFlipped ? (7 - boardRow) : boardRow;
        int visualCol = boardCol; // Columns remain the same
        
        return new Position(visualRow, visualCol);
    }
    
    /**
     * Check if a square is selected
     */
    private boolean isSquareSelected(int visualRow, int visualCol) {
        Position boardPosition = visualToBoard(visualRow, visualCol);
        return viewModel.isSquareSelected(boardPosition);
    }
    
    /**
     * Update selection highlighting based on ViewModel state
     */
    private void updateSelectionHighlight(Position oldPos, Position newPos) {
        // Clear old selection highlight
        if (oldPos != null) {
            Position oldVisual = boardToVisual(oldPos.getRow(), oldPos.getCol());
            StackPane oldSquare = getSquareAt(oldVisual.getRow(), oldVisual.getCol());
            if (oldSquare != null) {
                oldSquare.getStyleClass().remove("selected-square");
            }
        }
        
        // Add new selection highlight
        if (newPos != null) {
            Position newVisual = boardToVisual(newPos.getRow(), newPos.getCol());
            StackPane newSquare = getSquareAt(newVisual.getRow(), newVisual.getCol());
            if (newSquare != null) {
                newSquare.getStyleClass().add("selected-square");
            }
        }
    }
    
    /**
     * Update move highlights based on ViewModel valid moves
     */
    private void updateMoveHighlights() {
        // Clear all existing move highlights
        clearAllMoveHighlights();
        
        // Add highlights for valid moves
        for (Position boardPosition : viewModel.validMovesProperty().get()) {
            Position visualPosition = boardToVisual(boardPosition.getRow(), boardPosition.getCol());
            StackPane square = getSquareAt(visualPosition.getRow(), visualPosition.getCol());
            if (square != null) {
                square.getStyleClass().add("possible-move");
            }
        }
    }
    
    /**
     * Clear all move highlights
     */
    private void clearAllMoveHighlights() {
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
     * Set up event handlers for UI components
     */
    private void setupEventHandlers() {
        newGameButton.setOnAction(e -> handleNewGame());
        undoButton.setOnAction(e -> handleUndo());
        resignButton.setOnAction(e -> handleResign());
        drawButton.setOnAction(e -> handleDraw());
    }
    
    // Event handler methods - delegate to ViewModel
    
    @FXML
    private void handleNewGame() {
        viewModel.newGame();
        if (statusLabel != null) statusLabel.setText("New game started");
    }
    
    @FXML
    private void handleUndo() {
        viewModel.undoMove();
        if (statusLabel != null) statusLabel.setText("Move undone");
    }
    
    @FXML
    private void handleResign() {
        viewModel.resignGame();
        if (statusLabel != null) statusLabel.setText("Game resigned");
    }
    
    @FXML
    private void handleDraw() {
        viewModel.offerDraw();
        if (statusLabel != null) statusLabel.setText("Draw offered");
    }
    
    
    // Utility methods
    
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
     * Configure UI component properties
     */
    private void configureUIComponents() {
        // Set button tooltips
        if (newGameButton != null) {
            newGameButton.setTooltip(new Tooltip("Start a new chess game"));
        }
        if (undoButton != null) {
            undoButton.setTooltip(new Tooltip("Undo the last move"));
        }
        if (resignButton != null) {
            resignButton.setTooltip(new Tooltip("Resign the current game"));
        }
        if (drawButton != null) {
            drawButton.setTooltip(new Tooltip("Offer a draw to your opponent"));
        }
        
        // Configure move history
        if (moveHistoryList != null) {
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
        }
        
        // Set initial connection status
        if (connectionStatus != null) {
            connectionStatus.setText("🟢 Local Game");
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
}