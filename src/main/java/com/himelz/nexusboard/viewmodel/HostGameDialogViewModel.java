package com.himelz.nexusboard.viewmodel;

import javafx.beans.property.*;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import com.himelz.nexusboard.viewController.MultiplayerMenu;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/**
 * ViewModel for the Host Game Dialog following MVVM pattern.
 * Handles server hosting logic and state management.
 */
public class HostGameDialogViewModel {

    private final Stage primaryStage;

    // Observable Properties for UI Binding
    private final StringProperty playerName;
    private final StringProperty ipAddress;
    private final StringProperty port;
    private final StringProperty statusMessage;
    private final StringProperty playerCount;
    private final BooleanProperty isServerRunning;

    public HostGameDialogViewModel(Stage stage) {
        this.primaryStage = stage;

        // Initialize observable properties
        this.playerName = new SimpleStringProperty("");
        this.ipAddress = new SimpleStringProperty("Loading...");
        this.port = new SimpleStringProperty("8888");
        this.statusMessage = new SimpleStringProperty("Ready to start server");
        this.playerCount = new SimpleStringProperty("Players connected: 0/2");
        this.isServerRunning = new SimpleBooleanProperty(false);
    }

    // Property getters for UI binding
    public StringProperty playerNameProperty() {
        return playerName;
    }

    public StringProperty ipAddressProperty() {
        return ipAddress;
    }

    public StringProperty portProperty() {
        return port;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public StringProperty playerCountProperty() {
        return playerCount;
    }

    public BooleanProperty isServerRunningProperty() {
        return isServerRunning;
    }

    // Action methods

    /**
     * Initialize network information (get local IP address)
     */
    public void initializeNetworkInfo() {
        new Thread(() -> {
            try {
                String localIP = getLocalIPAddress();
                Platform.runLater(() -> {
                    ipAddress.set(localIP);
                    if (localIP.equals("Unable to determine IP")) {
                        statusMessage.set("Warning: Could not determine IP address");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    ipAddress.set("Error getting IP");
                    statusMessage.set("Error: Could not get network information");
                });
            }
        }).start();
    }

    /**
     * Start the game server
     */
    public void startServer() {
        if (isServerRunning.get()) {
            return;
        }

        statusMessage.set("Starting server...");

        // TODO: Implement actual server starting logic
        // For now, simulate the server starting process
        new Thread(() -> {
            try {
                Thread.sleep(2000); // Simulate server startup time
                Platform.runLater(() -> {
                    isServerRunning.set(true);
                    statusMessage.set("Server running - Waiting for players");
                    playerCount.set("Players connected: 0/2");

                    // TODO: Start actual server here
                    System.out.println("Server started on " + ipAddress.get() + ":" + port.get());
                    System.out.println("Player: " + playerName.get());
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusMessage.set("Failed to start server");
                });
            }
        }).start();
    }

    /**
     * Stop the game server
     */
    public void stopServer() {
        if (!isServerRunning.get()) {
            return;
        }

        statusMessage.set("Stopping server...");

        // TODO: Implement actual server stopping logic
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate server shutdown time
                Platform.runLater(() -> {
                    isServerRunning.set(false);
                    statusMessage.set("Server stopped");
                    playerCount.set("Players connected: 0/2");

                    // TODO: Stop actual server here
                    System.out.println("Server stopped");
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    statusMessage.set("Error stopping server");
                });
            }
        }).start();
    }

    /**
     * Copy IP address to clipboard
     */
    public void copyIpToClipboard() {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(ipAddress.get() + ":" + port.get());
            clipboard.setContent(content);

            statusMessage.set("Connection info copied to clipboard!");

            // Reset status message after 3 seconds
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    Platform.runLater(() -> {
                        if (isServerRunning.get()) {
                            statusMessage.set("Server running - Waiting for players");
                        } else {
                            statusMessage.set("Ready to start server");
                        }
                    });
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();

        } catch (Exception e) {
            statusMessage.set("Failed to copy to clipboard");
            System.err.println("Failed to copy to clipboard: " + e.getMessage());
        }
    }

    /**
     * Show help information
     */
    public void showHelp() {
        // TODO: Implement help dialog
        System.out.println("Host Game Help:");
        System.out.println("1. Enter your player name");
        System.out.println("2. Click 'Start Server' to begin hosting");
        System.out.println("3. Share your IP address (" + ipAddress.get() + ":" + port.get() + ") with friends");
        System.out.println("4. Wait for an opponent to connect");

        statusMessage.set("Help information shown in console");
    }

    /**
     * Navigate back to multiplayer menu
     */
    public void backToMultiplayerMenu() {
        // Stop server if running
        if (isServerRunning.get()) {
            stopServer();
        }

        try {
            // Navigate back to multiplayer menu
            MultiplayerMenu multiplayerMenu = new MultiplayerMenu(primaryStage);
            multiplayerMenu.show();
        } catch (Exception e) {
            System.err.println("Failed to navigate back to multiplayer menu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get the local IP address
     */
    private String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // Skip loopback and inactive interfaces
                if (networkInterface.isLoopback() || !networkInterface.isUp()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    // We want IPv4 addresses that are not loopback
                    if (!address.isLoopbackAddress() && !address.isLinkLocalAddress() &&
                            address.getHostAddress().contains(".")) {
                        return address.getHostAddress();
                    }
                }
            }

            // Fallback to localhost
            return InetAddress.getLocalHost().getHostAddress();

        } catch (Exception e) {
            System.err.println("Error getting local IP address: " + e.getMessage());
            return "Unable to determine IP";
        }
    }

    /**
     * Update player count (called by server when players connect/disconnect)
     */
    public void updatePlayerCount(int connectedPlayers) {
        Platform.runLater(() -> {
            playerCount.set("Players connected: " + connectedPlayers + "/2");

            if (connectedPlayers == 2) {
                statusMessage.set("Game ready - Starting match!");
                // TODO: Transition to game screen
            } else if (connectedPlayers == 1) {
                statusMessage.set("Server running - Waiting for one more player");
            } else {
                statusMessage.set("Server running - Waiting for players");
            }
        });
    }
}
