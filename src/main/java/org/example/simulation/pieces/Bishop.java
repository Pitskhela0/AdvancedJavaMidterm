package org.example.simulation.pieces;

import org.example.simulation.Piece;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

public class Bishop extends Piece {
    public Bishop(Color color) {
        super(color);
    }

    public Bishop(Position position, Color color) {
        super(position, color);
    }

    private boolean canMoveToDiagonals(Position from, Position to) {
        int xDiff = Math.abs(from.getX() - to.getX());
        int yDiff = Math.abs(from.getY() - to.getY());
        return xDiff == yDiff; // Bishops move diagonally (equal changes in x and y)
    }

    @Override
    public boolean canGo(Position newPosition) {
        // Check if movement is diagonal
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        int xDiff = Math.abs(currentX - newX);
        int yDiff = Math.abs(currentY - newY);
        return xDiff == yDiff; // Bishops move diagonally
    }

    @Override
    public boolean checks(Piece[][] board) {
        // Determine if this bishop is checking the opponent's king
        Position currentPos = getPosition();
        Position opponentKingPos = null;

        // Find opponent's king position
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] instanceof King &&
                        board[i][j].getColor() != this.getColor()) {
                    opponentKingPos = board[i][j].getPosition();
                    break;
                }
            }
        }

        if (opponentKingPos == null) {
            return false;
        }

        // Check if can move to the king's position (diagonal)
        int currentX = currentPos.getX();
        int currentY = currentPos.getY();
        int kingX = opponentKingPos.getX();
        int kingY = opponentKingPos.getY();

        int xDiff = Math.abs(currentX - kingX);
        int yDiff = Math.abs(currentY - kingY);

        if (xDiff != yDiff) {
            return false; // Not on a diagonal
        }

        // Check if path to king is clear
        int xStep = (kingX - currentX) / xDiff;
        int yStep = (kingY - currentY) / yDiff;

        // Check each square between bishop and king
        for (int i = 1; i < xDiff; i++) {
            int x = currentX + (i * xStep);
            int y = currentY + (i * yStep);
            if (board[x][y] != null) {
                return false; // Path is blocked
            }
        }

        return true; // Path is clear, king is in check
    }
}