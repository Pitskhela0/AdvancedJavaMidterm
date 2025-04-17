package org.example.simulation.pieces;

import org.example.simulation.Piece;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

public class Queen extends Piece {
    public Queen(Color color) {
        super(color);
    }

    public Queen(Position position, Color color) {
        super(position, color);
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

        // Queen can move like a rook (horizontally or vertically)
        if (xDiff == 0 || yDiff == 0) {
            return true;
        }

        // Queen can move like a bishop (diagonally)
        if (xDiff == yDiff) {
            return true;
        }

        return false;
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

        // Check if queen can reach the king's position
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int kingX = kingPosition.getX();
        int kingY = kingPosition.getY();

        int xDiff = Math.abs(kingX - currentX);
        int yDiff = Math.abs(kingY - currentY);

        // Queen can move horizontally, vertically or diagonally
        boolean canMove = (xDiff == 0 || yDiff == 0 || xDiff == yDiff);

        if (!canMove) {
            return false;
        }

        // Check if path to king is clear
        int xStep = (kingX == currentX) ? 0 : (kingX - currentX) / xDiff;
        int yStep = (kingY == currentY) ? 0 : (kingY - currentY) / yDiff;

        int steps = Math.max(xDiff, yDiff);

        for (int i = 1; i < steps; i++) {
            int x = currentX + (i * xStep);
            int y = currentY + (i * yStep);
            if (board[x][y] != null) {
                return false; // Path is blocked
            }
        }

        return true; // Path is clear, king is in check
    }
}