package com.himelz.nexusboard.viewController;

import com.himelz.nexusboard.viewmodel.LandingScreenViewModel;
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
 * Landing Page Controller implementing MVVM pattern.
 * Handles UI interactions and binds to LandingScreenViewModel.
 */
public class LandingPage implements Initializable {

    private Stage primaryStage;
    private Scene scene;
    private LandingScreenViewModel viewModel;
    
    // FXML Components
    @FXML private Text gameTitleText;
    @FXML private Label welcomeLabel;
    @FXML private Label versionLabel;
    
    @FXML private Button singlePlayerButton;
    @FXML private Button multiplayerButton;
    @FXML private Button settingsButton;
    @FXML private Button aboutButton;
    @FXML private Button exitButton;
    
    @FXML private VBox loadingOverlay;
    
    public LandingPage(Stage stage) {
        this.primaryStage = stage;
        this.viewModel = new LandingScreenViewModel(stage);
    }
    
    /**
     * Display the landing page
     */
    public void show() {
        try {
            // Load FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/himelz/nexusboard/nexusboard/screens/Landing.fxml"));
            loader.setController(this);
            
            // Create scene
            scene = new Scene(loader.load());
            
            // Add CSS stylesheet
            String cssPath = getClass().getResource("/com/himelz/nexusboard/nexusboard/styles/landing.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
            
            // Configure stage with dynamic sizing
            primaryStage.setTitle("Nexus Board - Chess Game");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.centerOnScreen();
            primaryStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            // Fallback: create a basic scene if FXML loading fails
            createBasicScene();
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize ViewModel
        viewModel.initialize();
        
        // Set up data binding
        setupDataBinding();
        
        // Configure UI components
        configureUI();
    }
    
    /**
     * Set up data binding between UI components and ViewModel
     */
    private void setupDataBinding() {
        // Bind text properties
        if (welcomeLabel != null) {
            welcomeLabel.textProperty().bind(viewModel.welcomeMessageProperty());
        }
        
        if (versionLabel != null) {
            versionLabel.textProperty().bind(viewModel.versionInfoProperty());
        }
        
        // Bind button states
        if (singlePlayerButton != null) {
            singlePlayerButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.singlePlayerEnabledProperty().not()));
        }
        
        if (multiplayerButton != null) {
            multiplayerButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.multiplayerEnabledProperty().not()));
        }
        
        if (settingsButton != null) {
            settingsButton.disableProperty().bind(viewModel.isLoadingProperty().or(viewModel.settingsEnabledProperty().not()));
        }
        
        if (aboutButton != null) {
            aboutButton.disableProperty().bind(viewModel.isLoadingProperty());
        }
        
        if (exitButton != null) {
            exitButton.disableProperty().bind(viewModel.isLoadingProperty());
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
        if (singlePlayerButton != null) {
            singlePlayerButton.setTooltip(new Tooltip("Play against the computer"));
        }
        
        if (multiplayerButton != null) {
            multiplayerButton.setTooltip(new Tooltip("Play against other players online"));
        }
        
        if (settingsButton != null) {
            settingsButton.setTooltip(new Tooltip("Configure game settings and preferences"));
        }
        
        if (aboutButton != null) {
            aboutButton.setTooltip(new Tooltip("About Nexus Board"));
        }
        
        if (exitButton != null) {
            exitButton.setTooltip(new Tooltip("Exit the application"));
        }
    }
    
    /**
     * Setup hover effects for buttons
     */
    private void setupButtonHoverEffects() {
        // Single Player Button
        if (singlePlayerButton != null) {
            setupButtonHover(singlePlayerButton, "#27ae60", "#2ecc71");
        }
        
        // Multiplayer Button
        if (multiplayerButton != null) {
            setupButtonHover(multiplayerButton, "#3498db", "#5dade2");
        }
        
        // Settings Button
        if (settingsButton != null) {
            setupButtonHover(settingsButton, "#f39c12", "#f5b041");
        }
        
        // About Button
        if (aboutButton != null) {
            setupButtonHover(aboutButton, "#9b59b6", "#bb8fce");
        }
        
        // Exit Button
        if (exitButton != null) {
            setupButtonHover(exitButton, "#e74c3c", "#ec7063");
        }
    }
    
    /**
     * Setup hover effect for individual button
     */
    private void setupButtonHover(Button button, String normalColor, String hoverColor) {
        String normalStyle = button.getStyle().replace(normalColor, normalColor);
        String hoverStyle = button.getStyle().replace(normalColor, hoverColor);
        
        button.setOnMouseEntered(e -> button.setStyle(hoverStyle));
        button.setOnMouseExited(e -> button.setStyle(normalStyle));
    }
    
    // FXML Event Handlers
    
    @FXML
    private void onSinglePlayerClick() {
        viewModel.startSinglePlayerGame();
    }
    
    @FXML
    private void onMultiplayerClick() {
        viewModel.startMultiplayerGame();
    }
    
    @FXML
    private void onSettingsClick() {
        viewModel.openSettings();
    }
    
    @FXML
    private void onAboutClick() {
        viewModel.showAbout();
    }
    
    @FXML
    private void onExitClick() {
        viewModel.exitApplication();
    }
    
    /**
     * Create a basic fallback scene if FXML loading fails
     */
    private void createBasicScene() {
        VBox root = new VBox(20);
        root.setStyle("-fx-padding: 50; -fx-alignment: center; -fx-background-color: #ecf0f1;");
        
        Label titleLabel = new Label("Nexus Board");
        titleLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Button startButton = new Button("Start Game");
        startButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30;");
        startButton.setOnAction(e -> viewModel.startSinglePlayerGame());
        
        Button exitButton = new Button("Exit");
        exitButton.setStyle("-fx-font-size: 16px; -fx-padding: 15 30;");
        exitButton.setOnAction(e -> viewModel.exitApplication());
        
        root.getChildren().addAll(titleLabel, startButton, exitButton);
        
        scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Nexus Board - Chess Game");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    /**
     * Get the ViewModel for testing or external access
     */
    public LandingScreenViewModel getViewModel() {
        return viewModel;
    }
}
