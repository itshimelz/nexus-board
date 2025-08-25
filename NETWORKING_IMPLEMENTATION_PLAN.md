# Nexus Board Networking Implementation Plan

## Overview
This document outlines the implementation plan for adding multiplayer networking capabilities to the Nexus Board chess application using Java socket programming. The implementation will follow a client-server architecture to enable real-time gameplay between two players over a network.

## Architecture Design

### Network Topology
```
┌─────────────────┐         ┌─────────────────┐
│   Player 1      │         │   Player 2      │
│  (Client)       │◄───────►│  (Client)       │
└─────────────────┘         └─────────────────┘
        ▲                         ▲
        │                         │
        ▼                         ▼
┌──────────────────────────────────────────┐
│           Game Server                    │
│         (Server Socket)                  │
└──────────────────────────────────────────┘
```

### Communication Protocol
- **Transport Layer**: TCP (Reliable, ordered delivery)
- **Port**: 8888 (configurable)
- **Message Format**: JSON-based protocol for structured data exchange
- **Encoding**: UTF-8

## Core Components

### 1. Server Component (`Server.java`)

#### Responsibilities:
- Listen for incoming client connections
- Manage game sessions between paired players
- Route game messages between clients
- Handle player disconnections and reconnections
- Maintain game state consistency

#### Key Methods:
```java
public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients;
    private List<GameSession> gameSessions;
    
    public void startServer(int port) { }
    public void stopServer() { }
    public void broadcastMessage(String message) { }
    public GameSession createGameSession(ClientHandler player1, ClientHandler player2) { }
    public void removeClient(ClientHandler client) { }
}
```

### 2. Client Component (`Client.java`)

#### Responsibilities:
- Establish connection to the server
- Send player moves and game actions to the server
- Receive and process messages from the server
- Handle network errors and disconnections
- Update local game state based on server messages

#### Key Methods:
```java
public class Client {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
    public boolean connectToServer(String host, int port) { }
    public void disconnect() { }
    public void sendMessage(String message) { }
    public void startListening() { }
    public void handleServerMessage(String message) { }
}
```

### 3. Client Handler (`ClientHandler.java`)

#### Responsibilities:
- Handle individual client connections on the server side
- Process incoming messages from clients
- Forward messages to appropriate recipients
- Manage client lifecycle (connection, disconnection)
- Maintain client state information

#### Key Methods:
```java
public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private Server server;
    private String playerName;
    private GameSession gameSession;
    
    public void run() { }
    public void sendMessage(String message) { }
    public void handleClientMessage(String message) { }
    public void disconnect() { }
    public String getPlayerName() { }
}
```

### 4. Game Session (`GameSession.java`)

#### Responsibilities:
- Manage game state for a specific match
- Track player information and game progress
- Validate and process game moves
- Handle game-specific logic and rules
- Notify players of game events

#### Key Methods:
```java
public class GameSession {
    private ClientHandler player1;
    private ClientHandler player2;
    private GameState gameState;
    private String sessionId;
    
    public void processMove(Move move, ClientHandler sender) { }
    public void notifyPlayers(String message) { }
    public boolean isPlayerInSession(ClientHandler player) { }
    public void endSession() { }
}
```

## Message Protocol Design

### Message Structure
All messages will follow a standardized JSON format:
```json
{
  "type": "message_type",
  "data": { },
  "timestamp": "ISO8601_timestamp"
}
```

### Message Types

#### 1. Connection Management
- `CONNECT`: Client connects to server
- `DISCONNECT`: Client disconnects from server
- `SESSION_READY`: Server notifies clients that game session is ready

#### 2. Player Management
- `PLAYER_JOIN`: Player joins a game session
- `PLAYER_LEAVE`: Player leaves a game session
- `PLAYER_INFO`: Player information exchange

#### 3. Game Actions
- `MOVE`: Chess move information
- `GAME_START`: Game start notification
- `GAME_END`: Game end with result
- `RESIGN`: Player resignation
- `DRAW_OFFER`: Draw offer to opponent
- `DRAW_ACCEPT`: Draw acceptance

#### 4. Chat System
- `CHAT_MESSAGE`: Text chat between players
- `CHAT_HISTORY`: Previous chat messages

#### 5. Game State
- `GAME_STATE`: Current game board state
- `MOVE_HISTORY`: History of moves in the game

### Example Messages

#### Player Connection:
```json
{
  "type": "CONNECT",
  "data": {
    "playerName": "Alice",
    "playerId": "player_12345"
  },
  "timestamp": "2025-08-25T10:30:00Z"
}
```

#### Chess Move:
```json
{
  "type": "MOVE",
  "data": {
    "from": {"row": 6, "col": 4},
    "to": {"row": 4, "col": 4},
    "piece": "PAWN",
    "player": "WHITE"
  },
  "timestamp": "2025-08-25T10:31:15Z"
}
```

#### Game State Update:
```json
{
  "type": "GAME_STATE",
  "data": {
    "board": [
      ["BR", "BN", "BB", "BQ", "BK", "BB", "BN", "BR"],
      ["BP", "BP", "BP", "BP", "BP", "BP", "BP", "BP"],
      [null, null, null, null, null, null, null, null],
      [null, null, null, null, null, null, null, null],
      [null, null, null, null, "WP", null, null, null],
      [null, null, null, null, null, null, null, null],
      ["WP", "WP", "WP", "WP", null, "WP", "WP", "WP"],
      ["WR", "WN", "WB", "WQ", "WK", "WB", "WN", "WR"]
    ],
    "currentPlayer": "BLACK",
    "moveNumber": 1
  },
  "timestamp": "2025-08-25T10:31:16Z"
}
```

## Implementation Phases - UI-Focused Approach

### UI Flow Design
```
Landing Page
     │
     ├── Single Player (existing functionality)
     │
     └── Multiplayer (NEW NETWORKING FEATURES)
            │
            └── Multiplayer Menu
                   ├── Host Game → Host Dialog → Game Screen (as Server)
                   ├── Join Game → Join Dialog → Game Screen (as Client)
                   └── Direct Connect → Quick Join Dialog
```

### Phase 1: Foundation & Core Networking (Steps 1-6)
**Duration**: 1-2 weeks
**Goals**:
- Set up project dependencies and networking modules
- Implement basic Server, Client, and ClientHandler classes
- Establish TCP connections and message exchange
- Create JSON-based message protocol

**Deliverables**:
- Working Server that can accept client connections
- Working Client that can connect to server
- Complete message protocol implementation
- Basic connection testing functionality

### Phase 2: Multiplayer UI Flow (Steps 7-10)
**Duration**: 1 week
**Goals**:
- Create multiplayer menu accessible from landing page
- Implement host game and join game dialogs
- Design intuitive UI for network game setup
- Handle connection states and user feedback

**Deliverables**:
- MultiplayerMenu UI with host/join options
- HostGameDialog for server setup
- JoinGameDialog for client connection
- Seamless navigation from existing UI

### Phase 3: Game Integration (Steps 11-16)
**Duration**: 2 weeks
**Goals**:
- Implement game session management
- Create network-aware game viewmodel
- Integrate network layer with existing game logic
- Add network-specific UI enhancements

**Deliverables**:
- Working game session management
- NetworkGameScreenViewModel for multiplayer
- Real-time move synchronization
- Enhanced game screen with network features
- Robust error handling and reconnection

## Error Handling and Recovery

### Network Errors
- **Connection Loss**: Implement automatic reconnection attempts
- **Timeout**: Set appropriate timeout values for socket operations
- **Message Loss**: Implement message acknowledgment system

### Game State Errors
- **Desynchronization**: Implement game state reconciliation
- **Invalid Moves**: Validate moves on both client and server
- **Cheating Prevention**: Server-side validation of all moves

### Recovery Mechanisms
- **Checkpoint System**: Periodic game state snapshots
- **Message Queue**: Buffer messages during disconnections
- **Session Persistence**: Save game sessions for later recovery

## Security Considerations

### Data Validation
- Validate all incoming messages on the server
- Sanitize player names and chat messages
- Prevent injection attacks through message data

### Connection Security
- Implement basic authentication for players
- Limit connection attempts to prevent DoS attacks
- Validate client IP addresses if needed

### Game Integrity
- Server-side validation of all game moves
- Prevent players from sending invalid game states
- Implement timeout mechanisms to prevent stalling

## Performance Optimization

### Connection Management
- Use thread pools for handling client connections
- Implement connection limits to prevent resource exhaustion
- Optimize socket buffer sizes

### Message Efficiency
- Compress large messages when possible
- Batch multiple small messages
- Use efficient serialization formats

### Scalability
- Design for horizontal scaling (multiple game servers)
- Implement load balancing for high player counts
- Use non-blocking I/O for better performance

## Testing Strategy

### Unit Testing
- Test individual network components in isolation
- Mock socket connections for testing
- Validate message parsing and serialization

### Integration Testing
- Test complete client-server communication
- Validate game session creation and management
- Test edge cases and error conditions

### Load Testing
- Simulate multiple concurrent players
- Test server performance under load
- Validate resource usage and memory management

## Deployment Considerations

### Server Deployment
- Choose appropriate hosting solution
- Configure firewall and network settings
- Implement monitoring and logging

### Client Configuration
- Handle different network environments
- Implement connection timeout and retry logic
- Provide user feedback for network status

### Network Requirements
- Minimum bandwidth requirements
- Latency considerations for real-time gameplay
- NAT traversal for peer-to-peer connections (if needed)

## Future Enhancements

### Advanced Features
- Tournament mode with multiple players
- Spectator mode for watching games
- Game recording and replay functionality
- Player ranking and statistics system

### Technical Improvements
- WebSocket implementation for better performance
- Encryption for secure communication
- Mobile client support
- Cloud-based server deployment

## Dependencies

### Required Libraries
- Standard Java Socket API (included in JDK)
- JSON processing library (e.g., Gson or Jackson)
- Logging framework (e.g., SLF4J)

### Existing Code Integration
- Reuse existing GameState and Board classes
- Integrate with current MVVM architecture
- Maintain compatibility with single-player mode

## UI Component Structure

### New UI Components to Implement
```
src/main/java/com/himelz/nexusboard/viewController/
├── LandingPage.java (existing - to be modified)
├── GameScreen.java (existing - to be enhanced)
├── MultiplayerMenu.java (new)
├── HostGameDialog.java (new)
└── JoinGameDialog.java (new)

src/main/resources/com/himelz/nexusboard/nexusboard/screens/
├── landing-page.fxml (existing)
├── game-screen.fxml (existing - to be enhanced)
├── multiplayer-menu.fxml (new)
├── host-game-dialog.fxml (new)
└── join-game-dialog.fxml (new)
```

### Key UI Features

#### Multiplayer Menu
- Clean, intuitive design matching existing UI style
- Clear action buttons with icons
- Consistent navigation patterns
- Options: Host Game, Join Game, Direct Connect, Back

#### Host Game Experience
1. Click "Host Game" → Open Host Dialog
2. Set player name and port → Start server
3. Display connection info for sharing
4. Show "Waiting for opponent..." status
5. Auto-transition to game when player joins

#### Join Game Experience
1. Click "Join Game" → Open Join Dialog
2. Enter server IP and player name
3. Show connection progress
4. Display connection errors if any
5. Auto-transition to game when connected

#### Network Game Screen Enhancements
- Display both player names
- Show connection status indicator
- Add network-specific controls
- Network latency display
- "Disconnect" button
- Optional chat functionality

## Step-by-Step Implementation Tasks

### Phase 1: Foundation & Core Networking
1. **Project Setup & Dependencies** - Add Gson and networking modules
2. **Create Message Protocol Classes** - GameMessage and MessageType
3. **Basic Server Implementation** - Core server functionality
4. **Basic Client Implementation** - Core client functionality  
5. **ClientHandler Implementation** - Individual client management
6. **Basic Connection Testing** - Verify foundation works

### Phase 2: Multiplayer UI Flow
7. **Create Multiplayer Menu UI** - MultiplayerMenu controller and FXML
8. **Connect Multiplayer Button to Menu** - Update LandingPage navigation
9. **Host Game Dialog UI** - Server setup interface
10. **Join Game Dialog UI** - Client connection interface

### Phase 3: Game Integration
11. **Game Session Management** - GameSession class implementation
12. **Player Pairing System** - Automatic matchmaking logic
13. **Network Game ViewModel** - NetworkGameScreenViewModel creation
14. **Move Synchronization** - Integrate network with GameState
15. **Network Game Screen Enhancements** - UI improvements for multiplayer
16. **Error Handling & Connection Management** - Robust network handling

## Success Metrics

- Successful real-time gameplay between two remote players
- Less than 200ms latency for move transmission
- 99.9% uptime for game sessions
- Proper handling of network disconnections and reconnections
- Secure and validated game state management