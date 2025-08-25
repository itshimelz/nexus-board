package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.viewmodel.MultiplayerMenuViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Multiplayer Menu Controller implementing MVVM pattern.
 * Handles multiplayer game options: Host, Join, and Direct Connect.
 */
public class MultiplayerMenu implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private MultiplayerMenuViewModel viewModel;
    
    // FXML Components
    @FXML private Text gameTitleText;
    @FXML private Label multiplayerTitleLabel;
    @FXML private Label connectionStatusLabel;
    @FXML private Label networkStatusLabel;
    @FXML private Label loadingMessage;
    
    @FXML private Button hostGameButton;
    @FXML private Button joinGameButton;
    @FXML private Button directConnectButton;
    @FXML private Button backButton;
    
    @FXML private VBox loadingOverlay;
    
    public MultiplayerMenu(Stage stage) {
        this.primaryStage = stage;
        this.viewModel = new MultiplayerMenuViewModel(stage);
    }
    
    /**
     * Display the multiplayer menu
     */
    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/MultiplayerMenu.fxml"));
            loader.setController(this);
            
            BorderPane root = loader.load();
            scene = new Scene(root, 800, 700);
            
            // Apply CSS if available
            try {
                scene.getStylesheets().add(getClass().getResource("/com/himelz/nexusboard/nexusboard/styles/application.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS file not found, using default styling");
            }
            
            primaryStage.setTitle("Nexus Board - Multiplayer");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Failed to load MultiplayerMenu FXML: " + e.getMessage());
            e.printStackTrace();
            createBasicScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDataBinding();
        configureUI();
    }
    
    /**
     * Set up data binding between UI components and ViewModel
     */
    private void setupDataBinding() {
        // Bind text properties
        if (gameTitleText != null) {
            gameTitleText.textProperty().bind(viewModel.gameTitleProperty());
        }
        
        if (connectionStatusLabel != null) {
            connectionStatusLabel.textProperty().bind(viewModel.connectionStatusProperty());
        }
        
        if (networkStatusLabel != null) {
            networkStatusLabel.textProperty().bind(viewModel.networkStatusProperty());
        }
        
        if (loadingMessage != null) {
            loadingMessage.textProperty().bind(viewModel.loadingMessageProperty());
        }
        
        // Bind button states
        if (hostGameButton != null) {
            hostGameButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.hostGameEnabledProperty().not()));
        }
        
        if (joinGameButton != null) {
            joinGameButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.joinGameEnabledProperty().not()));
        }
        
        if (directConnectButton != null) {
            directConnectButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.directConnectEnabledProperty().not()));
        }
        
        if (backButton != null) {
            backButton.disableProperty().bind(viewModel.isLoadingProperty());
        }
        
        // Bind loading overlay visibility
        if (loadingOverlay != null) {
            loadingOverlay.visibleProperty().bind(viewModel.isLoadingProperty());
        }
    }
    
    /**
     * Configure UI component properties
     */
    private void configureUI() {
        // Add hover effects to buttons
        setupButtonHoverEffects();
        
        // Set button tooltips
        if (hostGameButton != null) {
            hostGameButton.setTooltip(new Tooltip("Start a game server and wait for players to join"));
        }
        
        if (joinGameButton != null) {
            joinGameButton.setTooltip(new Tooltip("Connect to an existing game server"));
        }
        
        if (directConnectButton != null) {
            directConnectButton.setTooltip(new Tooltip("Quick connect by entering server IP directly"));
        }
        
        if (backButton != null) {
            backButton.setTooltip(new Tooltip("Return to main menu"));
        }
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonHoverEffects() {
        // Host Game Button hover effect
        if (hostGameButton != null) {
            setupButtonHover(hostGameButton, "#27ae60", "#2ecc71");
        }
        
        // Join Game Button hover effect
        if (joinGameButton != null) {
            setupButtonHover(joinGameButton, "#3498db", "#5dade2");
        }
        
        // Direct Connect Button hover effect
        if (directConnectButton != null) {
            setupButtonHover(directConnectButton, "#f39c12", "#f5b041");
        }
        
        // Back Button hover effect
        if (backButton != null) {
            setupButtonHover(backButton, "#95a5a6", "#aab7b8");
        }
    }
    
    /**
     * Setup hover effect for a specific button
     */
    private void setupButtonHover(Button button, String normalColor, String hoverColor) {
        String originalStyle = button.getStyle();
        
        button.setOnMouseEntered(_ -> 
            button.setStyle(originalStyle.replace(normalColor, hoverColor))
        );
        
        button.setOnMouseExited(_ -> 
            button.setStyle(originalStyle)
        );
    }
    
    // FXML Event Handlers
    
    @FXML
    private void onHostGameClick() {
        viewModel.hostGame();
    }
    
    @FXML
    private void onJoinGameClick() {
        viewModel.joinGame();
    }
    
    @FXML
    private void onDirectConnectClick() {
        viewModel.directConnect();
    }
    
    @FXML
    private void onBackClick() {
        viewModel.backToMainMenu();
    }
    
    /**
     * Create a basic fallback scene if FXML loading fails
     */
    private void createBasicScene() {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #ecf0f1;");
        
        Label titleLabel = new Label("Multiplayer Menu");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Button hostButton = new Button("Host Game");
        hostButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #27ae60; -fx-text-fill: white;");
        hostButton.setOnAction(_ -> viewModel.hostGame());
        
        Button joinButton = new Button("Join Game");
        joinButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #3498db; -fx-text-fill: white;");
        joinButton.setOnAction(_ -> viewModel.joinGame());
        
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #95a5a6; -fx-text-fill: white;");
        backButton.setOnAction(_ -> viewModel.backToMainMenu());
        
        root.getChildren().addAll(titleLabel, hostButton, joinButton, backButton);
        
        scene = new Scene(root, 800, 700);
        primaryStage.setTitle("Nexus Board - Multiplayer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Get the ViewModel for testing or external access
     */
    public MultiplayerMenuViewModel getViewModel() {
        return viewModel;
    }
}
