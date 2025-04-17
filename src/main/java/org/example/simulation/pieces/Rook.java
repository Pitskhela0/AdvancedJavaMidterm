package org.example.pieces;

import org.example.Piece;
import org.example.pieces.attributes.Color;
import org.example.pieces.attributes.Position;

public class Rook extends Piece {
    public Rook(Color color) {
        super(color);
    }

    public Rook(Position position, Color color) {
        super(position, color);
    }

    @Override
    public boolean isFileAmigue(Piece[][] board) {
        // Check if there's another rook of same color on the same file
        int count = 0;
        for (int i = 0; i < 8; i++) {
            if (board[i][getPosition().getY()] instanceof Rook &&
                    board[i][getPosition().getY()].getColor() == this.getColor() &&
                    !board[i][getPosition().getY()].equals(this)) {
                count++;
            }
        }
        return count > 0;
    }

    @Override
    public boolean isRankAmigue(Piece[][] board) {
        // Check if there's another rook of same color on the same rank
        int count = 0;
        for (int j = 0; j < 8; j++) {
            if (board[getPosition().getX()][j] instanceof Rook &&
                    board[getPosition().getX()][j].getColor() == this.getColor() &&
                    !board[getPosition().getX()][j].equals(this)) {
                count++;
            }
        }
        System.out.println(count);
        return count > 0;
    }

    @Override
    public boolean canGo(Position newPosition) {
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        // Rook can only move horizontally or vertically
        return (currentX == newX) || (currentY == newY);
    }

    @Override
    public boolean checks(Piece[][] board) {
        // Find opponent king's position
        Position kingPosition = null;

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] instanceof King &&
                        board[i][j].getColor() != this.getColor()) {
                    kingPosition = board[i][j].getPosition();
                    break;
                }
            }
        }

        if (kingPosition == null) {
            return false;
        }

        // Check if rook can reach the king's position
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int kingX = kingPosition.getX();
        int kingY = kingPosition.getY();

        // Rook can only move horizontally or vertically
        if (currentX != kingX && currentY != kingY) {
            return false;
        }

        // Check if path to king is clear
        if (currentX == kingX) {
            // Moving horizontally
            int startY = Math.min(currentY, kingY);
            int endY = Math.max(currentY, kingY);

            for (int y = startY + 1; y < endY; y++) {
                if (board[currentX][y] != null) {
                    return false; // Path is blocked
                }
            }
        } else {
            // Moving vertically
            int startX = Math.min(currentX, kingX);
            int endX = Math.max(currentX, kingX);

            for (int x = startX + 1; x < endX; x++) {
                if (board[x][currentY] != null) {
                    return false; // Path is blocked
                }
            }
        }

        return true; // Path is clear, king is in check
    }
}