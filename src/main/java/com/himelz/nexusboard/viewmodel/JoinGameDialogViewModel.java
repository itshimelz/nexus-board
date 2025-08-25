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
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;

/**
 * ViewModel for the Join Game Dialog following MVVM pattern.
 * Handles client connection logic and state management.
 */
public class JoinGameDialogViewModel implements Client.ClientListener {
    
    private final Stage primaryStage;
    private static final String RECENT_CONNECTIONS_FILE = "recent_connections.txt";
    
    // Client instance
    private Client gameClient;
    
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

    // ============ ClientListener Implementation ============
    
    @Override
    public void onConnected() {
        Platform.runLater(() -> {
            isConnecting.set(false);
            isConnected.set(true);
            statusMessage.set("Connected to server! Waiting for game to start...");
            System.out.println("Successfully connected to server");
        });
    }

    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            isConnecting.set(false);
            isConnected.set(false);
            statusMessage.set("Disconnected from server");
            gameClient = null;
            System.out.println("Disconnected from server");
        });
    }

    @Override
    public void onJoinedGame(String gameId, String playerId) {
        Platform.runLater(() -> {
            statusMessage.set("Joined game: " + gameId + " as " + playerId);
            System.out.println("Joined game: " + gameId + " as player: " + playerId);
        });
    }

    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        Platform.runLater(() -> {
            statusMessage.set("Player joined: " + playerName + " (" + playerId + ")");
            System.out.println("Player joined: " + playerName + " (ID: " + playerId + ")");
        });
    }

    @Override
    public void onPlayerLeft(String playerId) {
        Platform.runLater(() -> {
            statusMessage.set("Player left: " + playerId);
            System.out.println("Player left: " + playerId);
        });
    }

    @Override
    public void onGameStarted() {
        Platform.runLater(() -> {
            statusMessage.set("Game is starting! Loading game screen...");
            System.out.println("Game started! Transitioning to game screen...");
            
            // TODO: Transition to game screen
            // This should close the join dialog and open the game screen
        });
    }

    @Override
    public void onGameStateUpdated(GameState gameState) {
        Platform.runLater(() -> {
            System.out.println("Game state updated - Turn: " + gameState.getCurrentPlayer());
            // Game state updates will be handled by the GameScreen
        });
    }

    @Override
    public void onMoveReceived(String playerId, Move move) {
        Platform.runLater(() -> {
            System.out.println("Move received from " + playerId + ": " + 
                move.getFrom().toString() + " -> " + move.getTo().toString());
            // Moves will be handled by the GameScreen
        });
    }

    @Override
    public void onChatReceived(String playerId, String playerName, String message) {
        Platform.runLater(() -> {
            System.out.println("Chat from " + playerName + " (" + playerId + "): " + message);
            // Chat will be handled by the GameScreen
        });
    }

    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            isConnecting.set(false);
            isConnected.set(false);
            statusMessage.set("Error: " + error);
            System.err.println("Client error: " + error);
        });
    }

    @Override
    public void onMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("Server message: " + message);
            // General messages can be displayed in status or logged
            statusMessage.set(message);
        });
    }

    // ============ Client Management ============
    
    /**
     * Get the active client instance for use by other components
     * @return Active Client instance, or null if not connected
     */
    public Client getGameClient() {
        return gameClient;
    }

    /**
     * Check if client is currently connected to a server
     * @return true if connected, false otherwise
     */
    public boolean hasActiveConnection() {
        return gameClient != null && isConnected.get();
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
        int portNum;
        try {
            portNum = Integer.parseInt(portStr);
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
        
        // Create and configure client
        gameClient = new Client();
        gameClient.addClientListener(this);
        
        // Connect to server in background thread
        new Thread(() -> {
            String playerId = "Player_" + System.currentTimeMillis();
            boolean connected = gameClient.connect(ip, portNum, playerId, name);
            
            Platform.runLater(() -> {
                if (connected) {
                    // Add to recent connections
                    addToRecentConnections(ip + ":" + portStr);
                    
                    System.out.println("Attempting to connect to server at " + ip + ":" + portStr);
                    System.out.println("Player: " + name + " (ID: " + playerId + ")");
                } else {
                    isConnecting.set(false);
                    statusMessage.set("Failed to connect to server");
                    gameClient = null;
                }
            });
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

        // Disconnect from server in background thread
        new Thread(() -> {
            if (gameClient != null) {
                gameClient.disconnect();
                gameClient = null;
            }
            
            Platform.runLater(() -> {
                isConnecting.set(false);
                isConnected.set(false);
                statusMessage.set("Disconnected from server");
            });
        }).start();
    }    /**
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
