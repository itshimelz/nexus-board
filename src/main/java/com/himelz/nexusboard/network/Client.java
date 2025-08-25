package com.himelz.nexusboard.network;

import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client implementation for connecting to multiplayer chess games.
 * Handles server communication, game state synchronization, and message processing.
 */
public class Client {
    
    // Connection configuration
    private String serverHost;
    private int serverPort;
    private Socket socket;
    
    // Communication streams
    private BufferedReader input;
    private PrintWriter output;
    
    // Client state
    private boolean isConnected;
    private boolean isJoined;
    private String playerId;
    private String playerName;
    private String playerRole; // "host" or "guest"
    private String playerColor; // "white" or "black"
    
    // Threading
    private ExecutorService executorService;
    private Thread messageReaderThread;
    private final Object connectionLock = new Object();
    
    // Game state
    private GameState gameState;
    private final Object gameStateLock = new Object();
    
    // Client listeners
    private final List<ClientListener> listeners;
    
    /**
     * Interface for client event listeners
     */
    public interface ClientListener {
        void onConnected();
        void onDisconnected();
        void onJoinedGame(String role, String color);
        void onPlayerJoined(String playerId, String playerName);
        void onPlayerLeft(String playerId);
        void onGameStarted();
        void onGameStateUpdated(GameState gameState);
        void onMoveReceived(String playerId, Move move);
        void onChatReceived(String playerId, String playerName, String message);
        void onError(String error);
        void onMessage(String message);
    }
    
    /**
     * Creates a new client instance
     */
    public Client() {
        this.isConnected = false;
        this.isJoined = false;
        this.listeners = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
    }
    
    /**
     * Connects to the game server
     */
    public boolean connect(String host, int port, String playerId, String playerName) {
        synchronized (connectionLock) {
            if (isConnected) {
                notifyError("Already connected to server");
                return false;
            }
        }
        
        this.serverHost = host;
        this.serverPort = port;
        this.playerId = playerId;
        this.playerName = playerName;
        
        try {
            // Establish socket connection
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000); // 5 second timeout
            
            // Initialize communication streams
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            
            synchronized (connectionLock) {
                isConnected = true;
            }
            
            // Start message reader thread
            messageReaderThread = new Thread(this::messageReaderLoop, "Chess-Client-Reader");
            messageReaderThread.setDaemon(true);
            messageReaderThread.start();
            
            notifyConnected();
            System.out.println("Connected to server at " + host + ":" + port);
            
            // Send join game request
            sendJoinGameRequest();
            
            return true;
            
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
            notifyError("Failed to connect to server: " + e.getMessage());
            cleanup();
            return false;
        }
    }
    
    /**
     * Disconnects from the server
     */
    public void disconnect() {
        synchronized (connectionLock) {
            if (!isConnected) {
                return;
            }
            
            isConnected = false;
        }
        
        // Send disconnect message
        if (output != null) {
            try {
                sendMessage(createDisconnectMessage());
                Thread.sleep(100); // Give time for message to be sent
            } catch (Exception e) {
                // Ignore errors during disconnect
            }
        }
        
        cleanup();
        notifyDisconnected();
        System.out.println("Disconnected from server");
    }
    
    /**
     * Sends a move to the server
     */
    public void sendMove(Move move) {
        if (!isConnected || !isJoined) {
            notifyError("Not connected or joined to game");
            return;
        }
        
        String moveMessage = createMoveMessage(move);
        sendMessage(moveMessage);
    }
    
    /**
     * Sends a chat message
     */
    public void sendChatMessage(String message) {
        if (!isConnected || !isJoined) {
            notifyError("Not connected or joined to game");
            return;
        }
        
        String chatMessage = createChatMessage(message);
        sendMessage(chatMessage);
    }
    
    /**
     * Sends a ping to the server
     */
    public void sendPing() {
        if (!isConnected) {
            return;
        }
        
        sendMessage(createPingMessage());
    }
    
    /**
     * Main message reading loop
     */
    private void messageReaderLoop() {
        System.out.println("Message reader started");
        
        try {
            String message;
            while (isConnected && (message = input.readLine()) != null) {
                try {
                    processMessage(message.trim());
                } catch (Exception e) {
                    System.err.println("Error processing message: " + e.getMessage());
                    notifyError("Error processing message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            if (isConnected) {
                System.err.println("Connection lost: " + e.getMessage());
                notifyError("Connection lost: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }
    
    /**
     * Processes incoming messages from the server
     */
    private void processMessage(String message) {
        if (message.isEmpty()) {
            return;
        }
        
        System.out.println("Received: " + message);
        notifyMessage(message);
        
        try {
            // Simple JSON parsing
            if (message.startsWith("{") && message.endsWith("}")) {
                String type = extractJsonValue(message, "type");
                
                switch (type) {
                    case "WELCOME" -> handleWelcome(message);
                    case "PLAYER_ASSIGNED" -> handlePlayerAssigned(message);
                    case "PLAYER_JOINED" -> handlePlayerJoined(message);
                    case "PLAYER_LEFT" -> handlePlayerLeft(message);
                    case "GAME_START" -> handleGameStart(message);
                    case "GAME_STATE" -> handleGameState(message);
                    case "MOVE" -> handleMove(message);
                    case "CHAT" -> handleChat(message);
                    case "PONG" -> handlePong(message);
                    case "ERROR" -> handleError(message);
                    default -> System.out.println("Unknown message type: " + type);
                }
            } else {
                System.out.println("Invalid message format: " + message);
            }
            
        } catch (Exception e) {
            System.err.println("Error parsing message: " + e.getMessage());
        }
    }
    
    /**
     * Handles welcome message from server
     */
    private void handleWelcome(String message) {
        System.out.println("Received welcome from server");
    }
    
    /**
     * Handles player role assignment
     */
    private void handlePlayerAssigned(String message) {
        try {
            this.playerRole = extractJsonValue(message, "role");
            this.playerColor = extractJsonValue(message, "color");
            this.isJoined = true;
            
            notifyJoinedGame(playerRole, playerColor);
            System.out.println("Assigned as " + playerRole + " playing " + playerColor);
            
        } catch (Exception e) {
            notifyError("Error processing player assignment");
        }
    }
    
    /**
     * Handles another player joining
     */
    private void handlePlayerJoined(String message) {
        try {
            String joinedPlayerId = extractJsonValue(message, "playerId");
            String joinedPlayerName = extractJsonValue(message, "playerName");
            
            notifyPlayerJoined(joinedPlayerId, joinedPlayerName);
            System.out.println("Player joined: " + joinedPlayerName);
            
        } catch (Exception e) {
            notifyError("Error processing player joined message");
        }
    }
    
    /**
     * Handles player leaving
     */
    private void handlePlayerLeft(String message) {
        try {
            String leftPlayerId = extractJsonValue(message, "playerId");
            
            notifyPlayerLeft(leftPlayerId);
            System.out.println("Player left: " + leftPlayerId);
            
        } catch (Exception e) {
            notifyError("Error processing player left message");
        }
    }
    
    /**
     * Handles game start
     */
    private void handleGameStart(String message) {
        synchronized (gameStateLock) {
            gameState = new GameState();
        }
        
        notifyGameStarted();
        System.out.println("Game started!");
    }
    
    /**
     * Handles game state update
     */
    private void handleGameState(String message) {
        try {
            String stateData = extractJsonValue(message, "state");
            
            synchronized (gameStateLock) {
                // For now, we'll create a new game state
                // In production, this would parse the FEN string from stateData
                if (gameState == null) {
                    gameState = new GameState();
                }
                // TODO: Parse FEN string from stateData and update gameState
                System.out.println("Game state data: " + stateData);
            }
            
            notifyGameStateUpdated(gameState);
            System.out.println("Game state updated");
            
        } catch (Exception e) {
            notifyError("Error processing game state update");
        }
    }
    
    /**
     * Handles move from other player
     */
    private void handleMove(String message) {
        try {
            String movePlayerId = extractJsonValue(message, "playerId");
            String fromStr = extractJsonValue(message, "from");
            String toStr = extractJsonValue(message, "to");
            
            // Parse positions
            Position from = parsePosition(fromStr);
            Position to = parsePosition(toStr);
            
            if (from != null && to != null) {
                Move move = new Move(from, to, null); // Piece will be determined by game state
                notifyMoveReceived(movePlayerId, move);
                System.out.println("Move received from " + movePlayerId + ": " + fromStr + " to " + toStr);
            }
            
        } catch (Exception e) {
            notifyError("Error processing move");
        }
    }
    
    /**
     * Handles chat message
     */
    private void handleChat(String message) {
        try {
            String chatPlayerId = extractJsonValue(message, "playerId");
            String chatPlayerName = extractJsonValue(message, "playerName");
            String chatMessage = extractJsonValue(message, "message");
            
            notifyChatReceived(chatPlayerId, chatPlayerName, chatMessage);
            System.out.println("Chat from " + chatPlayerName + ": " + chatMessage);
            
        } catch (Exception e) {
            notifyError("Error processing chat message");
        }
    }
    
    /**
     * Handles pong response
     */
    private void handlePong(String message) {
        System.out.println("Pong received from server");
    }
    
    /**
     * Handles error message from server
     */
    private void handleError(String message) {
        try {
            String errorMessage = extractJsonValue(message, "message");
            notifyError("Server error: " + errorMessage);
            System.err.println("Server error: " + errorMessage);
            
        } catch (Exception e) {
            notifyError("Unknown server error");
        }
    }
    
    /**
     * Sends a message to the server
     */
    private void sendMessage(String message) {
        synchronized (connectionLock) {
            if (isConnected && output != null) {
                try {
                    output.println(message);
                    output.flush();
                    System.out.println("Sent: " + message);
                } catch (Exception e) {
                    System.err.println("Error sending message: " + e.getMessage());
                    notifyError("Error sending message: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Sends join game request
     */
    private void sendJoinGameRequest() {
        String joinMessage = createJoinGameMessage();
        sendMessage(joinMessage);
    }
    
    /**
     * Cleanup resources
     */
    private void cleanup() {
        // Close streams
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
        
        // Close socket
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
        
        // Shutdown executor
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
        
        // Wait for message reader thread
        if (messageReaderThread != null && messageReaderThread.isAlive()) {
            try {
                messageReaderThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        isJoined = false;
    }
    
    // Utility methods
    
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
     * Parses a position string to Position object
     */
    private Position parsePosition(String posStr) {
        if (posStr == null || posStr.length() < 2) {
            return null;
        }
        
        try {
            // Handle different position formats
            if (posStr.startsWith("Position")) {
                // Extract row and col from "Position{row=X, col=Y}" format
                int rowStart = posStr.indexOf("row=") + 4;
                int rowEnd = posStr.indexOf(",", rowStart);
                int colStart = posStr.indexOf("col=") + 4;
                int colEnd = posStr.indexOf("}", colStart);
                
                int row = Integer.parseInt(posStr.substring(rowStart, rowEnd));
                int col = Integer.parseInt(posStr.substring(colStart, colEnd));
                
                return new Position(row, col);
            } else {
                // Standard chess notation (e.g., "e2")
                char file = posStr.charAt(0);
                char rank = posStr.charAt(1);
                
                if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
                    return null;
                }
                
                int col = file - 'a';
                int row = rank - '1';
                
                return new Position(row, col);
            }
        } catch (Exception e) {
            return null;
        }
    }
    
    // Message creation methods
    
    private String createJoinGameMessage() {
        return "{\"type\":\"JOIN_GAME\",\"playerId\":\"" + playerId + 
               "\",\"playerName\":\"" + playerName + "\"}";
    }
    
    private String createMoveMessage(Move move) {
        return "{\"type\":\"MOVE\",\"from\":\"" + move.getFrom() + 
               "\",\"to\":\"" + move.getTo() + "\"}";
    }
    
    private String createChatMessage(String message) {
        return "{\"type\":\"CHAT\",\"message\":\"" + message + "\"}";
    }
    
    private String createPingMessage() {
        return "{\"type\":\"PING\",\"timestamp\":" + System.currentTimeMillis() + "}";
    }
    
    private String createDisconnectMessage() {
        return "{\"type\":\"DISCONNECT\"}";
    }
    
    // Getters
    
    public boolean isConnected() {
        synchronized (connectionLock) {
            return isConnected;
        }
    }
    
    public boolean isJoined() {
        return isJoined;
    }
    
    public String getPlayerId() {
        return playerId;
    }
    
    public String getPlayerName() {
        return playerName;
    }
    
    public String getPlayerRole() {
        return playerRole;
    }
    
    public String getPlayerColor() {
        return playerColor;
    }
    
    public GameState getGameState() {
        synchronized (gameStateLock) {
            return gameState;
        }
    }
    
    public String getServerHost() {
        return serverHost;
    }
    
    public int getServerPort() {
        return serverPort;
    }
    
    // Listener management
    
    public void addClientListener(ClientListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeClientListener(ClientListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    // Notification methods
    
    private void notifyConnected() {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onConnected();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyDisconnected() {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onDisconnected();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyJoinedGame(String role, String color) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onJoinedGame(role, color);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyPlayerJoined(String playerId, String playerName) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onPlayerJoined(playerId, playerName);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyPlayerLeft(String playerId) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onPlayerLeft(playerId);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyGameStarted() {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onGameStarted();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyGameStateUpdated(GameState gameState) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onGameStateUpdated(gameState);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyMoveReceived(String playerId, Move move) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onMoveReceived(playerId, move);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyChatReceived(String playerId, String playerName, String message) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onChatReceived(playerId, playerName, message);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyError(String error) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyMessage(String message) {
        synchronized (listeners) {
            for (ClientListener listener : listeners) {
                try {
                    listener.onMessage(message);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
}
