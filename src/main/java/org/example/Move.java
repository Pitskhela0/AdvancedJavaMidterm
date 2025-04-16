package org.example;

import org.example.piece_attributes.Color;
import org.example.piece_attributes.Position;
import org.example.pieces.*;

import java.util.HashSet;
import java.util.Set;

public class Move {
    private Piece piece;
    private final String action;
    private Position newPosition;
    private char file;
    private int rank;
    private final String comment;
    private boolean capture;
    private boolean check;
    private boolean checkmate;
    private final String annotation;
    private boolean promotion;
    private boolean kingSideCastling;
    private boolean queenSideCastling;

    public boolean isKingSideCastling() {
        return kingSideCastling;
    }

    public boolean isQueenSideCastling() {
        return queenSideCastling;
    }

    public Move(String action, String comment, String annotation, Color color){
        this.action = action;
        this.comment = comment;
        this.annotation = annotation;
        // checkmate
        if(action.contains("#"))
            this.checkmate = true;
        // check
        else if(action.contains("+"))
            this.check = true;

        if(action.equals("O-O")){
            kingSideCastling = true;
            return;
        }
        if(action.equals("O-O-O")){
            queenSideCastling = true;
            return;
        }

        System.out.println(action);


        char firstChar = action.charAt(0);
        // piece is pawn
        if(Character.isLowerCase(firstChar)){
            this.piece = new Pawn(color);
            // capturing
            if(action.contains("x")) {
                System.out.println("pawn capture");
                this.capture = true;
                this.file = action.charAt(0);
                char y = action.charAt(action.indexOf('x')+1);
                int x = Character.getNumericValue(action.charAt(action.indexOf('x')+2));
                this.newPosition = new Position(y,x);
            }
            // promotion
            if(action.contains("=")) {
                System.out.println("pawn promotion");
                this.promotion = true;
                this.piece = getPiece(action.charAt(action.indexOf("=")+1),color);
            }
            // ordinary move
            if(Character.isDigit(action.charAt(1))){
                System.out.println("pawn ordinary move");
                this.file = action.charAt(0);
                char y = action.charAt(0);
                int x = Character.getNumericValue(action.charAt(1));
                this.newPosition = new Position(y,x);
            }
            return;
        }

        this.piece = getPiece(firstChar,color);

        if(firstChar == 'K'){
            if(action.contains("x")){
                this.capture = true;
                char y = action.charAt(action.indexOf("x")+1);
                int x = Character.getNumericValue(action.charAt(action.indexOf("x")+2));
                this.newPosition = new Position(y,x);
            }
            else {
                char y = action.charAt(1);
                int x = Character.getNumericValue(action.charAt(2));
                this.newPosition = new Position(y,x);
            }
            return;
        }

        Set<Character> set = new HashSet<>(Set.of('N', 'R', 'Q', 'B'));

        if(set.contains(firstChar)){
            if(action.contains("x")){
                this.capture = true;
                int yPosition;
                int xPosition;

                // capture with no ambiguity
                if(action.indexOf("x") == 1){
                    System.out.println("capture with no ambiguity");
                    yPosition = 2;
                    xPosition = 3;
                }
                else if(action.indexOf("x") == 2){
                    // capture with digit ambiguity
                    if(Character.isDigit(action.charAt(1))){
                        System.out.println("capture with digit ambiguity");
                        this.rank = Character.getNumericValue(action.charAt(1));
                    }
                    // capture with char ambiguity
                    else {
                        System.out.println("capture with char ambiguity");
                        this.file = action.charAt(1);
                    }
                    yPosition = 3;
                    xPosition = 4;
                }
                // capture with char and digit ambiguity
                else{
                    System.out.println("capture with char and digit ambiguity");
                    this.file = action.charAt(1);
                    this.rank = Character.getNumericValue(action.charAt(2));
                    yPosition = 4;
                    xPosition = 5;
                }
                char y = action.charAt(yPosition);
                int x = Character.getNumericValue(action.charAt(xPosition));
                this.newPosition = new Position(y,x);
            }
            else {
                int yPosition;
                int xPosition;
                // no ambiguity
                if((action.length() == 3 || action.length() == 4) && Character.isAlphabetic(action.charAt(1)) && Character.isDigit(action.charAt(2))){
                    System.out.println(" no ambiguity");
                    yPosition = 1;
                    xPosition = 2;
                }
                else if(Character.isAlphabetic(action.charAt(2)) && Character.isDigit(action.charAt(3))){
                    // ambiguity with only char
                    if(Character.isAlphabetic(action.charAt(1))){
                        System.out.println("ambiguity with only char");
                        this.file = action.charAt(1);
                    }
                    // ambiguity with only digit
                    else {
                        System.out.println("ambiguity with only digit");
                        this.rank = Character.getNumericValue(action.charAt(1));
                    }
                    yPosition = 2;
                    xPosition = 3;
                }
                // ambiguity with char and digit
                else {
                    System.out.println("ambiguity with both");
                    yPosition = 3;
                    xPosition = 4;
                    this.file = action.charAt(1);
                    this.rank = Character.getNumericValue(action.charAt(2));
                }
                char y = action.charAt(yPosition);
                int x = Character.getNumericValue(action.charAt(xPosition));
                this.newPosition = new Position(y,x);
            }
        }
    }

    public char getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    public boolean isCapture() {
        return capture;
    }

    public boolean isCheck() {
        return check;
    }

    public boolean isCheckmate() {
        return checkmate;
    }

    public boolean isPromotion() {
        return promotion;
    }

    private Piece getPiece(char p, Color color){
        return switch (p) {
            case 'Q' -> new Queen(color);
            case 'B' -> new Bishop(color);
            case 'R' -> new Rook(color);
            case 'N' -> new Knight(color);
            case 'K' -> new King(color);
            default -> null;
        };
    }
    public Piece getPiece() {
        return piece;
    }

    public Position getNewPosition() {
        return newPosition;
    }

    public String getComment() {
        return comment;
    }

    public String getAction() {
        return action;
    }


    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        return "Move{" +
                "piece=" + (piece != null ? piece.getClass().getSimpleName() : "null") +
                ", action='" + action + '\'' +
                ", newPosition=" + (newPosition != null ? newPosition.toString() : "null") +
                ", file=" + file +
                ", rank=" + rank +
                ", comment='" + comment + '\'' +
                ", capture=" + capture +
                ", check=" + check +
                ", checkmate=" + checkmate +
                ", annotation='" + annotation + '\'' +
                ", promotion=" + promotion +
                '}';
    }

}
