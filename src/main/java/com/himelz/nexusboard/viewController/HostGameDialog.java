package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.viewmodel.HostGameDialogViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Host Game Dialog Controller implementing MVVM pattern.
 * Handles server setup and configuration for hosting multiplayer games.
 */
public class HostGameDialog implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private HostGameDialogViewModel viewModel;
    
    // FXML Components
    @FXML private Text titleText;
    @FXML private Label subtitleLabel;
    @FXML private TextField playerNameField;
    @FXML private TextField ipAddressField;
    @FXML private TextField portField;
    @FXML private Label statusLabel;
    @FXML private Label playerCountLabel;
    @FXML private Button copyIpButton;
    @FXML private Button startServerButton;
    @FXML private Button stopServerButton;
    @FXML private Button backButton;
    @FXML private Button helpButton;
    
    public HostGameDialog(Stage stage) {
        this.primaryStage = stage;
        this.viewModel = new HostGameDialogViewModel(stage);
    }
    
    /**
     * Display the host game dialog
     */
    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/HostGameDialog.fxml"));
            loader.setController(this);
            
            VBox root = loader.load();
            scene = new Scene(root);
            
            // Set dynamic sizing based on content
            primaryStage.sizeToScene();
            
            // Apply CSS if available
            try {
                scene.getStylesheets().add(getClass().getResource("/com/himelz/nexusboard/nexusboard/styles/application.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS file not found, using default styling");
            }
            
            primaryStage.setTitle("Nexus Board - Host Game");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Failed to load HostGameDialog FXML: " + e.getMessage());
            e.printStackTrace();
            createBasicScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDataBinding();
        configureUI();
        
        // Initialize with user's IP address
        viewModel.initializeNetworkInfo();
    }
    
    /**
     * Set up data binding between UI components and ViewModel
     */
    private void setupDataBinding() {
        // Bind text properties
        if (playerNameField != null) {
            playerNameField.textProperty().bindBidirectional(viewModel.playerNameProperty());
        }
        
        if (ipAddressField != null) {
            ipAddressField.textProperty().bind(viewModel.ipAddressProperty());
        }
        
        if (portField != null) {
            portField.textProperty().bindBidirectional(viewModel.portProperty());
        }
        
        if (statusLabel != null) {
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        }
        
        if (playerCountLabel != null) {
            playerCountLabel.textProperty().bind(viewModel.playerCountProperty());
        }
        
        // Bind button states
        if (startServerButton != null) {
            startServerButton.disableProperty().bind(
                viewModel.isServerRunningProperty().or(
                    viewModel.playerNameProperty().isEmpty().or(
                        viewModel.portProperty().isEmpty()
                    )
                )
            );
        }
        
        if (stopServerButton != null) {
            stopServerButton.disableProperty().bind(viewModel.isServerRunningProperty().not());
        }
        
        if (copyIpButton != null) {
            copyIpButton.disableProperty().bind(viewModel.ipAddressProperty().isEmpty());
        }
    }
    
    /**
     * Configure UI component properties
     */
    private void configureUI() {
        // Add hover effects to buttons
        setupButtonHoverEffects();
        
        // Set button tooltips
        if (startServerButton != null) {
            startServerButton.setTooltip(new Tooltip("Start the game server"));
        }
        
        if (stopServerButton != null) {
            stopServerButton.setTooltip(new Tooltip("Stop the game server"));
        }
        
        if (copyIpButton != null) {
            copyIpButton.setTooltip(new Tooltip("Copy IP address to clipboard"));
        }
        
        if (backButton != null) {
            backButton.setTooltip(new Tooltip("Return to multiplayer menu"));
        }
        
        if (helpButton != null) {
            helpButton.setTooltip(new Tooltip("Get help with hosting games"));
        }
        
        // Set default player name if empty
        if (playerNameField != null && playerNameField.getText().isEmpty()) {
            playerNameField.setText(System.getProperty("user.name", "Player"));
        }
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonHoverEffects() {
        // Start Server Button hover effect
        if (startServerButton != null) {
            setupButtonHover(startServerButton, "#27ae60", "#2ecc71");
        }
        
        // Stop Server Button hover effect
        if (stopServerButton != null) {
            setupButtonHover(stopServerButton, "#e74c3c", "#ec7063");
        }
        
        // Copy IP Button hover effect
        if (copyIpButton != null) {
            setupButtonHover(copyIpButton, "#3498db", "#5dade2");
        }
        
        // Back Button hover effect
        if (backButton != null) {
            setupButtonHover(backButton, "#95a5a6", "#aab7b8");
        }
        
        // Help Button hover effect
        if (helpButton != null) {
            setupButtonHover(helpButton, "#f39c12", "#f5b041");
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
    private void onStartServerClick() {
        viewModel.startServer();
    }
    
    @FXML
    private void onStopServerClick() {
        viewModel.stopServer();
    }
    
    @FXML
    private void onCopyIpClick() {
        viewModel.copyIpToClipboard();
    }
    
    @FXML
    private void onBackClick() {
        viewModel.backToMultiplayerMenu();
    }
    
    @FXML
    private void onHelpClick() {
        viewModel.showHelp();
    }
    
    /**
     * Create a basic fallback scene if FXML loading fails
     */
    private void createBasicScene() {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #ecf0f1;");
        
        Label titleLabel = new Label("Host Game");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        Button startButton = new Button("Start Server");
        startButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #27ae60; -fx-text-fill: white;");
        startButton.setOnAction(_ -> viewModel.startServer());
        
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #95a5a6; -fx-text-fill: white;");
        backButton.setOnAction(_ -> viewModel.backToMultiplayerMenu());
        
        root.getChildren().addAll(titleLabel, nameField, startButton, backButton);
        
        scene = new Scene(root);
        primaryStage.sizeToScene();
        primaryStage.setTitle("Nexus Board - Host Game");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    /**
     * Get the ViewModel for testing or external access
     */
    public HostGameDialogViewModel getViewModel() {
        return viewModel;
    }
}
