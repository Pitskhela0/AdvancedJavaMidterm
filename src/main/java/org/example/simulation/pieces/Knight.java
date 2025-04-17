package org.example.pieces;

import org.example.Piece;
import org.example.pieces.attributes.Color;
import org.example.pieces.attributes.Position;

public class Knight extends Piece {
    public Knight(Color color) {
        super(color);
    }

    public Knight(Position position, Color color) {
        super(position, color);
    }

    @Override
    public boolean isFileAmigue(Piece[][] board) {
        // Check if there's another knight of the same color that could move to the same file
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] instanceof Knight &&
                        board[i][j].getColor() == this.getColor() &&
                        !board[i][j].equals(this)) {

                    // Check if this knight could move to a position on the same file
                    Knight other = (Knight) board[i][j];
                    for (int x = 0; x < 8; x++) {
                        Position potential = new Position((char)('a' + getPosition().getY()), x + 1);
                        if (other.canGo(potential) && this.canGo(potential)) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count > 0;
    }

    @Override
    public boolean isRankAmigue(Piece[][] board) {
        // Check if there's another knight of the same color that could move to the same rank
        int count = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (board[i][j] instanceof Knight &&
                        board[i][j].getColor() == this.getColor() &&
                        !board[i][j].equals(this)) {

                    // Check if this knight could move to a position on the same rank
                    Knight other = (Knight) board[i][j];
                    for (int y = 0; y < 8; y++) {
                        Position potential = new Position((char)('a' + y), getPosition().getX() + 1);
                        if (other.canGo(potential) && this.canGo(potential)) {
                            count++;
                            break;
                        }
                    }
                }
            }
        }
        return count > 0;
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
    /**
     * Update to the Knight class or Piece class to properly handle rank disambiguation for knights
     */
    @Override
    public boolean needsRankDisambiguation(Piece[][] board, Position newPosition) {
        // For knights, check if there's another knight on the same rank that could move to the target
        int currentRank = getPosition().getX();
        int ambiguousKnights = 0;

        // Check all knights of the same color
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                if (piece instanceof Knight &&
                        piece.getColor() == this.getColor() &&
                        !piece.equals(this) &&
                        piece.canGo(newPosition) &&
                        isPathClear(board, piece.getPosition(), newPosition)) {

                    // If there's a knight on the same rank that can move to the target
                    if (piece.getPosition().getX() == currentRank) {
                        return true;
                    }

                    ambiguousKnights++;
                }
            }
        }

        // If there are multiple knights that can move to the target and file disambiguation
        // wouldn't be enough, we need rank disambiguation
        return ambiguousKnights > 0 && !needsFileDisambiguation(board, newPosition);
    }

}