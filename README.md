# Nexus Board

A multiplayer chess application built with Java and JavaFX featuring networked gameplay.

## Features

-  Online multiplayer chess
- 🎨 Modern JavaFX interface
- 📊 Real-time game state synchronization
- � Client-server architecture

## Technologies

- **Java 22** with JavaFX
- **Maven** for build management
- **MVVM** architecture pattern
- **Socket-based** networking

## Project Structure

```
src/main/java/com/himelz/nexusboard/
├── app/                    # Application entry point
├── model/                  # Game logic and data models
│   ├── board/             # Chess board implementation
│   ├── pieces/            # Chess piece classes
│   ├── Color.java         # Player color enum
│   └── GameState.java     # Game state management
├── network/               # Client-server networking
│   ├── Client.java        # Game client
│   ├── Server.java        # Game server
│   └── ClientHandler.java # Server-side client handling
├── viewController/        # JavaFX UI controllers
├── viewmodel/            # MVVM view models
└── utils/                # Utilities (FEN, validation)

src/main/resources/com/himelz/nexusboard/nexusboard/
├── images/               # Chess piece graphics
├── screens/              # FXML view files
└── styles/              # CSS stylesheets
```

## Quick Start

### Prerequisites
- JDK 22+
- Maven 3.6+

### Run
```bash
mvn clean javafx:run
```

### Build
```bash
mvn clean package
```

## Game Modes

- **Host Game**: Start a server and wait for players
- **Join Game**: Connect to an existing game server
- **Local Play**: Play on the same machine

## Architecture

Uses MVVM pattern with JavaFX for clean separation of concerns. Network layer handles client-server communication for multiplayer games.
