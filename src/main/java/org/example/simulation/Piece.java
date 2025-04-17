package org.example;

import org.example.pieces.Knight;
import org.example.pieces.Pawn;
import org.example.pieces.attributes.Color;
import org.example.pieces.attributes.Position;

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
     * Checks if there are other pieces of the same type and color that could move to the same file,
     * requiring file disambiguation in algebraic notation.
     *
     * @param board The current chess board
     * @return true if file disambiguation is needed
     */
    public abstract boolean isFileAmigue(Piece[][] board);

    /**
     * Checks if there are other pieces of the same type and color that could move to the same rank,
     * requiring rank disambiguation in algebraic notation.
     *
     * @param board The current chess board
     * @return true if rank disambiguation is needed
     */
    public abstract boolean isRankAmigue(Piece[][] board);

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
                if (board[i][j] instanceof org.example.pieces.King &&
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
        if (!(this instanceof org.example.pieces.Knight)) {
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
        // For knight moves with file disambiguation (like "Nbd7"), we don't need rank disambiguation
        if (this instanceof Knight && isCharAmb(board, newPosition)) {
            return false;
        }

        // For regular pawn moves, never needs rank disambiguation
        if (this instanceof Pawn && getPosition().getY() == newPosition.getY()) {
            return false;
        }

        // Count pieces of the same type and color that could move to target position
        int ambiguousPieces = 0;
        boolean differentRank = false;
        boolean sameFile = false;

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

                        // Check if the pieces are on different ranks
                        if (piece.getPosition().getX() != this.getPosition().getX()) {
                            differentRank = true;
                        }

                        // Check if they're on the same file
                        if (piece.getPosition().getY() == this.getPosition().getY()) {
                            sameFile = true;
                        }
                    }
                }
            }
        }

        // Rank disambiguation is needed when we have ambiguous pieces on different ranks
        // AND they're not already disambiguated by file
        return ambiguousPieces > 0 && differentRank && !sameFile;
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