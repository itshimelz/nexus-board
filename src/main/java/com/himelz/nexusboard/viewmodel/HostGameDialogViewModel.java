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
import com.himelz.nexusboard.network.Server;
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.viewController.GameScreen;

/**
 * ViewModel for the Host Game Dialog following MVVM pattern.
 * Handles server hosting logic and state management.
 */
public class HostGameDialogViewModel implements Server.ServerListener, Client.ClientListener {

    private final Stage primaryStage;
    
    // Server instance
    private Server gameServer;
    
    // Client instance for host to connect to their own server
    private Client hostClient;

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

        String name = playerName.get().trim();
        if (name.isEmpty()) {
            statusMessage.set("Please enter your player name");
            return;
        }

        try {
            int serverPort = Integer.parseInt(port.get().trim());
            if (serverPort < 1 || serverPort > 65535) {
                statusMessage.set("Port must be between 1 and 65535");
                return;
            }

            statusMessage.set("Starting server...");

            // Create and configure server
            gameServer = new Server(serverPort);
            gameServer.addServerListener(this);

            // Start server in background thread
            new Thread(() -> {
                boolean started = gameServer.start();
                Platform.runLater(() -> {
                    if (started) {
                        isServerRunning.set(true);
                        statusMessage.set("Server running - Connecting as host...");
                        System.out.println("Host player: " + name);
                        
                        // Connect the host as a client to their own server
                        connectHostAsClient(name, serverPort);
                    } else {
                        statusMessage.set("Failed to start server - Port may be in use");
                        gameServer = null;
                    }
                });
            }).start();

        } catch (NumberFormatException e) {
            statusMessage.set("Invalid port number");
        } catch (Exception e) {
            statusMessage.set("Error starting server: " + e.getMessage());
        }
    }

    /**
     * Stop the game server
     */
    public void stopServer() {
        if (!isServerRunning.get() || gameServer == null) {
            return;
        }

        statusMessage.set("Stopping server...");

        // Disconnect host client first
        if (hostClient != null && hostClient.isConnected()) {
            hostClient.disconnect();
            hostClient = null;
        }

        // Stop server in background thread
        new Thread(() -> {
            try {
                gameServer.stop();
                Platform.runLater(() -> {
                    gameServer = null;
                    isServerRunning.set(false);
                    statusMessage.set("Server stopped");
                    playerCount.set("Players connected: 0/2");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusMessage.set("Error stopping server: " + e.getMessage());
                });
            }
        }).start();
    }

    // ServerListener implementation methods
    
    @Override
    public void onServerStarted(int port) {
        Platform.runLater(() -> {
            statusMessage.set("Server started on port " + port + " - Waiting for players");
        });
    }
    
    @Override
    public void onServerStopped() {
        Platform.runLater(() -> {
            statusMessage.set("Server stopped");
            playerCount.set("Players connected: 0/2");
        });
    }
    
    @Override
    public void onPlayerConnected(String playerId, String playerName) {
        Platform.runLater(() -> {
            int currentCount = gameServer != null ? gameServer.getConnectedClientsCount() : 0;
            playerCount.set("Players connected: " + currentCount + "/2");
            
            if (currentCount == 1) {
                statusMessage.set("First player connected: " + playerName);
            } else if (currentCount == 2) {
                statusMessage.set("Game full - Starting match!");
                // TODO: Transition to game screen when both players are ready
            }
        });
    }
    
    @Override
    public void onPlayerDisconnected(String playerId) {
        Platform.runLater(() -> {
            int currentCount = gameServer != null ? gameServer.getConnectedClientsCount() : 0;
            playerCount.set("Players connected: " + currentCount + "/2");
            statusMessage.set("Player disconnected - Waiting for players");
        });
    }
    
    @Override
    public void onGameStateChanged(GameState gameState) {
        // Handle game state changes if needed
        Platform.runLater(() -> {
            System.out.println("Game state changed: " + gameState.getCurrentPlayer());
        });
    }
    
    @Override
    public void onError(String error) {
        Platform.runLater(() -> {
            System.err.println("Error: " + error);
            statusMessage.set("Error: " + error);
        });
    }
    
    @Override
    public void onMessageReceived(String playerId, String message) {
        // Handle messages received by server
        System.out.println("Message from " + playerId + ": " + message);
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
     * Connect the host as a client to their own server
     */
    private void connectHostAsClient(String hostName, int serverPort) {
        new Thread(() -> {
            try {
                // Give the server a moment to be ready
                Thread.sleep(500);
                
                // Create and connect host client
                hostClient = new Client();
                hostClient.addClientListener(this);
                
                // Connect to localhost with host player details
                String hostPlayerId = "host_" + System.currentTimeMillis();
                boolean connected = hostClient.connect("localhost", serverPort, hostPlayerId, hostName);
                
                Platform.runLater(() -> {
                    if (connected) {
                        statusMessage.set("Host connected - Waiting for opponent");
                        System.out.println("Host successfully connected as client");
                    } else {
                        statusMessage.set("Failed to connect host as client");
                        System.err.println("Host failed to connect as client");
                    }
                });
                
            } catch (Exception e) {
                Platform.runLater(() -> {
                    statusMessage.set("Error connecting host: " + e.getMessage());
                    System.err.println("Error connecting host as client: " + e.getMessage());
                });
            }
        }).start();
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
    
    // Client.ClientListener implementation (for host connecting to their own server)
    
    @Override
    public void onConnected() {
        Platform.runLater(() -> {
            System.out.println("Host connected to their own server as a client");
        });
    }
    
    @Override
    public void onDisconnected() {
        Platform.runLater(() -> {
            System.out.println("Host disconnected from their own server");
        });
    }
    
    @Override
    public void onJoinedGame(String role, String color) {
        Platform.runLater(() -> {
            System.out.println("Host joined game as " + role + " playing " + color);
        });
    }
    
    @Override
    public void onPlayerJoined(String playerId, String playerName) {
        Platform.runLater(() -> {
            System.out.println("Host client: Player joined - " + playerName);
        });
    }
    
    @Override
    public void onPlayerLeft(String playerId) {
        Platform.runLater(() -> {
            System.out.println("Host client: Player left - " + playerId);
        });
    }
    
    @Override
    public void onGameStarted() {
        Platform.runLater(() -> {
            statusMessage.set("Game is starting! Loading game screen...");
            System.out.println("Host: Game started! Transitioning to game screen...");
            
            try {
                // Get player information from client
                String playerId = hostClient.getPlayerId();
                String playerColorStr = hostClient.getPlayerColor();
                boolean isHost = true; // This is the host player
                
                // Convert string color to enum
                Color playerColor = "white".equalsIgnoreCase(playerColorStr) ? Color.WHITE : Color.BLACK;
                
                // Create and show game screen
                GameScreen gameScreen = new GameScreen(primaryStage, hostClient, gameServer, isHost, playerColor, playerId);
                gameScreen.show();
                
                System.out.println("Game screen opened for host player: " + playerId + " (Color: " + playerColor + ")");
                
            } catch (Exception e) {
                System.err.println("Failed to transition to game screen: " + e.getMessage());
                e.printStackTrace();
                statusMessage.set("Error loading game screen: " + e.getMessage());
            }
        });
    }
    
    @Override
    public void onGameStateUpdated(GameState gameState) {
        Platform.runLater(() -> {
            System.out.println("Host client: Game state updated - Turn: " + gameState.getCurrentPlayer());
            // Game state updates will be handled by the GameScreen
        });
    }
    
    @Override
    public void onMoveReceived(String playerId, Move move) {
        Platform.runLater(() -> {
            System.out.println("Host client: Move received from " + playerId + ": " + 
                move.getFrom().toString() + " -> " + move.getTo().toString());
            // Moves will be handled by the GameScreen
        });
    }
    
    @Override
    public void onChatReceived(String playerId, String playerName, String message) {
        Platform.runLater(() -> {
            System.out.println("Host client: Chat from " + playerName + ": " + message);
            // Chat will be handled by the GameScreen
        });
    }
    
    @Override
    public void onMessage(String message) {
        Platform.runLater(() -> {
            System.out.println("Host client message: " + message);
        });
    }
}
