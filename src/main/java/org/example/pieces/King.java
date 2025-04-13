package org.example.pieces;

import org.example.piece_attributes.Color;
import org.example.Game;
import org.example.Piece;
import org.example.piece_attributes.Position;

public class King extends Piece {
    public King(Color color, Position position) {
        super(color, position);
    }

    @Override
    public boolean isValidMove(Position position, Game game) {
        return false;
    }
}
