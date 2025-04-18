package org.example.simulation;

import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.*;

public class Display {
    /**
     * Prints the current state of the chess board to the console.
     * This method creates a visual representation of the board with coordinates.
     */
    public void printBoard(Piece[][] board) {
        System.out.println("\nCurrent board state:");
        System.out.println("    a   b   c   d   e   f   g   h");
        System.out.println(" +----+---+---+---+---+---+---+---+");

        for (int i = 7; i >= 0; i--) {
            System.out.print((i + 1) + " | ");
            for (int j = 0; j < 8; j++) {
                Piece piece = board[i][j];
                char symbol = getPieceSymbol(piece);
                System.out.print(symbol + " | ");
            }
            System.out.println(" " + (i + 1));
            System.out.println(" +----+---+---+---+---+---+---+---+");
        }
        System.out.println("    a   b   c   d   e   f   g   h\n");
    }

    /**
     * Returns a symbol representing the piece for board printing.
     *
     * @param piece The chess piece
     * @return A character representing the piece
     */
    private char getPieceSymbol(Piece piece) {
        if (piece == null) {
            return ' ';
        }

        char symbol = ' ';

        switch (piece) {
            case Pawn pawn -> symbol = 'P';
            case Rook rook -> symbol = 'R';
            case Knight knight -> symbol = 'N';
            case Bishop bishop -> symbol = 'B';
            case Queen queen -> symbol = 'Q';
            case King king -> symbol = 'K';
            default -> {
            }
        }

        return piece.getColor() == Color.white ? symbol : Character.toLowerCase(symbol);
    }
}
