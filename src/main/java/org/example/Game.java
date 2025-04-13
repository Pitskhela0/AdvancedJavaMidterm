package org.example;

import org.example.piece_attributes.Position;

import java.util.List;

public class Game {
    private Piece[][] board = new Piece[8][8];
    private Record record;

    public Game(List<Move> moves, Record record) {
        this.record = record;
    }

    public void runGame(){
        for(Move m : record.getMoves()){
            if(m.getPiece().isValidMove(m.getNewPosition(),this)){
                m.getPiece().setPosition(m.getNewPosition());
            }
            else{
                System.out.println("wrong move, game simulation is over");
            }
        }
    }
}
