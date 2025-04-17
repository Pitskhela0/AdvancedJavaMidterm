package org.example.simulation;

import org.example.simulation.pieces.Knight;
import org.example.simulation.pieces.Pawn;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;
import org.example.simulation.pieces.King;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Piece {
    private Position position;
    private final Color color;

    public Piece(Color color) {
        this.color = color;
    }

    public Piece(Position position, Color color) {
        this.color = color;
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    /**
     * Checks if this piece is putting the opponent's king in check
     *
     * @param board The current chess board
     * @return true if the piece is checking the opponent's king
     */
    public boolean checks(Piece[][] board) {
        // Find opponent's king
        Position kingPosition = null;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] instanceof King &&
                        board[i][j].getColor() != this.getColor()) {
                    kingPosition = board[i][j].getPosition();
                    break;
                }
            }
            if (kingPosition != null) break;
        }

        if (kingPosition == null) {
            return false;
        }

        // Check if this piece can attack the king
        if (!canGo(kingPosition)) {
            return false;
        }

        // Check if path to king is clear (for non-Knight pieces)
        if (!(this instanceof Knight)) {
            int startX = position.getX();
            int startY = position.getY();
            int kingX = kingPosition.getX();
            int kingY = kingPosition.getY();

            // Determine direction of movement
            int deltaX = (kingX != startX) ? (kingX - startX) / Math.abs(kingX - startX) : 0;
            int deltaY = (kingY != startY) ? (kingY - startY) / Math.abs(kingY - startY) : 0;

            // Check each square between piece and king
            int x = startX + deltaX;
            int y = startY + deltaY;

            while (x != kingX || y != kingY) {
                if (board[x][y] != null) {
                    return false; // Path is blocked
                }

                x += deltaX;
                y += deltaY;
            }
        }

        return true; // King is in check
    }

    /**
     * Determines if this piece can move to the given position according to chess rules,
     * without considering other pieces on the board (blocked paths, etc.)
     *
     * @param newPosition The target position
     * @return true if the piece can move to that position
     */
    public abstract boolean canGo(Position newPosition);

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Piece other = (Piece) obj;
        return this.color == other.color && Objects.equals(this.position, other.position);
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, position);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "position=" + position +
                ", color=" + color +
                '}';
    }

    /**
     * Checks if file disambiguation is needed for this piece.
     * File disambiguation is needed when there are multiple pieces of the same type
     * and color that can move to the same target square, and they are on different files.
     *
     * @param board The current board state
     * @param newPosition The target position to move to
     * @return true if file disambiguation is needed
     */
    public boolean needsFileDisambiguation(Piece[][] board, Position newPosition) {
        // Special case for pawn captures
        if (this instanceof Pawn && this.getPosition().getY() != newPosition.getY()) {
            // For pawn captures, the file is always shown in notation (e.g., "exd5")
            // This is not due to ambiguity but rather the standard notation format
            return true;
        }

        // Special case for pawns moving straight ahead
        if (this instanceof Pawn && this.getPosition().getY() == newPosition.getY()) {
            return false;  // No file disambiguation needed for forward pawn moves
        }

        // For other pieces, check for multiple pieces of same type that could move to target
        int ambiguousPieces = 0;
        boolean differentFile = false;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null &&
                        piece.getClass().equals(this.getClass()) &&
                        piece.getColor() == this.getColor() &&
                        !piece.equals(this)) {

                    // Check if this other piece can also move to the target position
                    if (piece.canGo(newPosition) && isPathClear(board, piece.getPosition(), newPosition)) {
                        ambiguousPieces++;

                        // Check if the pieces are on different files
                        if (piece.getPosition().getY() != this.getPosition().getY()) {
                            differentFile = true;
                        }
                    }
                }
            }
        }

        return ambiguousPieces > 0 && differentFile;
    }

    /**
     * Checks if rank disambiguation is needed for this piece.
     * Rank disambiguation is needed when there are multiple pieces of the same type
     * and color that can move to the same target square, and they are on different ranks.
     *
     * @param board The current board state
     * @param newPosition The target position to move to
     * @return true if rank disambiguation is needed
     */
    public boolean needsRankDisambiguation(Piece[][] board, Position newPosition) {
        // Special case for pawns - they never need rank disambiguation
        if (this instanceof Pawn) {
            return false;
        }

        // Collect all pieces of the same type and color that can move to the target position
        List<Piece> candidates = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null &&
                        piece.getClass().equals(this.getClass()) &&
                        piece.getColor() == this.getColor() &&
                        !piece.equals(this) &&
                        piece.canGo(newPosition) &&
                        isPathClear(board, piece.getPosition(), newPosition)) {

                    candidates.add(piece);
                }
            }
        }

        // If there are no other pieces that can make this move, no disambiguation is needed
        if (candidates.isEmpty()) {
            return false;
        }

        // Check if any candidate piece is on a different rank
        boolean differentRank = false;
        for (Piece candidate : candidates) {
            if (candidate.getPosition().getRank() != this.getPosition().getRank()) {
                differentRank = true;
                break;
            }
        }

        // If no pieces on different ranks, no rank disambiguation needed
        if (!differentRank) {
            return false;
        }

        // Check if file disambiguation would be enough
        boolean sameFile = false;
        for (Piece candidate : candidates) {
            if (candidate.getPosition().getFile() == this.getPosition().getFile()) {
                sameFile = true;
                break;
            }
        }

        // If there's a piece on the same file, we need rank disambiguation too
        return sameFile;
    }
    /**
     * Helper method to check if file disambiguation is needed
     */
    private boolean isCharAmb(Piece[][] board, Position newPosition) {
        return needsFileDisambiguation(board, newPosition);
    }
    /**
     * Helper method to check if the path is clear for a piece to move
     */
    protected boolean isPathClear(Piece[][] board, Position from, Position to) {
        // Knights can jump, so no path checking needed
        if (this instanceof Knight) {
            return true;
        }

        int startX = from.getX();
        int startY = from.getY();
        int endX = to.getX();
        int endY = to.getY();

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
}