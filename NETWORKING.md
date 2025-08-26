# Networking and Socket Programming in Nexus Board

This document explains how multiplayer networking works in Nexus Board: the client/server architecture, socket layer, threading model, message protocol, game-state synchronization, lifecycle flows, and error handling.

## Overview

- Transport: TCP sockets using Java's ServerSocket and Socket
- Protocol framing: Line-delimited JSON (one JSON object per line)
- Players: 2 (Host and Guest). Host plays White; Guest plays Black
- Authority: Server is authoritative for game rules and state
- Sync model: Server broadcasts MOVE and full GAME_STATE snapshots
- App pattern: MVVM with JavaFX; ViewModels orchestrate networking

## Architecture

- Server (`Server.java`)
  - Listens with `ServerSocket` on a configured port (default 8888 via UI)
  - Accepts up to 2 clients; creates a `ClientHandler` per connection
  - Maintains authoritative `GameState` protected by a lock
  - Assigns first player as Host/White, second as Guest/Black
  - Validates and applies moves; broadcasts updates and end states
  - Exposes listeners for UI to reflect server events

- Client (`Client.java`)
  - Connects to server via `Socket` with a 5s timeout
  - Sends a JOIN_GAME request, then game actions (MOVE, CHAT, etc.)
  - Runs a background message-reader thread to process server messages
  - Mirrors server state locally by consuming `GAME_STATE`
  - Notifies UI via ClientListener callbacks

- ClientHandler (`ClientHandler.java`)
  - Server-side worker per connection (runs in a thread-pool)
  - Reads client messages, parses simple JSON, and invokes server handlers
  - Sends `WELCOME` on connect and errors when needed

## Threading Model

- Server
  - One accept loop thread: accepts sockets and spawns `ClientHandler`
  - A cached thread pool runs each `ClientHandler` (one per client)
  - Locks
    - `clientsLock`: protects set/map of connected clients
    - `gameStateLock`: guards `GameState` and host/guest IDs

- Client
  - One background reader thread `Chess-Client-Reader` reads lines from server
  - `connectionLock`: protects `isConnected` and output stream
  - `gameStateLock`: guards local `GameState` mirror during updates

## Connection and Session Lifecycle

- Host flow
  1. User starts Server from the Host dialog (default port 8888)
  2. Server initializes `GameState` and begins accepting
  3. Host client auto-connects to localhost and sends JOIN_GAME
  4. Server assigns role host/white via `PLAYER_ASSIGNED`

- Guest flow
  1. Guest client connects to Host's IP:port and sends JOIN_GAME
  2. Server assigns role guest/black via `PLAYER_ASSIGNED`
  3. Server notifies host: `PLAYER_JOINED`
  4. Server sends `GAME_START` and an initial `GAME_STATE` snapshot to both

- Play flow
  1. Local player attempts a move; client sends `MOVE` with from/to positions
  2. Server validates via `MoveValidator.validateNetworkMove` and `GameState.makeMove`
  3. If valid, server broadcasts `MOVE` and a fresh `GAME_STATE`
  4. Server checks end conditions and may send `GAME_END` with a message

- Control flow
  - New game: client -> `NEW_GAME` → server resets and broadcasts `GAME_START` + `GAME_STATE`
  - Resign: client -> `RESIGN` → server broadcasts `GAME_END` and a terminal `GAME_STATE`
  - Draw offer: client -> `DRAW_OFFER` → server treats as accepted draw, broadcasts `GAME_END` + `GAME_STATE`

- Disconnect flow
  - Client sends `DISCONNECT` (best-effort), then closes
  - Server detects stream end/exception; removes client and informs the other side via `PLAYER_LEFT`
  - If only one player remains, server resets `GameState`

## Message Protocol (JSON over TCP)

All messages are single-line JSON objects. Keys are simple strings; values are strings or numbers. Server sometimes embeds arrays/objects as strings in JSON values (e.g., `boardState`).

Common envelope field: `type` identifies the message kind.

- From server to clients
  - WELCOME: `{ "type":"WELCOME", "clientId":"<uuid>" }`
  - PLAYER_ASSIGNED: `{ "type":"PLAYER_ASSIGNED", "playerId":"<id>", "role":"host|guest", "color":"white|black" }`
  - PLAYER_JOINED: `{ "type":"PLAYER_JOINED", "playerId":"<id>", "playerName":"<name>" }`
  - PLAYER_LEFT: `{ "type":"PLAYER_LEFT", "playerId":"<id>" }`
  - GAME_START: `{ "type":"GAME_START" }`
  - MOVE: `{ "type":"MOVE", "playerId":"<id>", "from":"e2", "to":"e4" }`
  - GAME_STATE: `{ "type":"GAME_STATE", "currentPlayer":"WHITE|BLACK", "gameStatus":"ACTIVE|CHECK|STALEMATE|CHECKMATE|DRAW", "moveCount":<n>, "boardState": <8x8 array> }`
  - CHAT: `{ "type":"CHAT", "playerId":"<id>", "playerName":"<name>", "message":"..." }`
  - PONG: `{ "type":"PONG", "timestamp":<ms> }`
  - GAME_END: `{ "type":"GAME_END", "message":"human-readable reason" }`
  - ERROR: `{ "type":"ERROR", "message":"error text" }`

- From clients to server
  - JOIN_GAME: `{ "type":"JOIN_GAME", "playerId":"<id>", "playerName":"<name>" }`
  - MOVE: `{ "type":"MOVE", "from":"e2", "to":"e4" }`
  - CHAT: `{ "type":"CHAT", "message":"..." }`
  - PING: `{ "type":"PING", "timestamp":<ms> }`
  - DISCONNECT: `{ "type":"DISCONNECT" }`
  - NEW_GAME: `{ "type":"NEW_GAME" }`
  - RESIGN: `{ "type":"RESIGN" }`
  - DRAW_OFFER: `{ "type":"DRAW_OFFER" }`

Notes
- Positions use algebraic like "e2"/"e4"; parsing centralizes on `Position.fromAlgebraic()`.
- Board JSON is an 8x8 array of either null or `{ "type": "Pawn|Rook|Knight|Bishop|Queen|King", "color": "WHITE|BLACK" }`.
- The server crafts full board snapshots after each accepted move to keep clients deterministic and in-sync.

## Validation and Authority

- The server validates every MOVE:
  - The sending player must match the side-to-move and their assigned color
  - The move must be legal per chess rules via `GameState.makeMove`
  - If invalid, server replies to that player with `ERROR`
- On success, the server is the single source of truth and broadcasts the move and the updated snapshot.

## Error Handling and Robustness

- Network errors and malformed JSON produce `ERROR` responses but do not crash the server/client loop
- The server caps connections at two; extra clients receive an error and are disconnected
- Disconnections clean up server state, notify the peer, and reset the game if a player leaves
- Parsing uses minimal string search instead of a JSON library by design; be aware of escaping/format limitations

## Roles, Colors, and Limits

- First JOIN assigns host/white; second assigns guest/black
- Only two players are supported per server instance
- Host and Guest IDs are tracked to route direct messages when needed

## Sequence Diagrams (textual)

- Host/Guest join
  - Client(host) → JOIN_GAME → Server
  - Server → PLAYER_ASSIGNED(role=host,color=white) → Host
  - Client(guest) → JOIN_GAME → Server
  - Server → PLAYER_ASSIGNED(role=guest,color=black) → Guest
  - Server → PLAYER_JOINED(guest info) → Host
  - Server → GAME_START → both
  - Server → GAME_STATE(initial board) → both

- Move exchange
  - Client(X) → MOVE(from,to) → Server
  - Server: validate; if ok: apply; if not: ERROR to X
  - Server → MOVE(playerId,from,to) → both
  - Server → GAME_STATE(snapshot) → both
  - Server: check end; maybe → GAME_END(message) → both

- Resign/Draw/New game
  - Client → RESIGN/DRAW_OFFER/NEW_GAME → Server
  - Server → GAME_END or GAME_START + GAME_STATE → both

## Integration with MVVM/UI

- HostGameDialogViewModel: starts Server, then auto-connects a Client to localhost; transitions to `GameScreen` on game start
- JoinGameDialogViewModel: connects a Client to a remote server; stores recent connections; transitions on game start
- GameScreenViewModel: mediates user moves; when allowed, sends MOVE; applies incoming MOVE/GAME_STATE to update UI

## Port, IP, and NAT

- Default port presented in UI is 8888; changeable by host
- Guests must know the host's LAN/WAN IP and port
- Across the internet, you’ll need router port forwarding or a public host

## Security Considerations

- No authentication or encryption; traffic is plaintext on TCP
- No input escaping in the hand-rolled parser; avoid untrusted message producers
- For production, consider TLS, authentication, and a JSON library (e.g., Jackson/Gson)

## Troubleshooting Tips

- Connection refused: ensure host started server and port is open
- Can’t see moves: check both sides use same Nexus Board version; watch logs for ERROR messages
- Out-of-sync board: server regularly sends full GAME_STATE; reconnect if state appears stale

## File Map

- `src/main/java/com/himelz/nexusboard/network/Server.java`
- `src/main/java/com/himelz/nexusboard/network/ClientHandler.java`
- `src/main/java/com/himelz/nexusboard/network/Client.java`
- UI flow: `viewmodel/HostGameDialogViewModel.java`, `viewmodel/JoinGameDialogViewModel.java`, `viewmodel/GameScreenViewModel.java`

## Code snippets

### Start a server and listen for events (Java)

```java
import com.himelz.nexusboard.network.Server;
import com.himelz.nexusboard.model.GameState;

public class ServerBootstrap {
  public static void main(String[] args) {
    int port = 8888;
    Server server = new Server(port);

    server.addServerListener(new Server.ServerListener() {
      @Override public void onServerStarted(int p) {
        System.out.println("Server started on port " + p);
      }
      @Override public void onServerStopped() {
        System.out.println("Server stopped");
      }
      @Override public void onPlayerConnected(String playerId, String playerName) {
        System.out.println("Player connected: " + playerName + " (" + playerId + ")");
      }
      @Override public void onPlayerDisconnected(String playerId) {
        System.out.println("Player disconnected: " + playerId);
      }
      @Override public void onGameStateChanged(GameState state) {
        System.out.println("Game state updated. Moves: " + state.getMoveHistory().size());
      }
      @Override public void onError(String error) {
        System.err.println("Server error: " + error);
      }
      @Override public void onMessageReceived(String playerId, String message) {
        System.out.println("MSG from " + playerId + ": " + message);
      }
    });

    if (!server.start()) {
      System.err.println("Failed to start server");
    }
  }
}
```

### Connect a client and join a game (Java)

```java
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.model.GameState;
import com.himelz.nexusboard.model.board.Move;

public class ClientBootstrap {
  public static void main(String[] args) {
    Client client = new Client();

    client.addClientListener(new Client.ClientListener() {
      @Override public void onConnected() { System.out.println("Connected"); }
      @Override public void onDisconnected() { System.out.println("Disconnected"); }
      @Override public void onJoinedGame(String role, String color) {
        System.out.println("Joined as " + role + " playing " + color);
      }
      @Override public void onPlayerJoined(String id, String name) {
        System.out.println("Peer joined: " + name);
      }
      @Override public void onPlayerLeft(String id) {
        System.out.println("Peer left: " + id);
      }
      @Override public void onGameStarted() { System.out.println("Game started"); }
      @Override public void onGameStateUpdated(GameState gs) {
        System.out.println("State: " + gs.getGameStatus());
      }
      @Override public void onMoveReceived(String pid, Move move) {
        System.out.println("Move received from " + pid + ": " + move);
      }
      @Override public void onChatReceived(String pid, String name, String msg) {
        System.out.println(name + ": " + msg);
      }
      @Override public void onError(String err) { System.err.println(err); }
      @Override public void onMessage(String msg) { System.out.println("RAW: " + msg); }
    });

    String playerId = "p-" + System.currentTimeMillis();
    String playerName = "Player";
    boolean ok = client.connect("127.0.0.1", 8888, playerId, playerName);
    if (!ok) System.err.println("Connect failed");
  }
}
```

### Send a move from the client (Java)

```java
import com.himelz.nexusboard.network.Client;
import com.himelz.nexusboard.model.board.Move;
import com.himelz.nexusboard.model.board.Position;

// Given a connected and joined Client instance
Client client = /* ... */;

// Create a move e2 -> e4 and send
Move move = new Move(Position.fromAlgebraic("e2"), Position.fromAlgebraic("e4"), null);
client.sendMove(move);
```

### Request a new game, resign, or offer a draw (Java)

```java
// Client must be connected and joined
client.sendNewGameRequest();
client.sendResign();
client.sendOfferDraw();
```

### Minimal raw JSON MOVE (wire example)

```json
{ "type": "MOVE", "from": "e2", "to": "e4" }
```
