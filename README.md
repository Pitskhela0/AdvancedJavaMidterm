# Chess PGN Parser and Validator

A Java application that parses chess games in Portable Game Notation (PGN) format, validates their syntax, and verifies the correctness of the moves according to chess rules.

## Project Overview

This project implements a chess game parser and simulator that can:
1. Read chess games from PGN files
2. Parse and validate the PGN syntax
3. Simulate the games to verify move legality
4. Detect check, checkmate, and other game conditions
5. Report any errors found in the PGN notation or move execution

The application is useful for validating chess database files, detecting errors in game records, and ensuring that recorded games follow the rules of chess.

## Features

- **PGN Parsing**: Parse standard PGN format with tags, movetext, comments, and annotations
- **Move Validation**: Verify that all moves in the game follow the rules of chess
- **Complete Chess Rules**: Support for all standard chess moves:
    - Basic piece movements
    - Castling (kingside and queenside)
    - En passant captures
    - Pawn promotion
    - Check and checkmate detection
- **Error Detection**: Identifies and reports:
    - Syntax errors in PGN format
    - Missing or duplicate rounds
    - Illegal moves
    - Inconsistencies between move notation and game state (e.g., check not indicated)
- **Multiple Game Support**: Process multiple games from a single PGN file

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven for dependency management and building

### Installation

1. Clone the repository:
   ```
   git clone https://github.com/Pitskhela0/AdvancedJavaMidterm.git
   cd chess-pgn-validator
   ```

2. Build the project:
   ```
   mvn clean package
   ```

### Usage

Run the application with a PGN file as an argument:

```
java -jar out/artifacts/ChessGame_jar/ChessGame.jar path/to/your/file.pgn
```

#### Example Output

When processing a PGN file, the application will output messages like:

```
Processing file: example.pgn
Game 1: Successful
Game 2: Error at move 15.Nxf7+ - Check status mismatch
Game 3: Successful
Game 4: Error at move 3.e4 - Missing round
Successfully completed 78 moves in valid games
```

## Project Structure

The project is organized into several packages:

- `org.example.parsing`: Classes for parsing PGN files
    - `GameParser`: Main parser for PGN files
    - `Move`: Represents a single chess move
    - `Record`: Represents a complete chess game with moves and metadata

- `org.example.simulation`: Classes for simulating chess games
    - `GameSimulator`: Simulates and validates chess games
    - `ChessUtils`: Utility functions for chess game validation
    - `Display`: Visualizes the chess board in text format

- `org.example.simulation.pieces`: Classes for each chess piece
    - `Piece`: Abstract base class for all chess pieces
    - `Pawn`, `Rook`, `Knight`, `Bishop`, `Queen`, `King`: Concrete piece implementations

## Sample Input and Output

### Example PGN Input

```
[Event "Wch27"]
[Site "Moscow"]
[Date "1969.??.??"]
[Round "1"]
[White "Petrosian, Tigran V"]
[Black "Spassky, Boris V"]
[Result "1/2-1/2"]

1. c4 e6 2. Nf3 d5 3. d4 Nf6 4. Nc3 Be7 5. Bg5 O-O 6. e3 h6 7. Bh4 b6 8. cxd5
Nxd5 9. Bxe7 Qxe7 10. Nxd5 exd5 11. Rc1 Be6 12. Qa4 c5 13. Qa3 Rc8 14. Bb5 a6
15. dxc5 bxc5 16. O-O Ra7 17. Be2 Nd7 18. Nd4 Qf8 19. Nxe6 fxe6 20. e4 d4
21. f4 Qe8 22. e5 Rb8 23. Bc4 Kh8 24. Qh3 Nf8 25. b3 a5 26. f5 exf5 27. Rxf5
Nh7