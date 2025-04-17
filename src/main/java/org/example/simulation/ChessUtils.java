package org.example.simulation;

import org.example.simulation.pieces.*;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class containing helper methods for chess game validation and analysis.
 * This class provides functionality for validating chess moves, checking for check and checkmate conditions,
 * and manipulating pieces on the board.
 */
public class ChessUtils {

    /**
     * Checks if the path between two positions is clear on the given board.
     *
     * @param board The chess board representation
     * @param piece The piece that is moving
     * @param targetPos The target position to move to
     * @return true if the path is clear with no obstacles, false if any piece blocks the path
     */
    public static boolean isPathClear(Piece[][] board, Piece piece, Position targetPos) {
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

        // Check all squares between start and end (excluding start and end)
        for (int x = startX + dx, y = startY + dy; x != endX || y != endY; x += dx, y += dy) {
            if (board[x][y] != null) {
                return false; // Path is blocked
            }
        }

        return true;
    }

    /**
     * Checks if the given king is in check.
     * A king is in check when it is under attack by any opponent's piece.
     *
     * @param board The chess board representation
     * @param king The king to check
     * @return true if the king is in check, false otherwise
     */
    public static boolean isInCheck(Piece[][] board, King king) {
        Position kingPos = king.getPosition();
        Color kingColor = king.getColor();

        // Check if any opponent piece can attack the king
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() != kingColor) {
                    // Special case for pawns
                    if (piece instanceof Pawn) {
                        if (isPawnCheckingKing(piece, kingPos)) {
                            return true;
                        }
                        continue;
                    }

                    // For other pieces
                    if (piece.canGo(kingPos) && isPathClear(board, piece, kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if a pawn is checking the king.
     * Pawns can only check by moving diagonally forward one square.
     *
     * @param pawn The pawn to check
     * @param kingPos The position of the king
     * @return true if the pawn is checking the king, false otherwise
     */
    private static boolean isPawnCheckingKing(Piece pawn, Position kingPos) {
        int pawnX = pawn.getPosition().getX();
        int pawnY = pawn.getPosition().getY();
        int kingX = kingPos.getX();
        int kingY = kingPos.getY();

        // Direction depends on pawn color
        int direction = (pawn.getColor() == Color.white) ? 1 : -1;

        // Pawns can only check diagonally forward
        return (kingX == pawnX + direction) && (Math.abs(kingY - pawnY) == 1);
    }

    /**
     * Determines if the given king is in checkmate.
     * A king is in checkmate when it is in check and has no legal moves to escape check.
     *
     * @param board The chess board representation
     * @param king The king to check
     * @return true if the king is in checkmate, false otherwise
     */
    public static boolean isCheckmate(Piece[][] board, King king) {
        // If not in check, not checkmate
        if (!isInCheck(board, king)) {
            return false;
        }

        // 1. Check if king can escape
        if (canKingEscape(board, king)) {
            return false;
        }

        // 2. Find all attacking pieces and their paths
        List<Piece> attackers = new ArrayList<>();
        List<List<Position>> attackPaths = findAttackersAndPaths(board, king, attackers);

        // If more than one attacker, king must move (already checked it can't)
        if (attackers.size() > 1) {
            return true;
        }

        // 3. If one attacker, check if it can be captured or blocked
        if (attackers.size() == 1) {
            Piece attacker = attackers.get(0);

            // Check if the attacker can be captured
            if (canAttackerBeCaptured(board, king, attacker)) {
                return false;
            }

            // For non-knight attackers, check if attack can be blocked
            if (!(attacker instanceof Knight)) {
                List<Position> path = attackPaths.get(0);
                if (canAttackBeBlocked(board, king, path)) {
                    return false;
                }
            }
        }

        // If we've reached here, it's checkmate
        return true;
    }

    /**
     * Checks if the king can move to escape check.
     *
     * @param board The chess board representation
     * @param king The king to check
     * @return true if the king can move to escape check, false otherwise
     */
    private static boolean canKingEscape(Piece[][] board, King king) {
        int kingX = king.getPosition().getX();
        int kingY = king.getPosition().getY();

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                int newX = kingX + dx;
                int newY = kingY + dy;

                // Ensure position is on the board
                if (newX < 0 || newX >= 8 || newY < 0 || newY >= 8) {
                    continue;
                }

                // Ensure the square is either empty or has an opponent's piece
                if (board[newX][newY] == null || board[newX][newY].getColor() != king.getColor()) {
                    // Try moving king there temporarily
                    Piece savedPiece = board[newX][newY];
                    board[newX][newY] = king;
                    board[kingX][kingY] = null;

                    Position oldPos = king.getPosition();
                    king.setPosition(new Position((char)('a' + newY), newX + 1));

                    boolean stillInCheck = isInCheck(board, king);

                    // Restore board
                    king.setPosition(oldPos);
                    board[kingX][kingY] = king;
                    board[newX][newY] = savedPiece;

                    if (!stillInCheck) {
                        return true; // King can escape
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds all pieces attacking the king and their attack paths.
     *
     * @param board The chess board representation
     * @param king The king being attacked
     * @param attackers List to be filled with the attacking pieces
     * @return List of attack paths (positions between attacker and king) for each attacker
     */
    private static List<List<Position>> findAttackersAndPaths(Piece[][] board, King king, List<Piece> attackers) {
        List<List<Position>> attackPaths = new ArrayList<>();
        Position kingPos = king.getPosition();
        int kingX = kingPos.getX();
        int kingY = kingPos.getY();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() != king.getColor()) {
                    boolean isAttacking = false;

                    // For pawns, use special check
                    if (piece instanceof Pawn) {
                        if (isPawnCheckingKing(piece, kingPos)) {
                            isAttacking = true;
                        }
                    }
                    // For other pieces
                    else if (piece.canGo(kingPos) && isPathClear(board, piece, kingPos)) {
                        isAttacking = true;
                    }

                    if (isAttacking) {
                        attackers.add(piece);
                        attackPaths.add(calculateAttackPath(piece, kingX, kingY));
                    }
                }
            }
        }
        return attackPaths;
    }

    /**
     * Calculates the attack path from a piece to the king.
     *
     * @param attacker The attacking piece
     * @param kingX The x-coordinate of the king
     * @param kingY The y-coordinate of the king
     * @return List of positions that make up the attack path
     */
    private static List<Position> calculateAttackPath(Piece attacker, int kingX, int kingY) {
        ArrayList<Position> path = new ArrayList<>();

        // Knights don't have a path that can be blocked
        if (attacker instanceof Knight) {
            return path;
        }

        int startX = attacker.getPosition().getX();
        int startY = attacker.getPosition().getY();

        // Compute direction from attacker to king
        int dx = Integer.compare(kingX - startX, 0);
        int dy = Integer.compare(kingY - startY, 0);

        // Add all squares in the attack path
        for (int x = startX + dx, y = startY + dy; x != kingX || y != kingY; x += dx, y += dy) {
            path.add(new Position((char)('a' + y), x + 1));
        }

        return path;
    }

    /**
     * Checks if the attacker can be captured by any friendly piece.
     *
     * @param board The chess board representation
     * @param king The king being attacked
     * @param attacker The attacking piece
     * @return true if the attacker can be captured, false otherwise
     */
    private static boolean canAttackerBeCaptured(Piece[][] board, King king, Piece attacker) {
        Position attackerPos = attacker.getPosition();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece defender = board[i][j];
                if (defender != null &&
                        defender.getColor() == king.getColor() &&
                        !(defender instanceof King)) {

                    if (defender.canGo(attackerPos) && isPathClear(board, defender, attackerPos)) {
                        return true; // Attacker can be captured
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if the attack can be blocked by any friendly piece.
     *
     * @param board The chess board representation
     * @param king The king being attacked
     * @param path The attack path to check for blocking
     * @return true if the attack can be blocked, false otherwise
     */
    private static boolean canAttackBeBlocked(Piece[][] board, King king, List<Position> path) {
        for (Position blockPos : path) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    Piece blocker = board[i][j];
                    if (blocker != null &&
                            blocker.getColor() == king.getColor() &&
                            !(blocker instanceof King)) {

                        if (blocker.canGo(blockPos) && isPathClear(board, blocker, blockPos)) {
                            return true; // Attack can be blocked
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Creates a new piece of the specified type.
     *
     * @param pieceType The character representing the piece type (Q, R, B, N, K, P)
     * @param position The position for the new piece
     * @param color The color of the new piece
     * @return The created piece, or null if the type is invalid
     */
    public static Piece createPiece(char pieceType, Position position, Color color) {
        return switch (pieceType) {
            case 'Q' -> new Queen(position, color);
            case 'B' -> new Bishop(position, color);
            case 'R' -> new Rook(position, color);
            case 'N' -> new Knight(position, color);
            case 'K' -> new King(position, color);
            case 'P' -> new Pawn(position, color);
            default -> null;
        };
    }

    /**
     * Moves a piece on the board to a new position.
     *
     * @param board The chess board representation
     * @param piece The piece to move
     * @param newPosition The target position
     */
    public static void movePiece(Piece[][] board, Piece piece, Position newPosition) {
        int oldX = piece.getPosition().getX();
        int oldY = piece.getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        board[newX][newY] = piece;
        board[oldX][oldY] = null;
        piece.setPosition(newPosition);
    }

    /**
     * Provides detailed debugging information about a potential checkmate position.
     * Prints analysis of the king's status, possible escape moves, attackers, and potential defenses.
     *
     * @param board The chess board representation
     * @param king The king to analyze
     */
    public static void debugCheckmate(Piece[][] board, King king) {
        System.out.println("\n===== DETAILED CHECKMATE ANALYSIS =====");
        Position kingPos = king.getPosition();
        System.out.println("Analyzing king at position: " + kingPos);

        boolean inCheck = isInCheck(board, king);
        System.out.println("King in check: " + inCheck);

        if (!inCheck) {
            System.out.println("Not checkmate - king is not in check");
            return;
        }

        // Check king's escape squares
        boolean canEscape = canKingEscape(board, king);
        if (canEscape) {
            System.out.println("Not checkmate - king can move to escape check");
            return;
        }

        // Find attackers
        List<Piece> attackers = new ArrayList<>();
        List<List<Position>> paths = findAttackersAndPaths(board, king, attackers);

        System.out.println("Found " + attackers.size() + " attacking pieces");
        for (Piece attacker : attackers) {
            System.out.println("Attacker: " + attacker.getClass().getSimpleName() +
                    " at " + attacker.getPosition());
        }

        if (attackers.size() > 1) {
            System.out.println("CHECKMATE: Multiple attackers and king cannot move");
            return;
        }

        if (!attackers.isEmpty()) {
            Piece attacker = attackers.get(0);
            boolean canCapture = canAttackerBeCaptured(board, king, attacker);

            if (canCapture) {
                System.out.println("Not checkmate - attacker can be captured");
                return;
            }

            if (!(attacker instanceof Knight)) {
                boolean canBlock = canAttackBeBlocked(board, king, paths.get(0));

                if (canBlock) {
                    System.out.println("Not checkmate - attack can be blocked");
                    return;
                }
            }
        }

        System.out.println("CHECKMATE CONFIRMED: King cannot move, attacker cannot be captured or blocked");
    }
}