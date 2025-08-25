package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.viewmodel.JoinGameDialogViewModel;
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
 * Join Game Dialog Controller implementing MVVM pattern.
 * Handles client connection to multiplayer games.
 */
public class JoinGameDialog implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private JoinGameDialogViewModel viewModel;
    
    // FXML Components
    @FXML private Text titleText;
    @FXML private Label subtitleLabel;
    @FXML private TextField playerNameField;
    @FXML private TextField serverIpField;
    @FXML private TextField portField;
    @FXML private TextField connectionStringField;
    @FXML private Label statusLabel;
    @FXML private ProgressIndicator connectionProgress;
    @FXML private Button parseConnectionButton;
    @FXML private Button connectButton;
    @FXML private Button disconnectButton;
    @FXML private Button backButton;
    @FXML private Button helpButton;
    @FXML private ListView<String> recentConnectionsList;
    
    public JoinGameDialog(Stage stage) {
        this.primaryStage = stage;
        this.viewModel = new JoinGameDialogViewModel(stage);
    }
    
    /**
     * Display the join game dialog
     */
    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/JoinGameDialog.fxml"));
            loader.setController(this);
            
            VBox root = loader.load();
            scene = new Scene(root);
            
            // Set dynamic sizing based on content
            primaryStage.sizeToScene();
            primaryStage.setMinWidth(500);
            primaryStage.setMinHeight(700);
            
            // Apply CSS if available
            try {
                scene.getStylesheets().add(getClass().getResource("/com/himelz/nexusboard/nexusboard/styles/application.css").toExternalForm());
            } catch (Exception e) {
                System.out.println("CSS file not found, using default styling");
            }
            
            primaryStage.setTitle("Nexus Board - Join Game");
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (IOException e) {
            System.err.println("Failed to load JoinGameDialog FXML: " + e.getMessage());
            e.printStackTrace();
            createBasicScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupDataBinding();
        configureUI();
        
        // Load recent connections
        viewModel.loadRecentConnections();
    }
    
    /**
     * Set up data binding between UI components and ViewModel
     */
    private void setupDataBinding() {
        // Bind text properties
        if (playerNameField != null) {
            playerNameField.textProperty().bindBidirectional(viewModel.playerNameProperty());
        }
        
        if (serverIpField != null) {
            serverIpField.textProperty().bindBidirectional(viewModel.serverIpProperty());
        }
        
        if (portField != null) {
            portField.textProperty().bindBidirectional(viewModel.portProperty());
        }
        
        if (connectionStringField != null) {
            connectionStringField.textProperty().bindBidirectional(viewModel.connectionStringProperty());
        }
        
        if (statusLabel != null) {
            statusLabel.textProperty().bind(viewModel.statusMessageProperty());
        }
        
        if (connectionProgress != null) {
            connectionProgress.visibleProperty().bind(viewModel.isConnectingProperty());
        }
        
        if (recentConnectionsList != null) {
            recentConnectionsList.setItems(viewModel.getRecentConnections());
        }
        
        // Bind button states
        if (connectButton != null) {
            connectButton.disableProperty().bind(
                viewModel.isConnectingProperty().or(
                    viewModel.isConnectedProperty()).or(
                        viewModel.playerNameProperty().isEmpty().or(
                            viewModel.serverIpProperty().isEmpty().or(
                                viewModel.portProperty().isEmpty()
                            )
                        )
                    )
            );
        }
        
        if (disconnectButton != null) {
            disconnectButton.disableProperty().bind(
                viewModel.isConnectedProperty().not().and(viewModel.isConnectingProperty().not())
            );
        }
        
        if (parseConnectionButton != null) {
            parseConnectionButton.disableProperty().bind(viewModel.connectionStringProperty().isEmpty());
        }
    }
    
    /**
     * Configure UI component properties
     */
    private void configureUI() {
        // Add hover effects to buttons
        setupButtonHoverEffects();
        
        // Set button tooltips
        if (connectButton != null) {
            connectButton.setTooltip(new Tooltip("Connect to the game server"));
        }
        
        if (disconnectButton != null) {
            disconnectButton.setTooltip(new Tooltip("Disconnect from the server"));
        }
        
        if (parseConnectionButton != null) {
            parseConnectionButton.setTooltip(new Tooltip("Parse connection string (IP:Port)"));
        }
        
        if (backButton != null) {
            backButton.setTooltip(new Tooltip("Return to multiplayer menu"));
        }
        
        if (helpButton != null) {
            helpButton.setTooltip(new Tooltip("Get help with joining games"));
        }
        
        // Set default player name if empty
        if (playerNameField != null && playerNameField.getText().isEmpty()) {
            playerNameField.setText(System.getProperty("user.name", "Player"));
        }
        
        // Configure recent connections list
        if (recentConnectionsList != null) {
            recentConnectionsList.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2) {
                    String selected = recentConnectionsList.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        viewModel.selectRecentConnection(selected);
                    }
                }
            });
        }
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonHoverEffects() {
        // Connect Button hover effect
        if (connectButton != null) {
            setupButtonHover(connectButton, "#3498db", "#5dade2");
        }
        
        // Disconnect Button hover effect
        if (disconnectButton != null) {
            setupButtonHover(disconnectButton, "#e74c3c", "#ec7063");
        }
        
        // Parse Connection Button hover effect
        if (parseConnectionButton != null) {
            setupButtonHover(parseConnectionButton, "#f39c12", "#f5b041");
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
    private void onConnectClick() {
        viewModel.connectToServer();
    }
    
    @FXML
    private void onDisconnectClick() {
        viewModel.disconnectFromServer();
    }
    
    @FXML
    private void onParseConnectionClick() {
        viewModel.parseConnectionString();
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
        
        Label titleLabel = new Label("Join Game");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        TextField nameField = new TextField();
        nameField.setPromptText("Enter your name");
        nameField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        TextField ipField = new TextField();
        ipField.setPromptText("Enter server IP");
        ipField.setStyle("-fx-font-size: 14px; -fx-padding: 10;");
        
        Button connectButton = new Button("Connect");
        connectButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #3498db; -fx-text-fill: white;");
        connectButton.setOnAction(_ -> viewModel.connectToServer());
        
        Button backButton = new Button("Back");
        backButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30; -fx-background-color: #95a5a6; -fx-text-fill: white;");
        backButton.setOnAction(_ -> viewModel.backToMultiplayerMenu());
        
        root.getChildren().addAll(titleLabel, nameField, ipField, connectButton, backButton);
        
        scene = new Scene(root);
        primaryStage.sizeToScene();
        primaryStage.setMinWidth(400);
        primaryStage.setMinHeight(400);
        primaryStage.setTitle("Nexus Board - Join Game");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
    
    /**
     * Get the ViewModel for testing or external access
     */
    public JoinGameDialogViewModel getViewModel() {
        return viewModel;
    }
}
