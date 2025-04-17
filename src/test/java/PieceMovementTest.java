import org.example.simulation.Piece;
import org.example.simulation.pieces.*;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for each chess piece's movement rules.
 * Verifies that each piece can move according to chess rules and can't make illegal moves.
 */
public class PieceMovementTest {

    @Test
    public void testPawnMovement() {
        // Test white pawn forward movement
        Pawn whitePawn = new Pawn(new Position('e', 2), Color.white);

        // Valid moves
        assertTrue(whitePawn.canGo(new Position('e', 3)), "White pawn can move forward one square");
        assertTrue(whitePawn.canGo(new Position('e', 4)), "White pawn can move forward two squares from starting position");

        // Invalid moves
        assertFalse(whitePawn.canGo(new Position('e', 5)), "White pawn cannot move forward three squares");
        assertFalse(whitePawn.canGo(new Position('d', 3)), "White pawn cannot move diagonally without capture");
        assertFalse(whitePawn.canGo(new Position('f', 3)), "White pawn cannot move diagonally without capture");
        assertFalse(whitePawn.canGo(new Position('e', 1)), "White pawn cannot move backward");

        // Test white pawn diagonal captures
        assertTrue(whitePawn.canGo(new Position('d', 3)),
                "White pawn can move diagonally for capture (though canGo doesn't validate if capture exists)");
        assertTrue(whitePawn.canGo(new Position('f', 3)),
                "White pawn can move diagonally for capture (though canGo doesn't validate if capture exists)");

        // Test black pawn movement
        Pawn blackPawn = new Pawn(new Position('e', 7), Color.black);

        // Valid moves
        assertTrue(blackPawn.canGo(new Position('e', 6)), "Black pawn can move forward one square");
        assertTrue(blackPawn.canGo(new Position('e', 5)), "Black pawn can move forward two squares from starting position");

        // Invalid moves
        assertFalse(blackPawn.canGo(new Position('e', 4)), "Black pawn cannot move forward three squares");
        assertFalse(blackPawn.canGo(new Position('e', 8)), "Black pawn cannot move backward");

        // Test black pawn diagonal captures
        assertTrue(blackPawn.canGo(new Position('d', 6)),
                "Black pawn can move diagonally for capture");
        assertTrue(blackPawn.canGo(new Position('f', 6)),
                "Black pawn can move diagonally for capture");
    }

    @Test
    public void testPawnPromotionPosition() {
        // White pawn about to promote
        Pawn whitePawn = new Pawn(new Position('e', 7), Color.white);
        assertTrue(whitePawn.canGo(new Position('e', 8)), "White pawn can move to promotion rank");

        // Black pawn about to promote
        Pawn blackPawn = new Pawn(new Position('e', 2), Color.black);
        assertTrue(blackPawn.canGo(new Position('e', 1)), "Black pawn can move to promotion rank");
    }

    @Test
    public void testRookMovement() {
        Rook rook = new Rook(new Position('d', 4), Color.white);

        // Valid moves - horizontal
        assertTrue(rook.canGo(new Position('a', 4)), "Rook can move left");
        assertTrue(rook.canGo(new Position('h', 4)), "Rook can move right");

        // Valid moves - vertical
        assertTrue(rook.canGo(new Position('d', 1)), "Rook can move down");
        assertTrue(rook.canGo(new Position('d', 8)), "Rook can move up");

        // Invalid moves - diagonal
        assertFalse(rook.canGo(new Position('e', 5)), "Rook cannot move diagonally");
        assertFalse(rook.canGo(new Position('c', 3)), "Rook cannot move diagonally");

        // Invalid moves - L-shape
        assertFalse(rook.canGo(new Position('f', 5)), "Rook cannot move in L-shape");
    }

    @Test
    public void testKnightMovement() {
        Knight knight = new Knight(new Position('d', 4), Color.white);

        // Valid moves - all L-shapes
        assertTrue(knight.canGo(new Position('c', 6)), "Knight can move 1 left, 2 up");
        assertTrue(knight.canGo(new Position('e', 6)), "Knight can move 1 right, 2 up");
        assertTrue(knight.canGo(new Position('f', 5)), "Knight can move 2 right, 1 up");
        assertTrue(knight.canGo(new Position('f', 3)), "Knight can move 2 right, 1 down");
        assertTrue(knight.canGo(new Position('e', 2)), "Knight can move 1 right, 2 down");
        assertTrue(knight.canGo(new Position('c', 2)), "Knight can move 1 left, 2 down");
        assertTrue(knight.canGo(new Position('b', 3)), "Knight can move 2 left, 1 down");
        assertTrue(knight.canGo(new Position('b', 5)), "Knight can move 2 left, 1 up");

        // Invalid moves
        assertFalse(knight.canGo(new Position('d', 5)), "Knight cannot move straight");
        assertFalse(knight.canGo(new Position('e', 5)), "Knight cannot move diagonally");
        assertFalse(knight.canGo(new Position('a', 7)), "Knight cannot move 3 left, 3 up");
    }

    @Test
    public void testBishopMovement() {
        Bishop bishop = new Bishop(new Position('d', 4), Color.white);

        // Valid moves - diagonals
        assertTrue(bishop.canGo(new Position('a', 1)), "Bishop can move down-left");
        assertTrue(bishop.canGo(new Position('h', 8)), "Bishop can move up-right");
        assertTrue(bishop.canGo(new Position('a', 7)), "Bishop can move up-left");
        assertTrue(bishop.canGo(new Position('g', 1)), "Bishop can move down-right");

        // Invalid moves - horizontal and vertical
        assertFalse(bishop.canGo(new Position('d', 8)), "Bishop cannot move straight up");
        assertFalse(bishop.canGo(new Position('h', 4)), "Bishop cannot move straight right");

        // Invalid moves - non-diagonal
        assertFalse(bishop.canGo(new Position('f', 7)), "Bishop cannot move in non-diagonal path");
    }

    @Test
    public void testQueenMovement() {
        Queen queen = new Queen(new Position('d', 4), Color.white);

        // Valid moves - horizontal and vertical (like a rook)
        assertTrue(queen.canGo(new Position('a', 4)), "Queen can move left");
        assertTrue(queen.canGo(new Position('h', 4)), "Queen can move right");
        assertTrue(queen.canGo(new Position('d', 1)), "Queen can move down");
        assertTrue(queen.canGo(new Position('d', 8)), "Queen can move up");

        // Valid moves - diagonals (like a bishop)
        assertTrue(queen.canGo(new Position('a', 1)), "Queen can move down-left");
        assertTrue(queen.canGo(new Position('h', 8)), "Queen can move up-right");
        assertTrue(queen.canGo(new Position('a', 7)), "Queen can move up-left");
        assertTrue(queen.canGo(new Position('g', 1)), "Queen can move down-right");

        // Invalid moves
        assertFalse(queen.canGo(new Position('f', 7)), "Queen cannot move in L-shape");
        assertFalse(queen.canGo(new Position('c', 7)), "Queen cannot move in non-straight, non-diagonal path");
    }

    @Test
    public void testKingMovement() {
        King king = new King(new Position('d', 4), Color.white);

        // Valid moves - one square in any direction
        assertTrue(king.canGo(new Position('c', 4)), "King can move one square left");
        assertTrue(king.canGo(new Position('e', 4)), "King can move one square right");
        assertTrue(king.canGo(new Position('d', 3)), "King can move one square down");
        assertTrue(king.canGo(new Position('d', 5)), "King can move one square up");
        assertTrue(king.canGo(new Position('c', 3)), "King can move one square down-left");
        assertTrue(king.canGo(new Position('e', 5)), "King can move one square up-right");
        assertTrue(king.canGo(new Position('c', 5)), "King can move one square up-left");
        assertTrue(king.canGo(new Position('e', 3)), "King can move one square down-right");

        // Invalid moves - more than one square
        assertFalse(king.canGo(new Position('b', 4)), "King cannot move two squares left");
        assertFalse(king.canGo(new Position('d', 6)), "King cannot move two squares up");
        assertFalse(king.canGo(new Position('f', 6)), "King cannot move in L-shape");
    }
}