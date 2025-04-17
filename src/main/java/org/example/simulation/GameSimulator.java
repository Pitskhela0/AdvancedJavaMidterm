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
    private King whiteKing = new King(new Position('e',1), white);
    private King blackKing = new King(new Position('e',8), black);
    private final Record record;

    public Piece[][] getBoard() { return board; }
    public King getWhiteKing() { return whiteKing; }
    public King getBlackKing() { return blackKing; }

    public GameSimulator(Record record) {
        this.record = record;
        initializeBoard();
    }

    private void initializeBoard() {
        board = new Piece[8][8];

        // Set up white pieces
        board[0][0] = new Rook(new Position('a', 1), white);
        board[0][1] = new Knight(new Position('b', 1), white);
        board[0][2] = new Bishop(new Position('c', 1), white);
        board[0][3] = new Queen(new Position('d', 1), white);
        board[0][4] = whiteKing;
        board[0][5] = new Bishop(new Position('f', 1), white);
        board[0][6] = new Knight(new Position('g', 1), white);
        board[0][7] = new Rook(new Position('h', 1), white);

        // Set up white pawns
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn(new Position((char)('a' + i), 2), white);
        }

        // Empty middle of the board
        for (int row = 2; row < 6; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        }

        // Set up black pawns
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Pawn(new Position((char)('a' + i), 7), black);
        }

        // Set up black pieces
        board[7][0] = new Rook(new Position('a', 8), black);
        board[7][1] = new Knight(new Position('b', 8), black);
        board[7][2] = new Bishop(new Position('c', 8), black);
        board[7][3] = new Queen(new Position('d', 8), black);
        board[7][4] = blackKing;
        board[7][5] = new Bishop(new Position('f', 8), black);
        board[7][6] = new Knight(new Position('g', 8), black);
        board[7][7] = new Rook(new Position('h', 8), black);
    }

    public void runGame() {
        if (record == null) {
            System.out.println("Cannot make simulation");
            return;
        }

        List<Move> moves = record.getMoves();
        if (moves == null || moves.isEmpty()) {
            System.out.println("No moves to simulate");
            return;
        }

        int count = 1;
        for (Move move : moves) {
            if (move == null) break;

            // Process each move
            if (!processMove(move)) {
                return; // Error encountered
            }

            count++;
            Main.countMoves++;
        }

        // Game completed successfully
        analyzeGameResult(count-1, moves.size());
    }

    private boolean processMove(Move move) {
        // Handle castling
        if (move.isKingSideCastling()) {
            return handleKingSideCastling(move.getColor());
        }

        if (move.isQueenSideCastling()) {
            return handleQueenSideCastling(move.getColor());
        }

        // Regular move
        Piece currentPiece = identifyPiece(move);
        if (currentPiece == null) {
            System.out.println("Error: Cannot identify piece for move " + move.getAction());
            return false;
        }

        // Validate move
        Position newPosition = move.getNewPosition();
        if (!validateMove(move, currentPiece, newPosition)) {
            return false;
        }

        // Execute move
        if (move.isPromotion()) {
            executePromotion(move, currentPiece, newPosition);
        } else {
            ChessUtils.movePiece(board, currentPiece, newPosition);
        }

        // Verify check status
        King opponentKing = (move.getColor() == white) ? blackKing : whiteKing;
        boolean actualCheckStatus = ChessUtils.isInCheck(board, opponentKing);

        if (move.isCheck() != actualCheckStatus) {
            System.out.println("Error: Check status mismatch for move " + move.getAction());
            return false;
        }

        // Verify checkmate if claimed
        if (move.isCheckmate() && !ChessUtils.isCheckmate(board, opponentKing)) {
            System.out.println("Error: Checkmate status mismatch for move " + move.getAction());
            return false;
        }

        return true;
    }

    private boolean validateMove(Move move, Piece piece, Position newPosition) {
        // Check for capture
        if (move.isCapture() == (board[newPosition.getX()][newPosition.getY()] == null)) {
            System.out.println("Error: Capture status mismatch for move " + move.getAction());
            return false;
        }

        // Check for file/rank ambiguity (skip for pawns)
        if (!(piece instanceof Pawn)) {
            if (piece.needsFileDisambiguation(board, newPosition) != move.isCharAmb()) {
                System.out.println("Error: Wrong file ambiguity");
                return false;
            }

            if (piece.needsRankDisambiguation(board, newPosition) != move.isDigitAmb()) {
                System.out.println("Error: Wrong rank ambiguity");
                return false;
            }
        }

        return true;
    }

    private void executePromotion(Move move, Piece pawn, Position newPosition) {
        Piece promotedPiece = ChessUtils.createPiece(move.getPromoted(), pawn.getPosition(), pawn.getColor());
        if (promotedPiece == null) {
            System.out.println("Error: Invalid promotion piece");
            return;
        }

        ChessUtils.movePiece(board, promotedPiece, newPosition);
        System.out.println("Promoted pawn to " + promotedPiece.getClass().getSimpleName());
    }

    private void analyzeGameResult(int movesCompleted, int totalMoves) {
        System.out.println("Successfully completed " + movesCompleted + " moves");

        // Determine the final game state
        Color lastMoveColor = (movesCompleted % 2 == 0) ? black : white;
        Color nextToMove = (lastMoveColor == white) ? black : white;
        King kingToCheck = (nextToMove == white) ? whiteKing : blackKing;

        // Display final board state
        System.out.println("Final board state:");
        new Display().printBoard(board);

        // Optional: Enable for detailed debugging
        // ChessUtils.debugCheckmate(board, kingToCheck);

        boolean isCheck = ChessUtils.isInCheck(board, kingToCheck);
        boolean isCheckmate = ChessUtils.isCheckmate(board, kingToCheck);
        String pgnResult = record.getResult();

        // Report final result
        if (isCheckmate) {
            System.out.println("Game ended in checkmate. " + lastMoveColor + " wins!");
            verifyResult(lastMoveColor, pgnResult);
        } else if (isCheck) {
            System.out.println("King is in check but not checkmate.");
            if ("white".equals(pgnResult) || "black".equals(pgnResult)) {
                System.out.println("Warning: Game ended without checkmate but PGN indicates a winner.");
            }
        } else {
            reportNonCheckmateEnding(pgnResult);
        }
    }

    private void verifyResult(Color winner, String pgnResult) {
        boolean resultsMatch =
                (winner == white && "white".equals(pgnResult)) ||
                        (winner == black && "black".equals(pgnResult));

        if (!resultsMatch) {
            System.out.println("Warning: Checkmate detection shows " + winner +
                    " won, but PGN indicates " + pgnResult);
        }
    }

    private void reportNonCheckmateEnding(String pgnResult) {
        if ("draw".equals(pgnResult)) {
            System.out.println("Game ended in a draw according to PGN.");
        } else if ("white".equals(pgnResult)) {
            System.out.println("Game ended with white winning according to PGN (not by checkmate).");
            System.out.println("This could indicate a resignation or a time forfeit.");
        } else if ("black".equals(pgnResult)) {
            System.out.println("Game ended with black winning according to PGN (not by checkmate).");
            System.out.println("This could indicate a resignation or a time forfeit.");
        } else {
            System.out.println("Game ended without checkmate. PGN Result: " + pgnResult);
        }
    }

    private boolean handleKingSideCastling(Color color) {
        King king = (color == white) ? whiteKing : blackKing;
        int row = (color == white) ? 0 : 7;

        if (!canCastleKingSide(king, row)) {
            System.out.println("Error: Invalid king-side castling");
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

        return true;
    }

    private boolean handleQueenSideCastling(Color color) {
        King king = (color == white) ? whiteKing : blackKing;
        int row = (color == white) ? 0 : 7;

        if (!canCastleQueenSide(king, row)) {
            System.out.println("Error: Invalid queen-side castling");
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

        return true;
    }

    private boolean canCastleKingSide(King king, int row) {
        // Check if king and rook are in their initial positions
        if (!(board[row][4] instanceof King) || !(board[row][7] instanceof Rook)) {
            return false;
        }

        // Check if squares between are empty
        if (board[row][5] != null || board[row][6] != null) {
            return false;
        }

        // Check if king is not in check and would not pass through check
        if (ChessUtils.isInCheck(board, king)) {
            return false;
        }

        Position tempPos = new Position((char)('e' + 1), row + 1);
        king.setPosition(tempPos);
        boolean passesCheck = ChessUtils.isInCheck(board, king);
        king.setPosition(new Position('e', row + 1));

        return !passesCheck;
    }

    private boolean canCastleQueenSide(King king, int row) {
        // Check if king and rook are in their initial positions
        if (!(board[row][4] instanceof King) || !(board[row][0] instanceof Rook)) {
            return false;
        }

        // Check if squares between are empty
        if (board[row][1] != null || board[row][2] != null || board[row][3] != null) {
            return false;
        }

        // Check if king is not in check and would not pass through check
        if (ChessUtils.isInCheck(board, king)) {
            return false;
        }

        Position tempPos = new Position((char)('e' - 1), row + 1);
        king.setPosition(tempPos);
        boolean passesCheck = ChessUtils.isInCheck(board, king);
        king.setPosition(new Position('e', row + 1));

        return !passesCheck;
    }

    private Piece identifyPiece(Move move) {
        char pieceType = move.getPiece();
        Color color = move.getColor();
        Position targetPos = move.getNewPosition();

        // Handle special cases for different piece types
        if (pieceType == 'N') {
            Piece knight = findKnight(move, color, targetPos);
            if (knight != null) return knight;
        }

        // Special case for pawns
        if (pieceType == 'P') {
            Piece pawn = findPawn(move, color, targetPos);
            if (pawn != null) return pawn;
        }

        // General case for all pieces
        return findGeneralPiece(move, pieceType, color, targetPos);
    }

    private Piece findKnight(Move move, Color color, Position targetPos) {
        char fileHint = move.getFile();
        int rankHint = move.getRank();

        // Handle file disambiguation
        if (move.isCharAmb() && !move.isDigitAmb()) {
            for (int rankIndex = 0; rankIndex < 8; rankIndex++) {
                int fileIndex = fileHint - 'a';
                if (isValidKnightAt(rankIndex, fileIndex, color, targetPos)) {
                    return board[rankIndex][fileIndex];
                }
            }
        }

        // Handle rank disambiguation
        if (!move.isCharAmb() && move.isDigitAmb()) {
            int rankIndex = rankHint - 1;
            for (int fileIndex = 0; fileIndex < 8; fileIndex++) {
                if (isValidKnightAt(rankIndex, fileIndex, color, targetPos)) {
                    return board[rankIndex][fileIndex];
                }
            }
        }

        // Handle both file and rank disambiguation
        if (move.isCharAmb() && move.isDigitAmb()) {
            int fileIndex = fileHint - 'a';
            int rankIndex = rankHint - 1;
            if (isValidKnightAt(rankIndex, fileIndex, color, targetPos)) {
                return board[rankIndex][fileIndex];
            }
        }

        return null;
    }

    private boolean isValidKnightAt(int rankIndex, int fileIndex, Color color, Position targetPos) {
        return rankIndex >= 0 && rankIndex < 8 && fileIndex >= 0 && fileIndex < 8 &&
                board[rankIndex][fileIndex] instanceof Knight &&
                board[rankIndex][fileIndex].getColor() == color &&
                board[rankIndex][fileIndex].canGo(targetPos) &&
                ChessUtils.isPathClear(board, board[rankIndex][fileIndex], targetPos);
    }

    private Piece findPawn(Move move, Color color, Position targetPos) {
        if (move.isCapture()) {
            return findPawnForCapture(move, color, targetPos);
        } else {
            return findPawnForNormalMove(move, color, targetPos);
        }
    }

    private Piece findPawnForNormalMove(Move move, Color color, Position targetPos) {
        char targetFile = targetPos.getFile();
        int targetRank = targetPos.getRank();
        int direction = (color == Color.white) ? -1 : 1;

        // Check one or two squares behind
        for (int offset = 1; offset <= 2; offset++) {
            int sourceRank = targetRank + (direction * offset);
            if (sourceRank < 1 || sourceRank > 8) continue;

            int x = sourceRank - 1;
            int y = targetFile - 'a';
            if (x < 0 || x >= 8 || y < 0 || y >= 8) continue;

            // Check if there's a pawn at this position
            if (board[x][y] instanceof Pawn && board[x][y].getColor() == color) {
                // For double moves, verify initial position and clear path
                if (offset == 2) {
                    boolean isInitialRank = (color == Color.white && sourceRank == 2) ||
                            (color == Color.black && sourceRank == 7);
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
        return null;
    }

    private Piece findPawnForCapture(Move move, Color color, Position targetPos) {
        char targetFile = targetPos.getFile();
        int targetRank = targetPos.getRank();
        int direction = (color == Color.white) ? -1 : 1;
        char sourceFile = move.getFile();
        int sourceRank = targetRank + direction;

        // Try the explicit source file first
        int x = sourceRank - 1;
        int y = sourceFile - 'a';
        if (x >= 0 && x < 8 && y >= 0 && y < 8) {
            if (board[x][y] instanceof Pawn && board[x][y].getColor() == color) {
                return board[x][y];
            }
        }

        // Fallback: check adjacent files
        for (int fileOffset : new int[] {-1, 1}) {
            char potentialFile = (char)(targetFile + fileOffset);
            if (potentialFile < 'a' || potentialFile > 'h') continue;

            int potentialX = sourceRank - 1;
            int potentialY = potentialFile - 'a';
            if (potentialX < 0 || potentialX >= 8 || potentialY < 0 || potentialY >= 8) continue;

            if (board[potentialX][potentialY] instanceof Pawn &&
                    board[potentialX][potentialY].getColor() == color) {
                return board[potentialX][potentialY];
            }
        }
        return null;
    }

    private Piece findGeneralPiece(Move move, char pieceType, Color color, Position targetPos) {
        List<Piece> candidates = new ArrayList<>();

        // Find all pieces of the given type that could move to the target
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece == null || piece.getColor() != color) continue;

                if (isPieceOfType(piece, pieceType) &&
                        piece.canGo(targetPos) &&
                        ChessUtils.isPathClear(board, piece, targetPos)) {
                    candidates.add(piece);
                }
            }
        }

        if (candidates.isEmpty()) return null;
        if (candidates.size() == 1) return candidates.get(0);

        // Handle disambiguation
        return disambiguatePieces(candidates, move.getFile(), move.getRank());
    }

    private boolean isPieceOfType(Piece piece, char pieceType) {
        return switch (pieceType) {
            case 'P' -> piece instanceof Pawn;
            case 'R' -> piece instanceof Rook;
            case 'N' -> piece instanceof Knight;
            case 'B' -> piece instanceof Bishop;
            case 'Q' -> piece instanceof Queen;
            case 'K' -> piece instanceof King;
            default -> false;
        };
    }

    private Piece disambiguatePieces(List<Piece> candidates, char fileHint, int rankHint) {
        for (Piece candidate : candidates) {
            Position pos = candidate.getPosition();

            // Skip if file hint is provided and doesn't match
            if (fileHint != '\u0000' && pos.getFile() != fileHint) continue;

            // Skip if rank hint is provided and doesn't match
            if (rankHint != 0 && pos.getRank() != rankHint) continue;

            // This candidate matches all disambiguation criteria
            return candidate;
        }
        return null;
    }
}