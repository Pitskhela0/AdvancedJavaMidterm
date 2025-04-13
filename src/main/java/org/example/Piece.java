package org.example;

import org.example.piece_attributes.Color;
import org.example.piece_attributes.Position;

public abstract class Piece {
    private Position position;
    private final Color color;

    public Piece(Color color, Position position){
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

    public abstract boolean isValidMove(Position position, Game game);
}
