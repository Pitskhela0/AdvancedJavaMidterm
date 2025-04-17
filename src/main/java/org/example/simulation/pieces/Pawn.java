package org.example.simulation.pieces;

import org.example.simulation.Piece;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

import static org.example.simulation.pieces.attributes.Color.black;
import static org.example.simulation.pieces.attributes.Color.white;

public class Pawn extends Piece {
    public Pawn(Color color) {
        super(color);
    }

    public Pawn(Position position, Color color) {
        super(position, color);
    }


    @Override
    public boolean canGo(Position newPosition) {
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int newX = newPosition.getX();
        int newY = newPosition.getY();

        // Direction depends on color
        int direction = (getColor() == white) ? 1 : -1;

        // Calculate differences
        int xDiff = newX - currentX;
        int yDiff = Math.abs(newY - currentY);

        // Normal forward movement (1 square)
        if (xDiff == direction && yDiff == 0) {
            return true;
        }

        // Initial double move (from starting position)
        boolean isInitialPosition = (getColor() == white && currentX == 1) ||
                (getColor() == black && currentX == 6);
        if (isInitialPosition && xDiff == 2 * direction && yDiff == 0) {
            return true;
        }

        // Diagonal capture
        if (xDiff == direction && yDiff == 1) {
            // We'll check in GameSimulator if there's actually a piece to capture
            return true;
        }

        return false;
    }

    @Override
    public boolean needsFileDisambiguation(Piece[][] board, Position newPosition) {
        // For normal pawn moves (not captures), there's no file disambiguation needed
        // as only one pawn can be on each file

        int currentY = getPosition().getY();
        int newY = newPosition.getY();

        // If it's a diagonal move (capture), check if there are multiple pawns that could capture
        if (currentY != newY) {
            int count = 0;
            int direction = (getColor() == white) ? 1 : -1;
            int targetX = newPosition.getX();

            // Check if there are pawns on adjacent files that could make this capture
            for (int fileOffset = -1; fileOffset <= 1; fileOffset += 2) {
                int adjacentFile = newY + fileOffset;

                // Skip if outside board
                if (adjacentFile < 0 || adjacentFile > 7) continue;

                // Check for pawn in the right position
                int pawnRow = targetX - direction;
                if (pawnRow < 0 || pawnRow > 7) continue;

                if (board[pawnRow][adjacentFile] instanceof Pawn &&
                        board[pawnRow][adjacentFile].getColor() == this.getColor()) {
                    count++;
                }
            }

            return count > 1;
        }

        // For straight moves, there's only one pawn that can make the move
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

        // Check if pawn can capture the king (diagonal move)
        int currentX = getPosition().getX();
        int currentY = getPosition().getY();
        int kingX = kingPosition.getX();
        int kingY = kingPosition.getY();

        int direction = (getColor() == white) ? 1 : -1;

        // Pawn can only check diagonally (capture move)
        return kingX == currentX + direction && Math.abs(kingY - currentY) == 1;
    }
}