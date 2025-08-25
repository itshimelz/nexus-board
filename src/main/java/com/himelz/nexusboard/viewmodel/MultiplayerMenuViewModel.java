package com.himelz.nexusboard.viewmodel;

import javafx.beans.property.*;
import javafx.stage.Stage;

/**
 * ViewModel for the Multiplayer Menu following MVVM pattern.
 * Handles multiplayer navigation logic and state management.
 */
public class MultiplayerMenuViewModel {
    
    private final Stage primaryStage;
    
    // Observable Properties for UI Binding
    private final StringProperty gameTitleText;
    private final StringProperty connectionStatusText;
    private final StringProperty networkStatusText;
    private final StringProperty loadingMessageText;
    private final BooleanProperty isLoading;
    private final BooleanProperty hostGameEnabled;
    private final BooleanProperty joinGameEnabled;
    private final BooleanProperty directConnectEnabled;
    
    public MultiplayerMenuViewModel(Stage stage) {
        this.primaryStage = stage;
        
        // Initialize observable properties
        this.gameTitleText = new SimpleStringProperty("NEXUS BOARD");
        this.connectionStatusText = new SimpleStringProperty("Ready to connect");
        this.networkStatusText = new SimpleStringProperty("Network: Ready");
        this.loadingMessageText = new SimpleStringProperty("Connecting...");
        this.isLoading = new SimpleBooleanProperty(false);
        this.hostGameEnabled = new SimpleBooleanProperty(true);
        this.joinGameEnabled = new SimpleBooleanProperty(true);
        this.directConnectEnabled = new SimpleBooleanProperty(true);
    }
    
    // Property getters for UI binding
    public StringProperty gameTitleProperty() {
        return gameTitleText;
    }
    
    public StringProperty connectionStatusProperty() {
        return connectionStatusText;
    }
    
    public StringProperty networkStatusProperty() {
        return networkStatusText;
    }
    
    public StringProperty loadingMessageProperty() {
        return loadingMessageText;
    }
    
    public BooleanProperty isLoadingProperty() {
        return isLoading;
    }
    
    public BooleanProperty hostGameEnabledProperty() {
        return hostGameEnabled;
    }
    
    public BooleanProperty joinGameEnabledProperty() {
        return joinGameEnabled;
    }
    
    public BooleanProperty directConnectEnabledProperty() {
        return directConnectEnabled;
    }
    
    // Action methods
    
    /**
     * Handle host game action
     */
    public void hostGame() {
        setLoading(true);
        loadingMessageText.set("Opening host game dialog...");
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Brief loading for smooth transition
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    try {
                        // Open HostGameDialog
                        com.himelz.nexusboard.viewController.HostGameDialog hostDialog = 
                            new com.himelz.nexusboard.viewController.HostGameDialog(primaryStage);
                        hostDialog.show();
                    } catch (Exception e) {
                        connectionStatusText.set("Failed to open host dialog");
                        System.err.println("Failed to open host game dialog: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }
    
    /**
     * Handle join game action
     */
    public void joinGame() {
        setLoading(true);
        loadingMessageText.set("Opening join game dialog...");
        
        new Thread(() -> {
            try {
                Thread.sleep(500); // Brief loading for smooth transition
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    try {
                        // Open JoinGameDialog
                        com.himelz.nexusboard.viewController.JoinGameDialog joinDialog = 
                            new com.himelz.nexusboard.viewController.JoinGameDialog(primaryStage);
                        joinDialog.show();
                    } catch (Exception e) {
                        connectionStatusText.set("Failed to open join dialog");
                        System.err.println("Failed to open join game dialog: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }
    
    /**
     * Handle direct connect action
     */
    public void directConnect() {
        setLoading(true);
        loadingMessageText.set("Preparing quick connect...");
        
        // TODO: Implement direct connect dialog
        // For now, just simulate the action
        new Thread(() -> {
            try {
                Thread.sleep(800); // Simulate processing
                javafx.application.Platform.runLater(() -> {
                    setLoading(false);
                    connectionStatusText.set("Opening quick connect...");
                    // TODO: Open quick connect dialog
                    System.out.println("Direct Connect clicked - Dialog will be implemented next");
                });
            } catch (InterruptedException e) {
                javafx.application.Platform.runLater(() -> setLoading(false));
            }
        }).start();
    }
    
    /**
     * Handle back to main menu action
     */
    public void backToMainMenu() {
        try {
            // Navigate back to landing page
            com.himelz.nexusboard.viewController.LandingPage landingPage = 
                new com.himelz.nexusboard.viewController.LandingPage(primaryStage);
            landingPage.show();
        } catch (Exception e) {
            System.err.println("Failed to navigate back to main menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Set loading state
     */
    private void setLoading(boolean loading) {
        isLoading.set(loading);
        if (!loading) {
            connectionStatusText.set("Ready to connect");
        }
    }
    
    /**
     * Update network status
     */
    public void updateNetworkStatus(String status) {
        networkStatusText.set("Network: " + status);
    }
    
    /**
     * Update connection status
     */
    public void updateConnectionStatus(String status) {
        connectionStatusText.set(status);
    }
}
