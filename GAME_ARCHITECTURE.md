# Nexus Board - Game Architecture

This document provides a comprehensive overview of the Nexus Board chess application architecture, including system components, data flow, and interaction patterns.

## Table of Contents
- [System Overview](#system-overview)
- [Architecture Patterns](#architecture-patterns)
- [Application Layers](#application-layers)
- [Network Architecture](#network-architecture)
- [Game Flow](#game-flow)
- [Data Models](#data-models)
- [Component Interactions](#component-interactions)

## System Overview

Nexus Board is a multiplayer chess application built using JavaFX with a client-server architecture. The application follows MVVM (Model-View-ViewModel) pattern for clean separation of concerns.

```mermaid
graph TB
    subgraph "Client Application"
        UI[JavaFX UI Layer]
        VM[ViewModel Layer]
        MODEL[Model Layer]
        NETWORK[Network Client]
    end
    
    subgraph "Server Application"
        SERVER[Game Server]
        HANDLER[Client Handler]
        VALIDATOR[Move Validator]
        GAMESTATE[Game State Manager]
    end
    
    UI --> VM
    VM --> MODEL
    VM --> NETWORK
    NETWORK --> SERVER
    SERVER --> HANDLER
    HANDLER --> VALIDATOR
    HANDLER --> GAMESTATE
    
    SERVER -.->|Game Updates| NETWORK
    NETWORK -.->|UI Updates| VM
    VM -.->|State Changes| UI
```

## Architecture Patterns

### MVVM Pattern Implementation

```mermaid
graph LR
    subgraph "View Layer"
        LP[LandingPage.java]
        MM[MultiplayerMenu.java]
        GS[GameScreen.java]
        HD[HostGameDialog.java]
        JD[JoinGameDialog.java]
    end
    
    subgraph "ViewModel Layer"
        LSVM[LandingScreenViewModel]
        MMVM[MultiplayerMenuViewModel]
        GSVM[GameScreenViewModel]
        HDVM[HostGameDialogViewModel]
        JDVM[JoinGameDialogViewModel]
    end
    
    subgraph "Model Layer"
        BOARD[Board Model]
        PIECES[Chess Pieces]
        GAMESTATE[Game State]
        NETWORK[Network Layer]
    end
    
    LP --> LSVM
    MM --> MMVM
    GS --> GSVM
    HD --> HDVM
    JD --> JDVM
    
    LSVM --> BOARD
    MMVM --> NETWORK
    GSVM --> BOARD
    GSVM --> PIECES
    GSVM --> GAMESTATE
    HDVM --> NETWORK
    JDVM --> NETWORK
```

## Application Layers

### Layer Structure

```mermaid
graph TD
    subgraph "Presentation Layer"
        FXML[FXML Views]
        CSS[CSS Styles]
        IMAGES[Image Assets]
        CONTROLLERS[View Controllers]
    end
    
    subgraph "Business Layer"
        VIEWMODELS[ViewModels]
        GAMELOGIC[Game Logic]
        VALIDATION[Move Validation]
        UTILS[Utilities]
    end
    
    subgraph "Data Layer"
        MODELS[Data Models]
        GAMESTATE[Game State]
        NETWORK[Network Communication]
    end
    
    subgraph "Infrastructure"
        JAVAFX[JavaFX Framework]
        SOCKETS[Java Sockets]
        THREADS[Thread Management]
    end
    
    CONTROLLERS --> VIEWMODELS
    VIEWMODELS --> GAMELOGIC
    VIEWMODELS --> VALIDATION
    GAMELOGIC --> MODELS
    GAMELOGIC --> GAMESTATE
    VIEWMODELS --> NETWORK
    
    NETWORK --> SOCKETS
    CONTROLLERS --> JAVAFX
    GAMELOGIC --> THREADS
```

## Network Architecture

### Client-Server Communication

```mermaid
sequenceDiagram
    participant Client1 as Client 1
    participant Server as Game Server
    participant Client2 as Client 2
    participant Handler as Client Handler
    
    Client1->>Server: Connect Request
    Server->>Handler: Create Handler Thread
    Handler->>Client1: Connection Established
    
    Client2->>Server: Connect Request
    Server->>Handler: Create Handler Thread
    Handler->>Client2: Connection Established
    
    Server->>Client1: Assign Color WHITE
    Server->>Client2: Assign Color BLACK
    
    Client1->>Server: Make Move
    Handler->>Handler: Validate Move
    Handler->>Server: Update Game State
    Server->>Client1: Move Confirmed
    Server->>Client2: Opponent Move Update
    
    Client2->>Server: Make Move
    Handler->>Handler: Validate Move
    Handler->>Server: Update Game State
    Server->>Client2: Move Confirmed
    Server->>Client1: Opponent Move Update
```

### Network Component Structure

```mermaid
graph TB
    subgraph "Client Side"
        CLIENT[Client.java]
        LISTENER[ClientListener Interface]
        CLIENTVM[Client ViewModels]
    end
    
    subgraph "Server Side"
        SERVER[Server.java]
        HANDLER[ClientHandler.java]
        SERVERLISTENER[ServerListener Interface]
        THREADPOOL[Thread Pool Executor]
    end
    
    subgraph "Shared Models"
        MOVE[Move.java]
        POSITION[Position.java]
        GAMESTATE[GameState.java]
        COLOR[Color.java]
    end
    
    CLIENT --> LISTENER
    LISTENER --> CLIENTVM
    CLIENT -.->|Socket Connection| SERVER
    
    SERVER --> HANDLER
    SERVER --> SERVERLISTENER
    SERVER --> THREADPOOL
    
    HANDLER --> MOVE
    HANDLER --> GAMESTATE
    CLIENT --> MOVE
    CLIENT --> POSITION
```

## Game Flow

### Application Navigation Flow

```mermaid
graph TD
    START[Application Start] --> LANDING[Landing Page]
    LANDING --> MENU[Multiplayer Menu]
    
    MENU --> HOST[Host Game Dialog]
    MENU --> JOIN[Join Game Dialog]
    
    HOST --> SERVER_START[Start Server]
    SERVER_START --> WAIT[Wait for Players]
    WAIT --> GAME[Game Screen]
    
    JOIN --> CLIENT_CONNECT[Connect to Server]
    CLIENT_CONNECT --> GAME
    
    GAME --> GAMEPLAY[Gameplay Loop]
    GAMEPLAY --> MOVE[Make Move]
    MOVE --> VALIDATE[Validate Move]
    VALIDATE --> UPDATE[Update Game State]
    UPDATE --> SYNC[Sync with Opponent]
    SYNC --> GAMEPLAY
    
    GAMEPLAY --> END[Game End]
    END --> MENU
```

### Game State Management

```mermaid
stateDiagram-v2
    [*] --> WAITING_FOR_PLAYERS
    WAITING_FOR_PLAYERS --> GAME_READY : Both players connected
    GAME_READY --> WHITE_TURN : Game starts
    
    WHITE_TURN --> PROCESSING_MOVE : White makes move
    PROCESSING_MOVE --> VALIDATING : Validate move
    VALIDATING --> BLACK_TURN : Move valid
    VALIDATING --> WHITE_TURN : Move invalid
    
    BLACK_TURN --> PROCESSING_MOVE : Black makes move
    PROCESSING_MOVE --> WHITE_TURN : Move valid & processed
    
    WHITE_TURN --> CHECKMATE : Game ends
    BLACK_TURN --> CHECKMATE : Game ends
    WHITE_TURN --> STALEMATE : Draw
    BLACK_TURN --> STALEMATE : Draw
    
    CHECKMATE --> [*]
    STALEMATE --> [*]
    
    note right of PROCESSING_MOVE
        Move validation includes:
        - Legal move check
        - Check detection
        - Checkmate detection
        - Stalemate detection
    end note
```

## Data Models

### Chess Model Hierarchy

```mermaid
classDiagram
    class ChessPiece {
        <<abstract>>
        +Color color
        +Position position
        +boolean hasMoved
        +getPossibleMoves()
        +isValidMove()
        +move()
    }
    
    class Piece {
        <<enumeration>>
        PAWN
        ROOK
        KNIGHT
        BISHOP
        QUEEN
        KING
    }
    
    class Pawn {
        +boolean enPassantVulnerable
        +getPossibleMoves()
    }
    
    class Rook {
        +getPossibleMoves()
    }
    
    class Knight {
        +getPossibleMoves()
    }
    
    class Bishop {
        +getPossibleMoves()
    }
    
    class Queen {
        +getPossibleMoves()
    }
    
    class King {
        +boolean inCheck
        +canCastle()
        +getPossibleMoves()
    }
    
    class Board {
        +Square[][] squares
        +List~ChessPiece~ pieces
        +getPieceAt(Position)
        +movePiece(Move)
        +isSquareUnderAttack()
    }
    
    class Position {
        +int row
        +int col
        +toString()
        +equals()
    }
    
    class Move {
        +Position from
        +Position to
        +ChessPiece piece
        +ChessPiece capturedPiece
        +boolean isCapture
        +boolean isCastling
    }
    
    class GameState {
        +Board board
        +Color currentPlayer
        +GameStatus status
        +List~Move~ moveHistory
        +boolean isCheck()
        +boolean isCheckmate()
        +boolean isStalemate()
    }
    
    ChessPiece <|-- Pawn
    ChessPiece <|-- Rook
    ChessPiece <|-- Knight
    ChessPiece <|-- Bishop
    ChessPiece <|-- Queen
    ChessPiece <|-- King
    
    ChessPiece --> Piece
    ChessPiece --> Position
    Board --> ChessPiece
    Move --> Position
    Move --> ChessPiece
    GameState --> Board
    GameState --> Move
```

## Component Interactions

### Client-Side Component Interaction

```mermaid
graph TB
    subgraph "UI Components"
        GAMEUI[Game Screen UI]
        MENUUI[Menu UI]
        DIALOGUI[Dialog UI]
    end
    
    subgraph "ViewModels"
        GAMEVM[GameScreenViewModel]
        MENUVM[MenuViewModel]
        DIALOGVM[DialogViewModel]
    end
    
    subgraph "Services"
        NETWORKSVC[Network Service]
        GAMESVC[Game Service]
        VALIDATIONSVC[Validation Service]
    end
    
    subgraph "Models"
        GAMEMODEL[Game Model]
        BOARDMODEL[Board Model]
        PIECEMODEL[Piece Models]
    end
    
    GAMEUI --> GAMEVM
    MENUUI --> MENUVM
    DIALOGUI --> DIALOGVM
    
    GAMEVM --> NETWORKSVC
    GAMEVM --> GAMESVC
    GAMEVM --> VALIDATIONSVC
    
    GAMESVC --> GAMEMODEL
    GAMESVC --> BOARDMODEL
    VALIDATIONSVC --> PIECEMODEL
    
    NETWORKSVC -.->|Network Events| GAMEVM
    GAMESVC -.->|Game Events| GAMEVM
    GAMEVM -.->|UI Updates| GAMEUI
```

### Server-Side Component Interaction

```mermaid
graph TB
    subgraph "Network Layer"
        SERVERSOCKET[ServerSocket]
        CLIENTSOCKET[Client Sockets]
        THREADPOOL[Thread Pool]
    end
    
    subgraph "Game Management"
        SERVER[Game Server]
        HANDLER[Client Handlers]
        GAMEMANAGER[Game Manager]
    end
    
    subgraph "Game Logic"
        VALIDATOR[Move Validator]
        GAMESTATE[Game State]
        RULESENGINE[Rules Engine]
    end
    
    subgraph "Communication"
        MESSAGEPARSER[Message Parser]
        BROADCASTER[Message Broadcaster]
    end
    
    SERVERSOCKET --> CLIENTSOCKET
    CLIENTSOCKET --> THREADPOOL
    THREADPOOL --> HANDLER
    
    SERVER --> HANDLER
    SERVER --> GAMEMANAGER
    HANDLER --> VALIDATOR
    HANDLER --> GAMESTATE
    
    VALIDATOR --> RULESENGINE
    GAMESTATE --> RULESENGINE
    
    HANDLER --> MESSAGEPARSER
    HANDLER --> BROADCASTER
    BROADCASTER --> CLIENTSOCKET
```

## Technology Stack

- **Frontend**: JavaFX 22 with FXML and CSS
- **Backend**: Java 22 with Socket Programming
- **Architecture**: MVVM Pattern
- **Concurrency**: ExecutorService and Thread Pools
- **Build Tool**: Maven
- **Module System**: Java Platform Module System (JPMS)

## Key Design Decisions

1. **MVVM Pattern**: Ensures clear separation between UI and business logic
2. **Socket-based Networking**: Direct TCP communication for real-time gameplay
3. **Thread-per-Client**: Each client gets dedicated handler thread
4. **Centralized Game State**: Server maintains authoritative game state
5. **Event-driven Architecture**: UI updates through event listeners
6. **Modular Design**: Clear separation of chess logic, networking, and UI concerns

This architecture supports scalable multiplayer chess gameplay with clean code organization and maintainable design patterns.
