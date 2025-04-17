package org.example.simulation;

import org.example.Main;
import org.example.parsing.Record;
import org.example.parsing.Move;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;
import org.example.simulation.pieces.*;

import java.util.ArrayList;
import java.util.List;

import static org.example.simulation.pieces.attributes.Color.black;
import static org.example.simulation.pieces.attributes.Color.white;

public class GameSimulator {
    private Piece[][] board;

    public Piece[][] getBoard() {
        return board;
    }

    private King whiteKing = new King(new Position('e',1), white);
    private King blackKing = new King(new Position('e',8), black);

    public King getWhiteKing() {
        return whiteKing;
    }

    public King getBlackKing() {
        return blackKing;
    }

    private final Record record;

    public GameSimulator(Record record) {
        // Initialize the chess board with starting positions
        board = new Piece[8][8];

        // Set up white pieces (bottom row in traditional chess notation)
        board[0][0] = new Rook(new Position('a', 1), white);
        board[0][1] = new Knight(new Position('b', 1), white);
        board[0][2] = new Bishop(new Position('c', 1), white);
        board[0][3] = new Queen(new Position('d', 1), white);
        board[0][4] = whiteKing;
        board[0][5] = new Bishop(new Position('f', 1), white);
        board[0][6] = new Knight(new Position('g', 1), white);
        board[0][7] = new Rook(new Position('h', 1), white);

        // Set up white pawns (second row)
        for (int i = 0; i < 8; i++) {
            char file = (char)('a' + i);
            board[1][i] = new Pawn(new Position(file, 2), white);
        }

        // Set up empty middle of the board (rows 3-6)
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }

        // Set up black pawns (seventh row)
        for (int i = 0; i < 8; i++) {
            char file = (char)('a' + i);
            board[6][i] = new Pawn(new Position(file, 7), black);
        }

        // Set up black pieces (top row)
        board[7][0] = new Rook(new Position('a', 8), black);
        board[7][1] = new Knight(new Position('b', 8), black);
        board[7][2] = new Bishop(new Position('c', 8), black);
        board[7][3] = new Queen(new Position('d', 8), black);
        board[7][4] = blackKing;
        board[7][5] = new Bishop(new Position('f', 8), black);
        board[7][6] = new Knight(new Position('g', 8), black);
        board[7][7] = new Rook(new Position('h', 8), black);

        // Store the game record
        this.record = record;
    }

    public void runGame() {
        if (record == null) {
            System.out.println("Cannot make simulation");
            return;
        }

        System.out.println(record.getTags());
        List<Move> moves = record.getMoves();

        if (moves == null || moves.isEmpty()) {
            System.out.println("No moves to simulate");
            return;
        }

        System.out.println("Starting game simulation with " + moves.size() + " moves");

        int count = 1;
        for (Move move : moves) {
            // no move left, meaning game ended
            if(move == null){
                Main.countMoves += 1;
                System.out.println("Game ended successfully");
                return;
            }
            System.out.println("Processing move: " + move.getAction() + " by " + move.getColor());

            // Handle castling
            if (move.isKingSideCastling()) {
                if (!handleKingSideCastling(move.getColor())) {
                    System.out.println("Error: Invalid king-side castling");
                    return;
                }
                continue;
            }

            if (move.isQueenSideCastling()) {
                if (!handleQueenSideCastling(move.getColor())) {
                    System.out.println("Error: Invalid queen-side castling");
                    return;
                }
                continue;
            }

            // Identify which piece to move
            Piece currentPiece = identifyPiece(move);

            if (currentPiece == null) {
                System.out.println("Error: Cannot identify piece for move " + move.getAction());
                return;
            }

            System.out.println("Identified piece: " + currentPiece.getClass().getSimpleName() +
                    " at " + currentPiece.getPosition());

            // Validate the move
            Position newPosition = move.getNewPosition();

            // Check for capture
            if (move.isCapture() == (board[newPosition.getX()][newPosition.getY()] == null)) {
                System.out.println("Error: Capture status mismatch");
                return;
            }

            System.out.println(currentPiece.getClass().toString());

            // Check for file/rank ambiguity
            if(!(currentPiece instanceof Pawn))
                if (currentPiece.needsFileDisambiguation(board, move.getNewPosition()) != move.isCharAmb()) {
                    System.out.println("Error: Wrong file ambiguity");
                    return;
                }

            // check for rank ambiguity
            if(!(currentPiece instanceof  Pawn))
                if (currentPiece.needsRankDisambiguation(board, move.getNewPosition()) != move.isDigitAmb()) {
                    System.out.println("Error: Wrong rank ambiguity");
                    return;
                }


            // Handle promotion
            if (move.isPromotion()) {
                Piece promotedPiece = getPiece(move.getPromoted(), currentPiece.getPosition(), currentPiece.getColor());
                if (promotedPiece == null) {
                    System.out.println("Error: Invalid promotion piece");
                    return;
                }

                // Move the promoted piece
                movePiece(promotedPiece, board, newPosition);
                System.out.println("Promoted pawn to " + promotedPiece.getClass().getSimpleName());
            } else {
                // Move the piece
                movePiece(currentPiece, board, newPosition);
            }

            // Verify check and checkmate
            King opponentKing = (move.getColor() == white) ? blackKing : whiteKing;
            if (move.isCheck() != isInCheck(opponentKing)) {
                System.out.println("Error: Check status mismatch");
                return;
            }

            if(count == moves.size())
                if (move.isCheckmate() != isCheckmate(opponentKing)) {
                    System.out.println("Error: Checkmate status mismatch");
                    return;
                }

            count++;
            System.out.println(count);
            System.out.println(moves.size());
            new Display().printBoard(board);
        }
        System.out.println("Successful game");
    }

    // todo checkmate checking at the end of the game
    // if there is no move check board for checkmate
    //

    private boolean handleKingSideCastling(Color color) {
        King king = (color == white) ? whiteKing : blackKing;
        int row = (color == white) ? 0 : 7;

        // Check if castling is legal
        if (!canCastleKingSide(king, row)) {
            return false;
        }

        // Move king
        board[row][4] = null;
        board[row][6] = king;
        king.setPosition(new Position((char)('e' + 2), row + 1));

        // Move rook
        Rook rook = (Rook) board[row][7];
        board[row][7] = null;
        board[row][5] = rook;
        rook.setPosition(new Position((char)('h' - 2), row + 1));

        System.out.println("Executed king-side castling for " + color);
        return true;
    }

    private boolean handleQueenSideCastling(Color color) {
        King king = (color == white) ? whiteKing : blackKing;
        int row = (color == white) ? 0 : 7;

        // Check if castling is legal
        if (!canCastleQueenSide(king, row)) {
            return false;
        }

        // Move king
        board[row][4] = null;
        board[row][2] = king;
        king.setPosition(new Position((char)('e' - 2), row + 1));

        // Move rook
        Rook rook = (Rook) board[row][0];
        board[row][0] = null;
        board[row][3] = rook;
        rook.setPosition(new Position((char)('a' + 3), row + 1));

        System.out.println("Executed queen-side castling for " + color);
        return true;
    }

    private boolean canCastleKingSide(King king, int row) {
        // Check if king and rook are in their initial positions
        if (!(board[row][4] instanceof King) ||
                !(board[row][7] instanceof Rook)) {
            return false;
        }

        // Check if squares between king and rook are empty
        if (board[row][5] != null || board[row][6] != null) {
            return false;
        }

        // Check if king is not in check
        if (isInCheck(king)) {
            return false;
        }

        // Check if king would pass through check
        // This is a simplified check - a full implementation would be more complex
        Position tempPos = new Position((char)('e' + 1), row + 1);
        king.setPosition(tempPos);
        boolean passesCheck = isInCheck(king);
        king.setPosition(new Position('e', row + 1));

        if (passesCheck) {
            return false;
        }

        return true;
    }

    private boolean canCastleQueenSide(King king, int row) {
        // Check if king and rook are in their initial positions
        if (!(board[row][4] instanceof King) ||
                !(board[row][0] instanceof Rook)) {
            return false;
        }

        // Check if squares between king and rook are empty
        if (board[row][1] != null || board[row][2] != null || board[row][3] != null) {
            return false;
        }

        // Check if king is not in check
        if (isInCheck(king)) {
            return false;
        }

        // Check if king would pass through check
        // This is a simplified check - a full implementation would be more complex
        Position tempPos = new Position((char)('e' - 1), row + 1);
        king.setPosition(tempPos);
        boolean passesCheck = isInCheck(king);
        king.setPosition(new Position('e', row + 1));

        if (passesCheck) {
            return false;
        }

        return true;
    }

    private Piece identifyPiece(Move move) {
        System.out.println("---data about move---");
        System.out.println(move.getAction());
        System.out.println("---------------------");

        char pieceType = move.getPiece();
        Color color = move.getColor();
        Position targetPos = move.getNewPosition();



        // Special case for knight moves with file disambiguation (like "Nhf6")
        if (pieceType == 'N' && move.isCharAmb() && !move.isDigitAmb()) {
            char fileHint = move.getFile();
            targetPos = move.getNewPosition();

            // Convert file character to array index (a=0, b=1, etc.)
            int fileIndex = fileHint - 'a';

            // Look for knights on the specified file
            for (int rankIndex = 0; rankIndex < 8; rankIndex++) {
                if (fileIndex >= 0 && fileIndex < 8 &&
                        board[rankIndex][fileIndex] instanceof Knight &&
                        board[rankIndex][fileIndex].getColor() == color &&
                        board[rankIndex][fileIndex].canGo(targetPos) &&
                        isPathClear(board[rankIndex][fileIndex], targetPos)) {

                    return board[rankIndex][fileIndex];
                }
            }
        }

        // Special case for knight moves with rank disambiguation (like "N6d7")
        if (pieceType == 'N' && !move.isCharAmb() && move.isDigitAmb()) {
            int rankHint = move.getRank();
            targetPos = move.getNewPosition();

            // Convert rank number to array index (1→0, 2→1, etc.)
            int rankIndex = rankHint - 1;

            // Look for knights on the specified rank
            for (int fileIndex = 0; fileIndex < 8; fileIndex++) {
                if (rankIndex >= 0 && rankIndex < 8 &&
                        board[rankIndex][fileIndex] instanceof Knight &&
                        board[rankIndex][fileIndex].getColor() == color &&
                        board[rankIndex][fileIndex].canGo(targetPos) &&
                        isPathClear(board[rankIndex][fileIndex], targetPos)) {

                    return board[rankIndex][fileIndex];
                }
            }
        }

        // Special case for knight moves with both file and rank disambiguation (like "Nf6d7")
        if (pieceType == 'N' && move.isCharAmb() && move.isDigitAmb()) {
            char fileHint = move.getFile();
            int rankHint = move.getRank();
            targetPos = move.getNewPosition();

            // Convert to array indices
            int fileIndex = fileHint - 'a';
            int rankIndex = rankHint - 1;

            // Check the specific square
            if (fileIndex >= 0 && fileIndex < 8 && rankIndex >= 0 && rankIndex < 8 &&
                    board[rankIndex][fileIndex] instanceof Knight &&
                    board[rankIndex][fileIndex].getColor() == color &&
                    board[rankIndex][fileIndex].canGo(targetPos) &&
                    isPathClear(board[rankIndex][fileIndex], targetPos)) {

                return board[rankIndex][fileIndex];
            }
        }

        // Special case for pawns moving forward
        if (pieceType == 'P' && !move.isCapture()) {
            // For standard pawn moves, the file of the move is the file of the pawn
            char targetFile = targetPos.getFile();
            int targetRank = targetPos.getRank();
            int direction = (color == Color.white) ? -1 : 1;

            // Check one square behind (or two for initial double move)
            for (int offset = 1; offset <= 2; offset++) {
                int sourceRank = targetRank + (direction * offset);

                // Check if source rank is valid
                if (sourceRank < 1 || sourceRank > 8) continue;

                // Get board indices
                int x = sourceRank - 1;
                int y = targetFile - 'a';

                // Make sure we're in bounds
                if (x < 0 || x >= 8 || y < 0 || y >= 8) continue;

                // Check if there's a pawn at this position
                if (board[x][y] instanceof Pawn &&
                        board[x][y].getColor() == color) {

                    // For double moves, check if it's from starting position
                    if (offset == 2) {
                        boolean isInitialRank = (color == Color.white && sourceRank == 2) ||
                                (color == Color.black && sourceRank == 7);

                        // Check if the square in between is empty
                        int middleRank = targetRank + direction;
                        int middleX = middleRank - 1;

                        if (isInitialRank && board[middleX][y] == null) {
                            return board[x][y];
                        }
                    } else {
                        // For single moves, just return the pawn
                        return board[x][y];
                    }
                }
            }
        }

        // Pawn captures require special handling
        if (pieceType == 'P' && move.isCapture()) {
            char targetFile = targetPos.getFile();
            int targetRank = targetPos.getRank();
            int direction = (color == Color.white) ? -1 : 1;

            // For pawn captures, source file is explicitly provided
            // No need to check both possible capture origins
            char sourceFile = move.getFile();  // This is the file from which the pawn is capturing
            int sourceRank = targetRank + direction;  // Pawn must be one rank away in the appropriate direction

            // Convert to array indices
            int x = sourceRank - 1;
            int y = sourceFile - 'a';

            // Make sure we're in bounds
            if (x >= 0 && x < 8 && y >= 0 && y < 8) {
                // Check if there's a pawn at this position
                if (board[x][y] instanceof Pawn && board[x][y].getColor() == color) {
                    return board[x][y];
                }
            }

            // If we can't find the pawn where expected, fall back to checking adjacent files
            // (This is a safety mechanism, but with correct PGN notation, we shouldn't need it)
            sourceRank = targetRank + direction;
            for (int fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                char potentialFile = (char)(targetFile + fileOffset);

                // Skip if outside board
                if (potentialFile < 'a' || potentialFile > 'h') continue;

                int potentialX = sourceRank - 1;
                int potentialY = potentialFile - 'a';

                if (potentialX >= 0 && potentialX < 8 && potentialY >= 0 && potentialY < 8 &&
                        board[potentialX][potentialY] instanceof Pawn &&
                        board[potentialX][potentialY].getColor() == color) {
                    return board[potentialX][potentialY];
                }
            }
        }

        // For other pieces, use the existing logic
        List<Piece> candidates = new ArrayList<>();

        // Find all pieces of the given type and color that could potentially move to the target
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece == null || piece.getColor() != color) {
                    continue;
                }

                boolean isRightType = false;
                switch (pieceType) {
                    case 'P': isRightType = piece instanceof Pawn; break;
                    case 'R': isRightType = piece instanceof Rook; break;
                    case 'N': isRightType = piece instanceof Knight; break;
                    case 'B': isRightType = piece instanceof Bishop; break;
                    case 'Q': isRightType = piece instanceof Queen; break;
                    case 'K': isRightType = piece instanceof King; break;
                }

                if (isRightType && piece.canGo(targetPos) && isPathClear(piece, targetPos)) {
                    candidates.add(piece);
                }
            }
        }

        System.out.println(candidates.size());

        if (candidates.isEmpty()) {
            return null;
        }

        if (candidates.size() == 1) {
            return candidates.get(0);
        }

        // Handle disambiguation
        char fileHint = move.getFile();
        int rankHint = move.getRank();

        for (Piece candidate : candidates) {
            Position pos = candidate.getPosition();

            if (fileHint != '\u0000' && pos.getFile() != fileHint) {
                continue;
            }

            if (rankHint != 0 && pos.getRank() != rankHint) {
                continue;
            }

            return candidate;
        }

        return null;
    }

    private boolean isPathClear(Piece piece, Position targetPos) {
        // Knights can jump, so no path checking needed
        if (piece instanceof Knight) {
            return true;
        }

        int startX = piece.getPosition().getX();
        int startY = piece.getPosition().getY();
        int endX = targetPos.getX();
        int endY = targetPos.getY();

        // Determine direction
        int dx = Integer.compare(endX - startX, 0);
        int dy = Integer.compare(endY - startY, 0);

        int x = startX + dx;
        int y = startY + dy;

        // Check all squares between start and end (excluding start and end)
        while (x != endX || y != endY) {
            if (board[x][y] != null) {
                return false; // Path is blocked
            }

            x += dx;
            y += dy;
        }

        return true;
    }

    private boolean isInCheck(King king) {
        Position kingPos = king.getPosition();
        Color kingColor = king.getColor();

        // Check if any opponent piece can attack the king
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() != kingColor) {
                    if (piece.canGo(kingPos) && isPathClear(piece, kingPos)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean isCheckmate(King king) {
        // If not in check, not checkmate
        if (!isInCheck(king)) {
            return false;
        }

        // Check if king can move to any adjacent square
        int kingX = king.getPosition().getX();
        int kingY = king.getPosition().getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int newX = kingX + dx;
                int newY = kingY + dy;

                // Check if new position is on the board
                if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) {
                    continue;
                }

                // Check if square is empty or has opponent's piece
                if (board[newX][newY] == null || board[newX][newY].getColor() != king.getColor()) {
                    // Try moving king there
                    Piece savedPiece = board[newX][newY];
                    board[newX][newY] = king;
                    board[kingX][kingY] = null;

                    Position oldPos = king.getPosition();
                    king.setPosition(new Position((char)('a' + newY), newX + 1));

                    boolean stillInCheck = isInCheck(king);

                    // Restore board
                    king.setPosition(oldPos);
                    board[kingX][kingY] = king;
                    board[newX][newY] = savedPiece;

                    if (!stillInCheck) {
                        return false; // King can escape
                    }
                }
            }
        }

        // Check if any friendly piece can block or capture the checking piece
        // This is a simplified implementation - a full implementation would check all possible moves
        // For now, we'll just assume it's checkmate if the king can't move

        return true;
    }

    private Piece getPiece(char pieceType, Position position, Color color) {
        return switch (pieceType) {
            case 'Q' -> new Queen(position, color);
            case 'B' -> new Bishop(position, color);
            case 'R' -> new Rook(position, color);
            case 'N' -> new Knight(position, color);
            case 'K' -> new King(position, color);
            default -> null;
        };
    }

    private void movePiece(Piece piece, Piece[][] board, Position newPosition) {
        // Update board
        int oldX = piece.getPosition().getX();
        int oldY = piece.getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        board[newX][newY] = piece;
        board[oldX][oldY] = null;

        // Update piece's internal position
        piece.setPosition(newPosition);

        System.out.println("Moved " + piece.getClass().getSimpleName() +
                " from " + (char)('a' + oldY) + (oldX + 1) +
                " to " + newPosition);
    }

    // Checkmate helper method - simplified version from your original code
    private boolean checkmate(Piece currentPiece) {
        return true; // Simplified implementation
    }
}