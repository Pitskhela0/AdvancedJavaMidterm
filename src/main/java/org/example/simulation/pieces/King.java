package org.example.pieces;

import org.example.Piece;
import org.example.pieces.attributes.Color;
import org.example.pieces.attributes.Position;

public class King extends Piece {
    private boolean hasMoved = false;

    public King(Color color) {
        super(color);
    }

    public King(Position position, Color color) {
        super(position, color);
    }

    @Override
    public boolean isFileAmigue(Piece[][] board) {
        // There can only be one king of each color, so no ambiguity
        return false;
    }

    @Override
    public boolean isRankAmigue(Piece[][] board) {
        // There can only be one king of each color, so no ambiguity
        return false;
    }

    @Override
    public boolean canGo(Position newPosition) {
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        // Calculate differences
        int xDiff = Math.abs(newX - currentX);
        int yDiff = Math.abs(newY - currentY);

        // King can move one square in any direction
        return xDiff <= 1 && yDiff <= 1 && (xDiff != 0 || yDiff != 0);
    }

    @Override
    public void setPosition(Position position) {
        super.setPosition(position);
        hasMoved = true;
    }

    public boolean canCastleKingSide(Piece[][] board) {
        // Check if king has not moved yet
        if (hasMoved) {
            return false;
        }

        int row = (getColor() == Color.white) ? 0 : 7;

        // Check if rook is in correct position and hasn't moved
        if (!(board[row][7] instanceof Rook) ||
                board[row][7].getColor() != getColor()) {
            return false;
        }

        // Check if squares between king and rook are empty
        for (int y = 5; y < 7; y++) {
            if (board[row][y] != null) {
                return false;
            }
        }

        // Check if king is not in check and would not be in check during castling
        // Note: This would require more complex logic to fully implement
        // We'll simplify for now

        return true;
    }

    public boolean canCastleQueenSide(Piece[][] board) {
        // Check if king has not moved yet
        if (hasMoved) {
            return false;
        }

        int row = (getColor() == Color.white) ? 0 : 7;

        // Check if rook is in correct position and hasn't moved
        if (!(board[row][0] instanceof Rook) ||
                board[row][0].getColor() != getColor()) {
            return false;
        }

        // Check if squares between king and rook are empty
        for (int y = 1; y < 4; y++) {
            if (board[row][y] != null) {
                return false;
            }
        }

        // Check if king is not in check and would not be in check during castling
        // Note: This would require more complex logic to fully implement
        // We'll simplify for now

        return true;
    }

    @Override
    public boolean checks(Piece[][] board) {
        // Kings don't check other kings directly in normal chess
        return false;
    }

    // Helper method to check if this king is in check
    public boolean isInCheck(Piece[][] board) {
        // Check if any opponent piece can attack the king
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece != null && piece.getColor() != this.getColor()) {
                    if (piece.canGo(this.getPosition()) && piece.checks(board)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}