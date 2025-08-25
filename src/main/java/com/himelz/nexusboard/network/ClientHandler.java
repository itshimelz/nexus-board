package com.himelz.nexusboard.network;

import com.himelz.nexusboard.model.board.Position;

import java.io.*;
import java.net.*;
import java.util.UUID;

/**
 * Handles individual client connections on the server side.
 * Manages communication between server and a specific client.
 */
public class ClientHandler implements Runnable {
    
    private final Server server;
    private final Socket clientSocket;
    private final String clientId;
    
    // Communication streams
    private BufferedReader input;
    private PrintWriter output;
    
    // Client state
    private String playerId;
    private String playerName;
    private boolean isConnected;
    private final Object connectionLock = new Object();
    
    /**
     * Creates a new client handler
     */
    public ClientHandler(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
        this.clientId = UUID.randomUUID().toString();
        this.isConnected = false;
    }
    
    /**
     * Main client handling loop
     */
    @Override
    public void run() {
        try {
            // Initialize communication streams
            input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            output = new PrintWriter(clientSocket.getOutputStream(), true);
            
            synchronized (connectionLock) {
                isConnected = true;
            }
            
            System.out.println("Client handler started for: " + clientSocket.getInetAddress());
            
            // Send welcome message
            sendMessage(createWelcomeMessage());
            
            // Main message processing loop
            String message;
            while (isConnected && (message = input.readLine()) != null) {
                try {
                    processMessage(message.trim());
                } catch (Exception e) {
                    System.err.println("Error processing message from client " + clientId + ": " + e.getMessage());
                    sendMessage(createErrorMessage("Error processing message: " + e.getMessage()));
                }
            }
            
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("IO Error with client " + clientId + ": " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Processes incoming messages from the client
     */
    private void processMessage(String message) {
        if (message.isEmpty()) {
            return;
        }
        
        System.out.println("Received from " + clientId + ": " + message);
        
        try {
            // Simple JSON parsing (would use Gson in production)
            if (message.startsWith("{") && message.endsWith("}")) {
                String type = extractJsonValue(message, "type");
                
                switch (type) {
                    case "JOIN_GAME" -> handleJoinGame(message);
                    case "MOVE" -> handleMove(message);
                    case "NEW_GAME" -> handleNewGame();
                    case "RESIGN" -> handleResign();
                    case "DRAW_OFFER" -> handleDrawOffer();
                    case "CHAT" -> handleChat(message);
                    case "PING" -> handlePing();
                    case "DISCONNECT" -> handleDisconnect();
                    default -> sendMessage(createErrorMessage("Unknown message type: " + type));
                }
            } else {
                sendMessage(createErrorMessage("Invalid message format"));
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
            sendMessage(createErrorMessage("Invalid message format"));
        }
    }
    
    /**
     * Handles join game request
     */
    private void handleJoinGame(String message) {
        try {
            this.playerId = extractJsonValue(message, "playerId");
            this.playerName = extractJsonValue(message, "playerName");
            
            if (playerId == null || playerId.isEmpty()) {
                this.playerId = "Player_" + clientId.substring(0, 8);
            }
            
            if (playerName == null || playerName.isEmpty()) {
                this.playerName = "Anonymous";
            }
            
            // Notify server about player joining
            server.handlePlayerJoined(this, playerId, playerName);
            
        } catch (Exception e) {
            sendMessage(createErrorMessage("Invalid join game message"));
        }
    }
    
    /**
     * Handles move request
     */
    private void handleMove(String message) {
        System.out.println("DEBUG: ClientHandler handling move message: " + message);
        System.out.println("DEBUG: ClientHandler playerId: " + playerId);
        
        try {
            String fromStr = extractJsonValue(message, "from");
            String toStr = extractJsonValue(message, "to");
            
            if (fromStr == null || toStr == null) {
                sendMessage(createErrorMessage("Invalid move format"));
                return;
            }
            
            // Parse positions
            Position from = parsePosition(fromStr);
            Position to = parsePosition(toStr);
            
            if (from == null || to == null) {
                sendMessage(createErrorMessage("Invalid position format"));
                return;
            }
            
            System.out.println("DEBUG: Parsed move from " + fromStr + " (" + from + ") to " + toStr + " (" + to + ")");
            
            // Forward to server with positions - let server create proper Move object
            // Server has access to game state and can determine the moving piece
            server.handleMove(playerId, from, to);
            
        } catch (Exception e) {
            sendMessage(createErrorMessage("Error processing move"));
        }
    }
    
    /**
     * Handles chat message
     */
    private void handleChat(String message) {
        try {
            String chatMessage = extractJsonValue(message, "message");
            if (chatMessage != null && !chatMessage.isEmpty()) {
                // Broadcast chat message to all clients
                String broadcastMessage = createChatMessage(playerId, playerName, chatMessage);
                server.broadcastMessage(broadcastMessage);
            }
        } catch (Exception e) {
            sendMessage(createErrorMessage("Error processing chat message"));
        }
    }
    
    /**
     * Handles ping request
     */
    private void handlePing() {
        sendMessage(createPongMessage());
    }
    
    /**
     * Handles disconnect request
     */
    private void handleDisconnect() {
        disconnect();
    }

    private void handleNewGame() {
        server.handleNewGameRequest(playerId);
    }

    private void handleResign() {
        server.handleResign(playerId);
    }

    private void handleDrawOffer() {
        server.handleDrawOffer(playerId);
    }
    
    /**
     * Sends a message to the client
     */
    public void sendMessage(String message) {
        synchronized (connectionLock) {
            if (isConnected && output != null) {
                try {
                    output.println(message);
                    output.flush();
                    System.out.println("Sent to " + clientId + ": " + message);
                } catch (Exception e) {
                    System.err.println("Error sending message to client " + clientId + ": " + e.getMessage());
                    disconnect();
                }
            }
        }
    }
    
    /**
     * Disconnects the client
     */
    public void disconnect() {
        synchronized (connectionLock) {
            if (!isConnected) {
                return;
            }
            
            isConnected = false;
        }
        
        // Notify server about player leaving
        if (playerId != null) {
            server.handlePlayerLeft(this, playerId);
        }
        
        // Close streams and socket
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing input stream: " + e.getMessage());
        }
        
        try {
            if (output != null) {
                output.close();
            }
        } catch (Exception e) {
            System.err.println("Error closing output stream: " + e.getMessage());
        }
        
        try {
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }
        
        System.out.println("Client disconnected: " + clientId);
    }
    
    // Utility methods for JSON handling (simple implementation)
    
    /**
     * Extracts a value from a simple JSON string
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) {
            return null;
        }
        
        start += pattern.length();
        int end = json.indexOf("\"", start);
        if (end == -1) {
            return null;
        }
        
        return json.substring(start, end);
    }
    
    /**
     * Parses a position string (e.g., "e2") to Position using canonical mapping.
     * Position rows are 0 at the top (black back rank) and 7 at the bottom.
     */
    private Position parsePosition(String posStr) {
        if (posStr == null) {
            return null;
        }
        try {
            // Use the centralized converter to avoid off-by-8 errors
            return Position.fromAlgebraic(posStr.toLowerCase());
        } catch (Exception e) {
            return null;
        }
    }
    
    // Message creation methods
    
    private String createWelcomeMessage() {
        return "{\"type\":\"WELCOME\",\"clientId\":\"" + clientId + "\"}";
    }
    
    private String createChatMessage(String playerId, String playerName, String message) {
        return "{\"type\":\"CHAT\",\"playerId\":\"" + playerId + 
               "\",\"playerName\":\"" + playerName + "\",\"message\":\"" + message + "\"}";
    }
    
    private String createPongMessage() {
        return "{\"type\":\"PONG\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
    
    private String createErrorMessage(String error) {
        return "{\"type\":\"ERROR\",\"message\":\"" + error + "\"}";
    }

    // (no-op) message helper removed
    
    // Getters
    
    public String getClientId() {
        return clientId;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public boolean isConnected() {
        synchronized (connectionLock) {
            return isConnected;
        }
    }
    
    public InetAddress getClientAddress() {
        return clientSocket.getInetAddress();
    }
}
