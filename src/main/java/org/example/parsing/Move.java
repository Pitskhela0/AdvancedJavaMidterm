package org.example.parsing;

import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;

public class Move {
    private char piece;
    private Color color;
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
    private char promoted;
    private boolean isCharAmb;
    private boolean isDigitAmb;

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
        this.color = color;
        this.isCharAmb = false; // Explicitly initialize to false
        this.isDigitAmb = false; // Explicitly initialize to false

        // checkmate
        if(action.contains("#"))
            checkmate = true;
            // check
        else if(action.contains("+"))
            check = true;

        if(action.equals("O-O")){
            kingSideCastling = true;
            return;
        }
        if(action.equals("O-O-O")){
            queenSideCastling = true;
            return;
        }

        char firstChar = action.charAt(0);
        // piece is pawn
        if(Character.isLowerCase(firstChar)){
            piece = 'P';

            // In the Move constructor where pawn captures are handled:
            // capturing
            if(action.contains("x")) {
                capture = true;
                char originFile = action.charAt(0);
                char targetFile = action.charAt(action.indexOf('x')+1);

                // For pawn captures, the file character before 'x' is always the source file
                // This is not ambiguity in the traditional sense, but the way captures are notated
                file = originFile;  // Store the origin file

                int y = Character.getNumericValue(action.charAt(action.indexOf('x')+2));
                newPosition = new Position(targetFile, y);

                // For pawn captures, file disambiguation is a special case
                // It's always needed as part of the notation, but it's not due to multiple pieces
                // being able to make the same move
                isCharAmb = true;
            }
            // promotion
            else if(action.contains("=")) {
                promotion = true;
                promoted = action.charAt(action.indexOf("=")+1);

                // Handle the position
                char x = action.charAt(0);
                int y = Character.getNumericValue(action.charAt(1));
                newPosition = new Position(x, y);

                // For simple pawn moves, no file disambiguation is needed
                isCharAmb = false;
            }
            // ordinary move
            else if(Character.isDigit(action.charAt(1))){
                char x = action.charAt(0);
                int y = Character.getNumericValue(action.charAt(1));
                newPosition = new Position(x, y);

                // For regular pawn moves, there's no file ambiguity
                // The file character is part of the destination, not disambiguation
                isCharAmb = false;
            }
            return;
        }

        this.piece = action.charAt(0);

        /**
         * Update the Move constructor to better handle knight moves with disambiguation
         */
// Inside the Move class, update the section that handles knights and other pieces:

        // Non-pawn, non-king pieces (like knights)
        if (Character.isUpperCase(firstChar) && firstChar != 'K') {
            this.piece = firstChar;

            if (action.contains("x")) {
                // Handle captures with disambiguation
                // (existing code)
            } else {
                int yPosition;
                int xPosition;

                // Check for file disambiguation (like Nhf6)
                if (action.length() >= 4 && Character.isLetter(action.charAt(1)) &&
                        Character.isLetter(action.charAt(2)) && Character.isDigit(action.charAt(3))) {

                    // This is a file disambiguation (e.g., "Nhf6")
                    isCharAmb = true;
                    isDigitAmb = false;
                    file = action.charAt(1);
                    yPosition = 2;
                    xPosition = 3;
                }
                // Check for rank disambiguation (like N6d7)
                else if (action.length() >= 4 && Character.isDigit(action.charAt(1)) &&
                        Character.isLetter(action.charAt(2)) && Character.isDigit(action.charAt(3))) {

                    // This is a rank disambiguation (e.g., "N6d7")
                    isDigitAmb = true;
                    isCharAmb = false;
                    rank = Character.getNumericValue(action.charAt(1));
                    yPosition = 2;
                    xPosition = 3;
                }
                // Check for both file and rank disambiguation (like Nf6d7)
                else if (action.length() >= 5 && Character.isLetter(action.charAt(1)) &&
                        Character.isDigit(action.charAt(2)) && Character.isLetter(action.charAt(3)) &&
                        Character.isDigit(action.charAt(4))) {

                    // This has both file and rank disambiguation
                    isCharAmb = true;
                    isDigitAmb = true;
                    file = action.charAt(1);
                    rank = Character.getNumericValue(action.charAt(2));
                    yPosition = 3;
                    xPosition = 4;
                }
                // No disambiguation (like Nd7)
                else {
                    isCharAmb = false;
                    isDigitAmb = false;
                    yPosition = 1;
                    xPosition = 2;
                }

                char x = action.charAt(yPosition);
                int y = Character.getNumericValue(action.charAt(xPosition));
                this.newPosition = new Position(x, y);
            }
        }

        if(action.contains("x")){
            capture = true;
            int yPosition;
            int xPosition;

            // capture with no ambiguity
            if(action.indexOf("x") == 1){
                yPosition = 2;
                xPosition = 3;
            }
            else if(action.indexOf("x") == 2){
                // capture with digit ambiguity
                if(Character.isDigit(action.charAt(1))){
                    isDigitAmb = true;
                    rank = Character.getNumericValue(action.charAt(1));
                }
                // capture with char ambiguity
                else {
                    isCharAmb = true;
                    file = action.charAt(1);
                }
                yPosition = 3;
                xPosition = 4;
            }
            // capture with char and digit ambiguity
            else{
                isCharAmb = true;
                isDigitAmb = true;
                file = action.charAt(1);
                rank = Character.getNumericValue(action.charAt(2));
                yPosition = 4;
                xPosition = 5;
            }
            char x = action.charAt(yPosition);
            int y = Character.getNumericValue(action.charAt(xPosition));
            newPosition = new Position(x,y);
        }
        else {
            int yPosition;
            int xPosition;
            // no ambiguity
            if((action.length() == 3 || action.length() == 4) && Character.isAlphabetic(action.charAt(1)) && Character.isDigit(action.charAt(2))){
                yPosition = 1;
                xPosition = 2;
            }
            else if(Character.isAlphabetic(action.charAt(2)) && Character.isDigit(action.charAt(3))){
                // ambiguity with only char
                if(Character.isAlphabetic(action.charAt(1))){
                    isCharAmb = true;
                    this.file = action.charAt(1);
                }
                // ambiguity with only digit
                else {
                    isDigitAmb = true;
                    this.rank = Character.getNumericValue(action.charAt(1));
                }
                yPosition = 2;
                xPosition = 3;
            }
            // ambiguity with char and digit
            else {
                yPosition = 3;
                xPosition = 4;
                isCharAmb = true;
                isDigitAmb = true;
                file = action.charAt(1);
                rank = Character.getNumericValue(action.charAt(2));
            }
            char x = action.charAt(yPosition);
            int y = Character.getNumericValue(action.charAt(xPosition));
            this.newPosition = new Position(x,y);
        }
    }

    public boolean isCharAmb(){
        return isCharAmb;
    }

    public boolean isDigitAmb(){
        return isDigitAmb;
    }

    public Color getColor() { return color; }

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

    public char getPiece() {
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

    public char getPromoted() {
        return promoted;
    }

    public String getAnnotation() {
        return annotation;
    }

    @Override
    public String toString() {
        return "Move{" +
                "piece=" + piece +
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
                ", promoted="+promoted+
                ", isCharAmb="+isCharAmb+
                ", isDigitAmb="+isDigitAmb+
                '}';
    }
}