package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Position;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeScreen implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private GameState gameState;
    
    // Move selection state
    private Position selectedSquare;
    private StackPane selectedSquarePane;
    
    // FXML Controls
    @FXML private GridPane chessBoard;
    @FXML private Label gameStatusLabel;
    @FXML private Label turnLabel;
    @FXML private Button newGameButton;
    @FXML private Button undoButton;
    @FXML private Button resignButton;
    @FXML private ListView<String> moveHistoryList;
    @FXML private HBox capturedWhitePieces;
    @FXML private HBox capturedBlackPieces;
    @FXML private Label statusLabel;

    public HomeScreen(Stage stage) {
        this.primaryStage = stage;
        this.gameState = new GameState();
    }

    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/HomeScreen.fxml"));
            loader.setController(this);
            
            // Create scene
            scene = new Scene(loader.load(), 900, 700);
            
            // Configure stage
            primaryStage.setTitle("Nexus Board - Chess Game");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(700);
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: create a basic scene if FXML loading fails
            createBasicScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the chess board
        initializeChessBoard();
        
        // Set up event handlers
        setupEventHandlers();
        
        // Update initial game status
        updateGameStatus();
        
        // Render initial chess pieces
        renderChessPieces();
    }
    
    private void initializeChessBoard() {
        // Create chess board squares
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = createChessSquare(row, col);
                chessBoard.add(square, col, row);
            }
        }
    }
    
    private StackPane createChessSquare(int row, int col) {
        StackPane square = new StackPane();
        square.setPrefSize(60, 60);
        
        // Alternate square colors (chess board pattern)
        if ((row + col) % 2 == 0) {
            square.setStyle("-fx-background-color: #F0D9B5;"); // Light squares
        } else {
            square.setStyle("-fx-background-color: #B58863;"); // Dark squares
        }
        
        // Add click event handler for move input
        square.setOnMouseClicked(event -> handleSquareClick(row, col));
        
        // Make square responsive to hover
        square.setOnMouseEntered(event -> {
            square.setStyle(square.getStyle() + "-fx-border-color: #666; -fx-border-width: 2px;");
        });
        
        square.setOnMouseExited(event -> {
            // Reset to original color
            if ((row + col) % 2 == 0) {
                square.setStyle("-fx-background-color: #F0D9B5;");
            } else {
                square.setStyle("-fx-background-color: #B58863;");
            }
        });
        
        return square;
    }
    
    private void handleSquareClick(int row, int col) {
        Position clickedPosition = new Position(row, col);
        StackPane clickedSquare = getSquare(row, col);
        
        if (selectedSquare == null) {
            // First click - select a piece
            var piece = gameState.getBoard().getPiece(clickedPosition);
            if (piece != null && piece.getColor() == gameState.getCurrentPlayer()) {
                selectedSquare = clickedPosition;
                selectedSquarePane = clickedSquare;
                highlightSquare(clickedSquare, true);
                statusLabel.setText("Selected: " + piece.getClass().getSimpleName() + " at " + (char)('a' + col) + (8 - row));
            } else {
                statusLabel.setText("No valid piece to select at " + (char)('a' + col) + (8 - row));
            }
        } else {
            // Second click - attempt to move
            if (clickedPosition.equals(selectedSquare)) {
                // Clicking same square - deselect
                clearSelection();
                statusLabel.setText("Selection cleared");
            } else {
                // Attempt to move from selected square to clicked square
                boolean moveSuccessful = gameState.makeMove(selectedSquare, clickedPosition);
                
                if (moveSuccessful) {
                    // Move was successful
                    renderChessPieces();
                    updateGameStatus();
                    addMoveToHistory(selectedSquare, clickedPosition);
                    clearSelection();
                    statusLabel.setText("Move: " + positionToAlgebraic(selectedSquare) + " to " + positionToAlgebraic(clickedPosition));
                    
                    // Check for game end conditions
                    checkGameEndConditions();
                } else {
                    // Invalid move
                    statusLabel.setText("Invalid move from " + positionToAlgebraic(selectedSquare) + " to " + positionToAlgebraic(clickedPosition));
                    clearSelection();
                }
            }
        }
    }
    
    private void clearSelection() {
        if (selectedSquarePane != null) {
            highlightSquare(selectedSquarePane, false);
        }
        selectedSquare = null;
        selectedSquarePane = null;
    }
    
    private void highlightSquare(StackPane square, boolean highlight) {
        if (highlight) {
            square.setStyle(square.getStyle() + "-fx-border-color: #FFD700; -fx-border-width: 3px;");
        } else {
            // Remove highlight - determine original color
            Integer row = GridPane.getRowIndex(square);
            Integer col = GridPane.getColumnIndex(square);
            if (row != null && col != null) {
                if ((row + col) % 2 == 0) {
                    square.setStyle("-fx-background-color: #F0D9B5;");
                } else {
                    square.setStyle("-fx-background-color: #B58863;");
                }
            }
        }
    }
    
    private String positionToAlgebraic(Position pos) {
        return (char)('a' + pos.getCol()) + String.valueOf(8 - pos.getRow());
    }
    
    private void addMoveToHistory(Position from, Position to) {
        String moveNotation = positionToAlgebraic(from) + "-" + positionToAlgebraic(to);
        moveHistoryList.getItems().add(moveNotation);
        
        // Auto-scroll to latest move
        moveHistoryList.scrollTo(moveHistoryList.getItems().size() - 1);
    }
    
    private void checkGameEndConditions() {
        var gameStatus = gameState.getGameStatus();
        switch (gameStatus) {
            case CHECKMATE:
                var winner = gameState.getWinner();
                statusLabel.setText("Checkmate! " + winner + " wins!");
                gameStatusLabel.setText("Game Over - " + winner + " wins");
                break;
            case STALEMATE:
                statusLabel.setText("Stalemate! Game is a draw.");
                gameStatusLabel.setText("Game Over - Stalemate");
                break;
            case CHECK:
                statusLabel.setText(gameState.getCurrentPlayer() + " is in check!");
                break;
            case DRAW:
                statusLabel.setText("Game is a draw.");
                gameStatusLabel.setText("Game Over - Draw");
                break;
            default:
                // Game continues
                break;
        }
    }
    
    private void setupEventHandlers() {
        newGameButton.setOnAction(event -> startNewGame());
        undoButton.setOnAction(event -> undoLastMove());
        resignButton.setOnAction(event -> resignGame());
    }
    
    private void startNewGame() {
        gameState = new GameState();
        clearSelection(); // Clear any selected pieces
        updateGameStatus();
        renderChessPieces(); // Re-render pieces for new game
        moveHistoryList.getItems().clear();
        capturedWhitePieces.getChildren().clear();
        capturedBlackPieces.getChildren().clear();
        statusLabel.setText("New game started");
    }
    
    private void undoLastMove() {
        // TODO: Implement undo functionality
        statusLabel.setText("Undo move (not implemented yet)");
    }
    
    private void resignGame() {
        // TODO: Implement resign functionality
        statusLabel.setText("Game resigned");
    }
    
    private void updateGameStatus() {
        gameStatusLabel.setText(gameState.getCurrentPlayer().toString() + " to move");
        // TODO: Update turn counter based on move history
        turnLabel.setText("Turn: " + (gameState.getMoveHistory().size() / 2 + 1));
    }
    
    private void renderChessPieces() {
        // Clear all existing piece labels
        clearBoardPieces();
        
        // Render pieces based on current game state
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position position = new Position(row, col);
                var piece = gameState.getBoard().getPiece(position);
                
                if (piece != null) {
                    addPieceToSquare(row, col, piece.getUnicodeSymbol());
                }
            }
        }
    }
    
    private void clearBoardPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                StackPane square = getSquare(row, col);
                if (square != null) {
                    // Remove any existing piece labels
                    square.getChildren().removeIf(node -> node instanceof Label);
                }
            }
        }
    }
    
    private void addPieceToSquare(int row, int col, String pieceSymbol) {
        StackPane square = getSquare(row, col);
        if (square != null) {
            Label pieceLabel = new Label(pieceSymbol);
            pieceLabel.setStyle("-fx-font-size: 36px; -fx-font-family: 'Segoe UI Symbol';");
            pieceLabel.setMouseTransparent(false); // Allow mouse events for drag and drop
            square.getChildren().add(pieceLabel);
        }
    }
    
    private StackPane getSquare(int row, int col) {
        // Find the square at the given position
        for (var node : chessBoard.getChildren()) {
            if (GridPane.getRowIndex(node) != null && GridPane.getColumnIndex(node) != null) {
                if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) {
                    return (StackPane) node;
                }
            }
        }
        return null;
    }
    
    private void createBasicScene() {
        // Create a basic scene as fallback
        javafx.scene.layout.StackPane root = new javafx.scene.layout.StackPane();
        javafx.scene.control.Label label = new javafx.scene.control.Label("Nexus Board Chess Game - UI Loading...");
        root.getChildren().add(label);
        
        scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Nexus Board - Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

}
