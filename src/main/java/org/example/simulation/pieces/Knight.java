package org.example.simulation.pieces;

import org.example.simulation.Piece;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color);
    }

    public Knight(Position position, Color color) {
        super(position, color);
    }


    @Override
    public boolean canGo(Position newPosition) {
        // Knight moves in an L-shape: 2 squares in one direction, then 1 square perpendicular
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        int xDiff = Math.abs(currentX - newX);
        int yDiff = Math.abs(currentY - newY);

        return (xDiff == 1 && yDiff == 2) || (xDiff == 2 && yDiff == 1);
    }

    @Override
    public boolean checks(Piece[][] board) {
        // Determine if this knight is checking the opponent's king
        Position kingPosition = null;

        // Find opponent king's position
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

        // Check if knight can reach the king's position in one move
        return canGo(kingPosition);
    }
}