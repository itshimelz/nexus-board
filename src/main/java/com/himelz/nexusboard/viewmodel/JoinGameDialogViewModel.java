package com.himelz.nexusboard.viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.application.Platform;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * ViewModel for the Join Game Dialog following MVVM pattern.
 * Handles client connection logic and state management.
 */
public class JoinGameDialogViewModel {
    
    private final Stage primaryStage;
    private static final String RECENT_CONNECTIONS_FILE = "recent_connections.txt";
    
    // Observable Properties for UI Binding
    private final StringProperty playerName;
    private final StringProperty serverIp;
    private final StringProperty port;
    private final StringProperty connectionString;
    private final StringProperty statusMessage;
    private final BooleanProperty isConnecting;
    private final BooleanProperty isConnected;
    private final ObservableList<String> recentConnections;
    
    public JoinGameDialogViewModel(Stage stage) {
        this.primaryStage = stage;
        
        // Initialize observable properties
        this.playerName = new SimpleStringProperty("");
        this.serverIp = new SimpleStringProperty("");
        this.port = new SimpleStringProperty("8888");
        this.connectionString = new SimpleStringProperty("");
        this.statusMessage = new SimpleStringProperty("Ready to connect");
        this.isConnecting = new SimpleBooleanProperty(false);
        this.isConnected = new SimpleBooleanProperty(false);
        this.recentConnections = FXCollections.observableArrayList();
    }
    
    // Property getters for UI binding
    public StringProperty playerNameProperty() {
        return playerName;
    }
    
    public StringProperty serverIpProperty() {
        return serverIp;
    }
    
    public StringProperty portProperty() {
        return port;
    }
    
    public StringProperty connectionStringProperty() {
        return connectionString;
    }
    
    public StringProperty statusMessageProperty() {
        return statusMessage;
    }
    
    public BooleanProperty isConnectingProperty() {
        return isConnecting;
    }
    
    public BooleanProperty isConnectedProperty() {
        return isConnected;
    }
    
    public ObservableList<String> getRecentConnections() {
        return recentConnections;
    }
    
    // Action methods
    
    /**
     * Connect to the game server
     */
    public void connectToServer() {
        if (isConnecting.get() || isConnected.get()) {
            return;
        }
        
        String ip = serverIp.get().trim();
        String portStr = port.get().trim();
        String name = playerName.get().trim();
        
        if (ip.isEmpty() || portStr.isEmpty() || name.isEmpty()) {
            statusMessage.set("Please fill in all fields");
            return;
        }
        
        // Validate IP format (basic validation)
        if (!isValidIP(ip)) {
            statusMessage.set("Invalid IP address format");
            return;
        }
        
        // Validate port
        try {
            int portNum = Integer.parseInt(portStr);
            if (portNum < 1 || portNum > 65535) {
                statusMessage.set("Port must be between 1 and 65535");
                return;
            }
        } catch (NumberFormatException e) {
            statusMessage.set("Invalid port number");
            return;
        }
        
        isConnecting.set(true);
        statusMessage.set("Connecting to " + ip + ":" + portStr + "...");
        
        // TODO: Implement actual client connection logic
        // For now, simulate the connection process
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Simulate connection time
                Platform.runLater(() -> {
                    // Simulate successful connection
                    isConnecting.set(false);
                    isConnected.set(true);
                    statusMessage.set("Connected successfully! Waiting for game to start...");
                    
                    // Add to recent connections
                    addToRecentConnections(ip + ":" + portStr);
                    
                    // TODO: Start actual client connection here
                    System.out.println("Connected to server at " + ip + ":" + portStr);
                    System.out.println("Player: " + name);
                    
                    // TODO: Transition to game screen when game starts
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    isConnecting.set(false);
                    statusMessage.set("Connection interrupted");
                });
            }
        }).start();
    }
    
    /**
     * Disconnect from the game server
     */
    public void disconnectFromServer() {
        if (!isConnected.get() && !isConnecting.get()) {
            return;
        }
        
        statusMessage.set("Disconnecting...");
        
        // TODO: Implement actual client disconnection logic
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Simulate disconnection time
                Platform.runLater(() -> {
                    isConnecting.set(false);
                    isConnected.set(false);
                    statusMessage.set("Disconnected from server");
                    
                    // TODO: Stop actual client connection here
                    System.out.println("Disconnected from server");
                });
            } catch (InterruptedException e) {
                Platform.runLater(() -> {
                    isConnecting.set(false);
                    isConnected.set(false);
                    statusMessage.set("Disconnection error");
                });
            }
        }).start();
    }
    
    /**
     * Parse connection string (IP:Port format)
     */
    public void parseConnectionString() {
        String connStr = connectionString.get().trim();
        if (connStr.isEmpty()) {
            statusMessage.set("Please enter a connection string");
            return;
        }
        
        try {
            if (connStr.contains(":")) {
                String[] parts = connStr.split(":");
                if (parts.length == 2) {
                    serverIp.set(parts[0].trim());
                    port.set(parts[1].trim());
                    statusMessage.set("Connection info parsed successfully");
                    connectionString.set(""); // Clear after parsing
                } else {
                    statusMessage.set("Invalid format. Use IP:Port (e.g., 192.168.1.100:8888)");
                }
            } else {
                // Assume it's just an IP, use default port
                serverIp.set(connStr);
                port.set("8888");
                statusMessage.set("IP set, using default port 8888");
                connectionString.set(""); // Clear after parsing
            }
        } catch (Exception e) {
            statusMessage.set("Error parsing connection string");
        }
    }
    
    /**
     * Select a recent connection from the list
     */
    public void selectRecentConnection(String connection) {
        if (connection == null || connection.isEmpty()) {
            return;
        }
        
        try {
            if (connection.contains(":")) {
                String[] parts = connection.split(":");
                if (parts.length == 2) {
                    serverIp.set(parts[0].trim());
                    port.set(parts[1].trim());
                    statusMessage.set("Recent connection selected: " + connection);
                }
            } else {
                serverIp.set(connection.trim());
                port.set("8888");
                statusMessage.set("Recent connection selected: " + connection);
            }
        } catch (Exception e) {
            statusMessage.set("Error selecting recent connection");
        }
    }
    
    /**
     * Show help information
     */
    public void showHelp() {
        // TODO: Implement help dialog
        System.out.println("Join Game Help:");
        System.out.println("1. Enter your player name");
        System.out.println("2. Get the server IP address from your friend");
        System.out.println("3. Enter the server IP and port (default 8888)");
        System.out.println("4. Click 'Connect' to join the game");
        System.out.println("5. You can also paste 'IP:Port' format in the connection field");
        
        statusMessage.set("Help information shown in console");
    }
    
    /**
     * Navigate back to multiplayer menu
     */
    public void backToMultiplayerMenu() {
        // Disconnect if connected
        if (isConnected.get() || isConnecting.get()) {
            disconnectFromServer();
        }
        
        try {
            // Navigate back to multiplayer menu
            com.himelz.nexusboard.viewController.MultiplayerMenu multiplayerMenu = 
                new com.himelz.nexusboard.viewController.MultiplayerMenu(primaryStage);
            multiplayerMenu.show();
        } catch (Exception e) {
            System.err.println("Failed to navigate back to multiplayer menu: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load recent connections from file
     */
    public void loadRecentConnections() {
        new Thread(() -> {
            try {
                Path filePath = Paths.get(RECENT_CONNECTIONS_FILE);
                if (Files.exists(filePath)) {
                    List<String> lines = Files.readAllLines(filePath);
                    Platform.runLater(() -> {
                        recentConnections.clear();
                        recentConnections.addAll(lines);
                    });
                }
            } catch (Exception e) {
                System.err.println("Error loading recent connections: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Add connection to recent connections list
     */
    private void addToRecentConnections(String connection) {
        if (connection == null || connection.isEmpty()) {
            return;
        }
        
        // Remove if already exists (to move to top)
        recentConnections.remove(connection);
        
        // Add to top of list
        recentConnections.add(0, connection);
        
        // Keep only last 10 connections
        while (recentConnections.size() > 10) {
            recentConnections.remove(recentConnections.size() - 1);
        }
        
        // Save to file
        saveRecentConnections();
    }
    
    /**
     * Save recent connections to file
     */
    private void saveRecentConnections() {
        new Thread(() -> {
            try {
                Path filePath = Paths.get(RECENT_CONNECTIONS_FILE);
                Files.write(filePath, recentConnections);
            } catch (Exception e) {
                System.err.println("Error saving recent connections: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Basic IP address validation
     */
    private boolean isValidIP(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // Simple regex for IPv4 validation
        String ipPattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        
        // Also allow localhost and local hostnames
        if (ip.equals("localhost") || ip.equals("127.0.0.1")) {
            return true;
        }
        
        return ip.matches(ipPattern);
    }
}
