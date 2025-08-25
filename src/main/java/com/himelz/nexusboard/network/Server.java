package com.himelz.nexusboard.network;

import com.himelz.nexusboard.model.Color;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;
import com.himelz.nexusboard.model.pieces.ChessPiece;
import com.himelz.nexusboard.utils.MoveValidator;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * Server implementation for handling multiplayer chess games.
 * Manages client connections, game state synchronization, and message routing.
 */
public class Server {
    
    // Server configuration
    private final int port;
    private ServerSocket serverSocket;
    private boolean isRunning;
    private boolean isStarted;
    
    // Thread management
    private ExecutorService clientThreadPool;
    private Thread serverThread;
    
    // Client management
    private final Set<ClientHandler> connectedClients;
    private final Map<String, ClientHandler> clientsByPlayerId;
    private final Object clientsLock = new Object();
    
    // Game management
    private GameState gameState;
    private String hostPlayerId;
    private String guestPlayerId;
    private final Object gameStateLock = new Object();
    
    // Server listeners
    private final List<ServerListener> listeners;
    
    /**
     * Interface for server event listeners
     */
    public interface ServerListener {
        void onServerStarted(int port);
        void onServerStopped();
        void onPlayerConnected(String playerId, String playerName);
        void onPlayerDisconnected(String playerId);
        void onGameStateChanged(GameState gameState);
        void onError(String error);
        void onMessageReceived(String playerId, String message);
    }
    
    /**
     * Creates a new server instance
     */
    public Server(int port) {
        this.port = port;
        this.isRunning = false;
        this.isStarted = false;
        this.connectedClients = ConcurrentHashMap.newKeySet();
        this.clientsByPlayerId = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.clientThreadPool = Executors.newCachedThreadPool();
    }
    
    /**
     * Starts the server
     */
    public synchronized boolean start() {
        if (isStarted) {
            notifyError("Server is already started");
            return false;
        }
        
        try {
            serverSocket = new ServerSocket(port);
            isRunning = true;
            isStarted = true;
            
            // Initialize game state
            synchronized (gameStateLock) {
                gameState = new GameState();
                hostPlayerId = null;
                guestPlayerId = null;
            }
            
            // Start server thread
            serverThread = new Thread(this::serverLoop, "Chess-Server-Main");
            serverThread.setDaemon(false);
            serverThread.start();
            
            notifyServerStarted(port);
            System.out.println("Chess server started on port " + port);
            
            return true;
            
        } catch (IOException e) {
            isRunning = false;
            isStarted = false;
            notifyError("Failed to start server: " + e.getMessage());
            System.err.println("Failed to start server: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Stops the server
     */
    public synchronized void stop() {
        if (!isStarted) {
            return;
        }
        
        System.out.println("Stopping chess server...");
        isRunning = false;
        
        // Close all client connections
        synchronized (clientsLock) {
            for (ClientHandler client : new ArrayList<>(connectedClients)) {
                client.disconnect();
            }
            connectedClients.clear();
            clientsByPlayerId.clear();
        }
        
        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }
        
        // Shutdown thread pool
        clientThreadPool.shutdown();
        try {
            if (!clientThreadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                clientThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            clientThreadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        // Wait for server thread to finish
        if (serverThread != null && serverThread.isAlive()) {
            try {
                serverThread.join(3000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        isStarted = false;
        notifyServerStopped();
        System.out.println("Chess server stopped");
    }
    
    /**
     * Main server loop for accepting client connections
     */
    private void serverLoop() {
        System.out.println("Server listening for connections...");
        
        while (isRunning) {
            try {
                Socket clientSocket = serverSocket.accept();
                
                if (!isRunning) {
                    clientSocket.close();
                    break;
                }
                
                System.out.println("New client connection from: " + clientSocket.getInetAddress());
                
                // Create client handler
                ClientHandler clientHandler = new ClientHandler(this, clientSocket);
                
                synchronized (clientsLock) {
                    if (connectedClients.size() >= 2) {
                        // Server is full
                        clientHandler.sendMessage(createErrorMessage("Server is full. Maximum 2 players allowed."));
                        clientHandler.disconnect();
                        continue;
                    }
                    
                    connectedClients.add(clientHandler);
                }
                
                // Start client handler in thread pool
                clientThreadPool.submit(clientHandler);
                
            } catch (IOException e) {
                if (isRunning) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                    notifyError("Error accepting client connection: " + e.getMessage());
                }
            }
        }
    }
    
    /**
     * Handles a new player joining
     */
    public void handlePlayerJoined(ClientHandler clientHandler, String playerId, String playerName) {
        synchronized (clientsLock) {
            clientsByPlayerId.put(playerId, clientHandler);
        }
        
        synchronized (gameStateLock) {
            if (hostPlayerId == null) {
                // First player becomes host
                hostPlayerId = playerId;
                gameState.setHostPlayer(playerId, playerName);
                
                // Send host confirmation
                clientHandler.sendMessage(createPlayerAssignedMessage(playerId, "host", "white"));
                
            } else if (guestPlayerId == null) {
                // Second player becomes guest
                guestPlayerId = playerId;
                gameState.setGuestPlayer(playerId, playerName);
                
                // Send guest confirmation
                clientHandler.sendMessage(createPlayerAssignedMessage(playerId, "guest", "black"));
                
                // Notify host about guest joining
                ClientHandler hostHandler = clientsByPlayerId.get(hostPlayerId);
                if (hostHandler != null) {
                    hostHandler.sendMessage(createPlayerJoinedMessage(playerId, playerName));
                }
                
                // Start the game
                startGame();
                
            } else {
                // Game is full
                clientHandler.sendMessage(createErrorMessage("Game is full"));
                clientHandler.disconnect();
                return;
            }
        }
        
        notifyPlayerConnected(playerId, playerName);
        System.out.println("Player joined: " + playerName + " (" + playerId + ")");
    }
    
    /**
     * Handles a player leaving
     */
    public void handlePlayerLeft(ClientHandler clientHandler, String playerId) {
        synchronized (clientsLock) {
            connectedClients.remove(clientHandler);
            clientsByPlayerId.remove(playerId);
        }
        
        synchronized (gameStateLock) {
            if (playerId.equals(hostPlayerId)) {
                hostPlayerId = null;
                // Notify guest that host left
                if (guestPlayerId != null) {
                    ClientHandler guestHandler = clientsByPlayerId.get(guestPlayerId);
                    if (guestHandler != null) {
                        guestHandler.sendMessage(createPlayerLeftMessage(playerId));
                    }
                }
            } else if (playerId.equals(guestPlayerId)) {
                guestPlayerId = null;
                // Notify host that guest left
                if (hostPlayerId != null) {
                    ClientHandler hostHandler = clientsByPlayerId.get(hostPlayerId);
                    if (hostHandler != null) {
                        hostHandler.sendMessage(createPlayerLeftMessage(playerId));
                    }
                }
            }
            
            // Reset game if a player left
            if (hostPlayerId == null || guestPlayerId == null) {
                gameState = new GameState();
            }
        }
        
        notifyPlayerDisconnected(playerId);
        System.out.println("Player left: " + playerId);
    }
    
    /**
     * Handle a move with position data (creates Move object internally)
     */
    public void handleMove(String playerId, Position from, Position to) {
        // Get the piece at the from position
        ChessPiece piece = gameState.getBoard().getPiece(from);
        if (piece != null) {
            Move move = new Move(from, to, piece);
            handleMove(playerId, move);
        } else {
            // Send error message to client
            ClientHandler client = clientsByPlayerId.get(playerId);
            if (client != null) {
                client.sendMessage("{\"type\":\"moveResult\",\"success\":false,\"error\":\"No piece at source position\"}");
            }
        }
    }
    
    /**
     * Handles a move from a player with comprehensive validation
     */
    public void handleMove(String playerId, Move move) {
        System.out.println("DEBUG: Handling move from playerId: " + playerId + 
                          ", hostPlayerId: " + hostPlayerId + ", guestPlayerId: " + guestPlayerId);
        
        synchronized (gameStateLock) {
            // Use MoveValidator for comprehensive validation
            MoveValidator.ValidationResult validation = MoveValidator.validateNetworkMove(
                gameState, move, playerId, hostPlayerId, guestPlayerId);
            
            if (!validation.isValid()) {
                sendErrorToPlayer(playerId, validation.getMessage());
                System.out.println("Move validation failed for " + playerId + ": " + validation.getMessage());
                return;
            }
            
            // Apply move to game state (this includes chess rule validation)
            if (gameState.makeMove(move)) {
                // Move is valid, broadcast to all players
                String moveMessage = createMoveMessage(playerId, move);
                broadcastMessage(moveMessage);
                
                // Send updated game state to ensure synchronization
                String gameStateMessage = createGameStateMessage(gameState);
                broadcastMessage(gameStateMessage);
                
                notifyGameStateChanged(gameState);
                
                System.out.println("Move processed: " + move.getFrom().toAlgebraic() + 
                    " -> " + move.getTo().toAlgebraic() + " by " + playerId);
                
                // Check for game end conditions
                checkGameEndConditions();
                
            } else {
                // Invalid move according to chess rules
                sendErrorToPlayer(playerId, "Invalid move: violates chess rules");
                System.out.println("Invalid move attempted: " + move.getFrom().toAlgebraic() + 
                    " -> " + move.getTo().toAlgebraic() + " by " + playerId);
            }
        }
    }
    
    /**
     * Helper method to send error message to a specific player
     */
    private void sendErrorToPlayer(String playerId, String errorMessage) {
        ClientHandler playerHandler = clientsByPlayerId.get(playerId);
        if (playerHandler != null) {
            playerHandler.sendMessage(createErrorMessage(errorMessage));
        }
        System.out.println("Error sent to " + playerId + ": " + errorMessage);
    }
    
    /**
     * Check for game end conditions and notify players
     */
    private void checkGameEndConditions() {
        GameState.GameStatus status = gameState.getGameStatus();
        
        switch (status) {
            case CHECKMATE:
                Color winner = gameState.getCurrentPlayer() == Color.WHITE ? Color.BLACK : Color.WHITE;
                String winnerName = (winner == gameState.getHostColor()) ? "Host" : "Guest";
                broadcastMessage(createMessage("GAME_END", "Checkmate! " + winnerName + " wins!"));
                System.out.println("Game ended: Checkmate, " + winnerName + " wins");
                break;
                
            case STALEMATE:
                broadcastMessage(createMessage("GAME_END", "Game ends in stalemate (draw)"));
                System.out.println("Game ended: Stalemate");
                break;
                
            case DRAW:
                broadcastMessage(createMessage("GAME_END", "Game ends in a draw"));
                System.out.println("Game ended: Draw");
                break;
                
            case CHECK:
                String playerInCheck = gameState.getCurrentPlayer() == gameState.getHostColor() ? "Host" : "Guest";
                broadcastMessage(createMessage("CHECK", playerInCheck + " is in check!"));
                System.out.println(playerInCheck + " is in check");
                break;
                
            case ACTIVE:
                // Game continues
                break;
        }
    }
    
    /**
     * Starts the game between two players
     */
    private void startGame() {
        synchronized (gameStateLock) {
            if (hostPlayerId != null && guestPlayerId != null) {
                // Initialize new game
                gameState = new GameState();
                
                // Send game start message to both players
                String gameStartMessage = createGameStartMessage();
                broadcastMessage(gameStartMessage);
                
                // Send initial game state
                String gameStateMessage = createGameStateMessage(gameState);
                broadcastMessage(gameStateMessage);
                
                notifyGameStateChanged(gameState);
                System.out.println("Game started between " + hostPlayerId + " and " + guestPlayerId);
            }
        }
    }

    // ============ Public handlers for control messages ============

    /**
     * Handle a request from a player to start a new game.
     */
    public void handleNewGameRequest(String requesterId) {
        synchronized (gameStateLock) {
            // Reset game regardless of who asked, if at least one player exists
            gameState = new GameState();
            String gameStartMessage = createGameStartMessage();
            broadcastMessage(gameStartMessage);
            String gameStateMessage = createGameStateMessage(gameState);
            broadcastMessage(gameStateMessage);
            notifyGameStateChanged(gameState);
            System.out.println("New game requested by " + requesterId + "; game reset and broadcasted");
        }
    }

    /**
     * Handle resignation from a player; end game and broadcast.
     */
    public void handleResign(String playerId) {
        synchronized (gameStateLock) {
            String who = playerId.equals(hostPlayerId) ? "Host" : (playerId.equals(guestPlayerId) ? "Guest" : playerId);
            // Mark game ended (use DRAW as non-active terminal status if no dedicated RESIGN status exists)
            gameState.setGameStatus(GameState.GameStatus.DRAW);
            broadcastMessage(createMessage("GAME_END", who + " resigned"));
            // Also broadcast state so clients disable moves
            broadcastMessage(createGameStateMessage(gameState));
            notifyGameStateChanged(gameState);
            System.out.println("Game ended by resignation from: " + who);
        }
    }

    /**
     * Auto-accept draw offer and end the game as draw.
     */
    public void handleDrawOffer(String playerId) {
        synchronized (gameStateLock) {
            gameState.setGameStatus(GameState.GameStatus.DRAW);
            broadcastMessage(createMessage("GAME_END", "Game ended in a draw by agreement"));
            broadcastMessage(createGameStateMessage(gameState));
            notifyGameStateChanged(gameState);
            System.out.println("Game ended in draw by agreement (offered by: " + playerId + ")");
        }
    }
    
    /**
     * Broadcasts a message to all connected clients
     */
    public void broadcastMessage(String message) {
        System.out.println("DEBUG: Broadcasting message to " + connectedClients.size() + " clients: " + 
                          (message.length() > 100 ? message.substring(0, 100) + "..." : message));
        
        synchronized (clientsLock) {
            for (ClientHandler client : connectedClients) {
                System.out.println("DEBUG: Sending to client: " + client.getClientId());
                client.sendMessage(message);
            }
        }
    }
    
    /**
     * Sends a message to a specific player
     */
    public void sendMessageToPlayer(String playerId, String message) {
        synchronized (clientsLock) {
            ClientHandler clientHandler = clientsByPlayerId.get(playerId);
            if (clientHandler != null) {
                clientHandler.sendMessage(message);
            }
        }
    }
    
    // Message creation methods (JSON-based protocol)
    
    private String createPlayerAssignedMessage(String playerId, String role, String color) {
        return "{\"type\":\"PLAYER_ASSIGNED\",\"playerId\":\"" + playerId + 
               "\",\"role\":\"" + role + "\",\"color\":\"" + color + "\"}";
    }
    
    private String createPlayerJoinedMessage(String playerId, String playerName) {
        return "{\"type\":\"PLAYER_JOINED\",\"playerId\":\"" + playerId + 
               "\",\"playerName\":\"" + playerName + "\"}";
    }
    
    private String createPlayerLeftMessage(String playerId) {
        return "{\"type\":\"PLAYER_LEFT\",\"playerId\":\"" + playerId + "\"}";
    }
    
    private String createMoveMessage(String playerId, Move move) {
        return "{\"type\":\"MOVE\",\"playerId\":\"" + playerId + 
               "\",\"from\":\"" + move.getFrom() + "\",\"to\":\"" + move.getTo() + "\"}";
    }
    
    private String createGameStateMessage(GameState gameState) {
        // Create a comprehensive game state message including board state
        String currentPlayer = gameState.getCurrentPlayer().toString();
        String gameStatus = gameState.getGameStatus().toString();
        int moveCount = gameState.getMoveHistory().size();
        String boardState = createBoardStateJson(gameState);
        
        return "{\"type\":\"GAME_STATE\"," +
               "\"currentPlayer\":\"" + currentPlayer + "\"," +
               "\"gameStatus\":\"" + gameStatus + "\"," +
               "\"moveCount\":" + moveCount + "," +
               "\"boardState\":" + boardState + "}";
    }

    private String createBoardStateJson(GameState gameState) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int row = 0; row < 8; row++) {
            if (row > 0) json.append(",");
            json.append("[");
            
            for (int col = 0; col < 8; col++) {
                if (col > 0) json.append(",");
                
                ChessPiece piece = gameState.getBoard().getPiece(new Position(row, col));
                if (piece == null) {
                    json.append("null");
                } else {
                    json.append("{");
                    json.append("\"type\":\"").append(piece.getClass().getSimpleName()).append("\",");
                    json.append("\"color\":\"").append(piece.getColor().toString()).append("\"");
                    // Remove row and col from piece JSON as they're implied by position
                    json.append("}");
                }
            }
            
            json.append("]");
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String createGameStartMessage() {
        return "{\"type\":\"GAME_START\"}";
    }
    
    private String createErrorMessage(String error) {
        return "{\"type\":\"ERROR\",\"message\":\"" + error + "\"}";
    }
    
    /**
     * Create a generic message with specified type and content
     */
    private String createMessage(String type, String message) {
        return "{\"type\":\"" + type + "\",\"message\":\"" + message + "\"}";
    }
    
    // Getter methods
    
    public boolean isRunning() {
        return isRunning && isStarted;
    }
    
    public int getPort() {
        return port;
    }
    
    public int getConnectedClientsCount() {
        synchronized (clientsLock) {
            return connectedClients.size();
        }
    }
    
    public Set<String> getConnectedPlayerIds() {
        synchronized (clientsLock) {
            return new HashSet<>(clientsByPlayerId.keySet());
        }
    }
    
    public String getHostPlayerId() {
        synchronized (gameStateLock) {
            return hostPlayerId;
        }
    }
    
    public String getGuestPlayerId() {
        synchronized (gameStateLock) {
            return guestPlayerId;
        }
    }
    
    public GameState getGameState() {
        synchronized (gameStateLock) {
            return gameState; // Should return a copy in production
        }
    }
    
    // Listener management
    
    public void addServerListener(ServerListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }
    
    public void removeServerListener(ServerListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
    
    // Notification methods
    
    private void notifyServerStarted(int port) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onServerStarted(port);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyServerStopped() {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onServerStopped();
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyPlayerConnected(String playerId, String playerName) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onPlayerConnected(playerId, playerName);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyPlayerDisconnected(String playerId) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onPlayerDisconnected(playerId);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyGameStateChanged(GameState gameState) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onGameStateChanged(gameState);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyError(String error) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
    
    private void notifyMessageReceived(String playerId, String message) {
        synchronized (listeners) {
            for (ServerListener listener : listeners) {
                try {
                    listener.onMessageReceived(playerId, message);
                } catch (Exception e) {
                    System.err.println("Error notifying listener: " + e.getMessage());
                }
            }
        }
    }
}
