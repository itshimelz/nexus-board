package com.himelz.nexusboard.viewmodel;

import javafx.beans.property.*;
import javafx.stage.Stage;
import com.himelz.nexusboard.viewController.GameScreen;

/**
 * ViewModel for the Landing Page following MVVM pattern.
 * Handles navigation logic, game mode selection, and observable properties.
 */
public class LandingScreenViewModel {
    
    // Observable properties for UI binding
    private final StringProperty welcomeMessage;
    private final StringProperty versionInfo;
    private final BooleanProperty isLoading;
    private final BooleanProperty singlePlayerEnabled;
    private final BooleanProperty multiplayerEnabled;
    private final BooleanProperty settingsEnabled;
    
    // Navigation reference
    private Stage primaryStage;
    
    public LandingScreenViewModel(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Initialize observable properties
        this.welcomeMessage = new SimpleStringProperty("Welcome to Nexus Board");
        this.versionInfo = new SimpleStringProperty("Version 1.0.0");
        this.isLoading = new SimpleBooleanProperty(false);
        this.singlePlayerEnabled = new SimpleBooleanProperty(true);
        this.multiplayerEnabled = new SimpleBooleanProperty(false);
        this.settingsEnabled = new SimpleBooleanProperty(true);
    }
    
    // Property getters for UI binding
    public StringProperty welcomeMessageProperty() {
        return welcomeMessage;
    }
    
    public StringProperty versionInfoProperty() {
        return versionInfo;
    }
    
    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }
    
    public BooleanProperty singlePlayerEnabledProperty() {
        return singlePlayerEnabled;
    }
    
    public BooleanProperty multiplayerEnabledProperty() {
        return multiplayerEnabled;
    }
    
    public BooleanProperty settingsEnabledProperty() {
        return settingsEnabled;
    }
    
    // Value getters
    public String getWelcomeMessage() {
        return welcomeMessage.get();
    }
    
    public String getVersionInfo() {
        return versionInfo.get();
    }
    
    public boolean isLoading() {
        return isLoading.get();
    }
    
    public boolean isSinglePlayerEnabled() {
        return singlePlayerEnabled.get();
    }
    
    public boolean isMultiplayerEnabled() {
        return multiplayerEnabled.get();
    }
    
    public boolean isSettingsEnabled() {
        return settingsEnabled.get();
    }
    
    // Command methods for UI actions
    
    /**
     * Handle single player game start
     */
    public void startSinglePlayerGame() {
        if (!isLoading.get()) {
            isLoading.set(true);
            
            try {
                // Navigate to Game Screen for single player game
                GameScreen gameScreen = new GameScreen(primaryStage);
                gameScreen.show();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error - could show error dialog
            } finally {
                isLoading.set(false);
            }
        }
    }
    
    /**
     * Handle multiplayer game options
     */
    public void startMultiplayerGame() {
        if (!isLoading.get()) {
            isLoading.set(true);
            
            try {
                // For now, navigate to Game Screen
                // TODO: Implement multiplayer lobby/connection screen
                GameScreen gameScreen = new GameScreen(primaryStage);
                gameScreen.show();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle error
            } finally {
                isLoading.set(false);
            }
        }
    }
    
    /**
     * Handle settings/preferences
     */
    public void openSettings() {
        // TODO: Implement settings screen
        System.out.println("Opening settings...");
    }
    
    /**
     * Handle application exit
     */
    public void exitApplication() {
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
    
    /**
     * Handle help/about information
     */
    public void showAbout() {
        // TODO: Implement about dialog
        System.out.println("Showing about information...");
    }
    
    /**
     * Initialize any required data or settings
     */
    public void initialize() {
        // Initialize welcome message
        welcomeMessage.set("Welcome to Nexus Board");
        versionInfo.set("Version 1.0.0 - Chess Game");
        
        // Set initial button states
        singlePlayerEnabled.set(true);
        multiplayerEnabled.set(true);
        settingsEnabled.set(true);
        isLoading.set(false);
    }
}
