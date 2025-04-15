package org.example;

import org.example.piece_attributes.Color;
import org.example.piece_attributes.Position;

public class Move {
    private Piece piece;
    private Position newPosition;
    private String file;
    private String comment;

    public Move(Piece piece, Position newPosition, String file,String comment){
        this.piece = piece;
        this.newPosition = newPosition;
        this.file = file;
        this.comment = comment;
    }
    public Piece getPiece() {
        return piece;
    }

    public Position getNewPosition() {
        return newPosition;
    }

    public String getFile() {
        return file;
    }

    public String getComment() {
        return comment;
    }
}
