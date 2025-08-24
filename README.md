# Nexus Board - Chess Application

A feature-rich chess application built with Java and JavaFX, featuring both single-player and multiplayer modes.

## Table of Contents
- [Features](#features)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
- [Game Features](#game-features)
- [Multiplayer](#multiplayer)
- [Build & Run](#build--run)
- [License](#license)

## Features

- 🎮 Play chess against AI or human opponent
- 🌐 Online multiplayer support
- 🎨 Modern, responsive UI with drag-and-drop piece movement
- 📊 Move validation and game state tracking
- 📝 Move history and game recording (PGN support)
- 🎨 Customizable board and piece themes

## Technologies

- **Language**: Java 17+
- **UI Framework**: JavaFX 17+
- **Build Tool**: Maven
- **Architecture**: MVVM (Model-View-ViewModel)
- **Networking**: Java Sockets

## Project Structure

```
src/
└── main/
    ├── java/
    │    └── com.himelz.nexusboard/
    │         ├── app/                   # Application entry point and core classes
    │         │    ├── ChessApplication.java
    │         │    └── module-info.java
    │         │
    │         ├── model/                 # Game logic and data models
    │         │    ├── board/            # Board representation and logic
    │         │    ├── pieces/           # Chess piece implementations
    │         │    └── GameState.java    # Game state management
    │         │
    │         ├── viewController/        # JavaFX controllers for UI
    │         ├── viewmodel/             # ViewModel layer for MVVM
    │         ├── network/               # Multiplayer networking components
    │         └── utils/                 # Helper and utility classes
    │
    └── resources/                       # Static resources
         └── com.himelz.nexusboard/
              └── nexusboard/
                   ├── screens/          # FXML view files
                   │    ├── HomeScreen.fxml
                   │    └── LandingScreen.fxml
                   ├── css/              # Stylesheets
                   └── images/           # Game assets and piece images
```

## Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.6.3 or higher
- JavaFX 17+

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/itshimelz/nexus-board.git
   cd Nexus-Board
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Game Features

### Single Player
- Play against a computer opponent
- Multiple difficulty levels
- Undo/Redo moves
- Game analysis

### Multiplayer
- Local two-player mode
- Online multiplayer with lobby system
- Chat functionality
- Game history and statistics

## Build & Run

### Development Mode
```bash
mvn clean javafx:run
```

### Create Executable JAR
```bash
mvn clean package
java -jar target/nexus-board-1.0.0-jar-with-dependencies.jar
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Chess piece icons by [Wikimedia Commons](https://commons.wikimedia.org/wiki/Category:SVG_chess_pieces)
- Inspired by popular chess applications like Lichess and Chess.com
