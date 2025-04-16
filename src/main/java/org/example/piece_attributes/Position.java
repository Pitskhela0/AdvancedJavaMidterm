package org.example.piece_attributes;

public class Position {
    private int x;
    private char y;

    public Position(char y, int x){
        this.y = y;
        this.x = x;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y -'a'+1;
    }

    @Override
    public String toString() {
        return ""+y+"  "+x;
    }
}
