import org.example.simulation.ChessUtils;
import org.example.simulation.Piece;
import org.example.simulation.pieces.*;
import org.example.simulation.pieces.attributes.Color;
import org.example.simulation.pieces.attributes.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ChessUtils class.
 * Tests path clearing, check detection, checkmate detection, and piece creation.
 */
public class ChessUtilsTest {

    private Piece[][] board;
    private King whiteKing;
    private King blackKing;

    @BeforeEach
    public void setUp() {
        // Initialize an empty board
        board = new Piece[8][8];

        // Create kings
        whiteKing = new King(new Position('e', 1), Color.white);
        blackKing = new King(new Position('e', 8), Color.black);

        // Place kings on the board
        board[0][4] = whiteKing;
        board[7][4] = blackKing;
    }

    @Test
    public void testIsPathClear_EmptyPath() {
        // Test a clear path for rook
        Rook rook = new Rook(new Position('a', 1), Color.white);
        board[0][0] = rook;
        Position targetPos = new Position('a', 5);

        assertTrue(ChessUtils.isPathClear(board, rook, targetPos),
                "Path should be clear for rook moving along file");
    }

    @Test
    public void testIsPathClear_BlockedPath() {
        // Test a blocked path for rook
        Rook rook = new Rook(new Position('a', 1), Color.white);
        board[0][0] = rook;
        Pawn pawn = new Pawn(new Position('a', 3), Color.white);
        board[2][0] = pawn;
        Position targetPos = new Position('a', 5);

        assertFalse(ChessUtils.isPathClear(board, rook, targetPos),
                "Path should be blocked for rook by pawn");
    }

    @Test
    public void testIsPathClear_DiagonalPath() {
        // Test a clear diagonal path for bishop
        Bishop bishop = new Bishop(new Position('c', 1), Color.white);
        board[0][2] = bishop;
        Position targetPos = new Position('f', 4);

        assertTrue(ChessUtils.isPathClear(board, bishop, targetPos),
                "Diagonal path should be clear for bishop");
    }

    @Test
    public void testIsPathClear_KnightJump() {
        // Test knight's path (which should always be clear due to jumping)
        Knight knight = new Knight(new Position('b', 1), Color.white);
        board[0][1] = knight;

        // Place pieces that would block other pieces but not knights
        board[1][0] = new Pawn(new Position('a', 2), Color.white);
        board[1][1] = new Pawn(new Position('b', 2), Color.white);
        board[1][2] = new Pawn(new Position('c', 2), Color.white);

        Position targetPos = new Position('c', 3);

        assertTrue(ChessUtils.isPathClear(board, knight, targetPos),
                "Knights should always have a clear path (they jump)");
    }

    @Test
    public void testIsInCheck_DirectAttack() {
        // Test direct attack by rook
        Rook rook = new Rook(new Position('e', 7), Color.black);
        board[6][4] = rook;

        assertTrue(ChessUtils.isInCheck(board, whiteKing),
                "White king should be in check from black rook");
    }

    @Test
    public void testIsInCheck_BlockedAttack() {
        // Test attack blocked by another piece
        Rook rook = new Rook(new Position('e', 7), Color.black);
        board[6][4] = rook;
        Pawn pawn = new Pawn(new Position('e', 2), Color.white);
        board[1][4] = pawn;

        assertFalse(ChessUtils.isInCheck(board, whiteKing),
                "White king should not be in check if attack is blocked");
    }

    @Test
    public void testIsInCheck_KnightAttack() {
        // Test knight's attack which can't be blocked
        Knight knight = new Knight(new Position('d', 3), Color.black);
        board[2][3] = knight;

        assertTrue(ChessUtils.isInCheck(board, whiteKing),
                "White king should be in check from knight");
    }

    @Test
    public void testIsInCheck_PawnAttack() {
        // Test pawn's attack
        Pawn pawn = new Pawn(new Position('d', 2), Color.black);
        board[1][3] = pawn;

        assertTrue(ChessUtils.isInCheck(board, whiteKing),
                "White king should be in check from pawn");
    }

    @Test
    public void testIsInCheck_DiagonalAttack() {
        // Test diagonal attack from bishop
        Bishop bishop = new Bishop(new Position('a', 5), Color.black);
        board[4][0] = bishop;

        assertTrue(ChessUtils.isInCheck(board, whiteKing),
                "White king should be in check from bishop");
    }

    @Test
    public void testIsCheckmate_SimpleCheckmate() {
        // Set up a simple checkmate with two rooks
        Rook rook1 = new Rook(new Position('a', 1), Color.black);
        Rook rook2 = new Rook(new Position('b', 2), Color.black);
        board[0][0] = rook1;
        board[1][1] = rook2;

        // Move white king to corner for easier checkmate
        board[0][4] = null;
        whiteKing.setPosition(new Position('h', 1));
        board[0][7] = whiteKing;

        assertTrue(ChessUtils.isCheckmate(board, whiteKing),
                "White king should be in checkmate");
    }

    @Test
    public void testIsCheckmate_CheckButCanEscape() {
        // Set up a check but with escape route
        Rook rook = new Rook(new Position('e', 2), Color.black);
        board[1][4] = rook;

        assertFalse(ChessUtils.isCheckmate(board, whiteKing),
                "White king should be in check but not checkmate (can move to d1 or f1)");
    }

    @Test
    public void testIsCheckmate_CheckButCanCapture() {
        // Set up a check where the attacking piece can be captured
        Queen queen = new Queen(new Position('e', 2), Color.black);
        board[1][4] = queen;

        // Add a piece that can capture the queen
        Bishop bishop = new Bishop(new Position('g', 4), Color.white);
        board[3][6] = bishop;

        assertFalse(ChessUtils.isCheckmate(board, whiteKing),
                "White king should not be in checkmate as bishop can capture queen");
    }

    @Test
    public void testIsCheckmate_CheckButCanBlock() {
        // Set up a check where the attack can be blocked
        Rook rook = new Rook(new Position('e', 8), Color.black);
        board[7][4] = null; // Remove black king for this test
        board[7][4] = rook;

        // Add a piece that can block
        Knight knight = new Knight(new Position('d', 3), Color.white);
        board[2][3] = knight;

        assertFalse(ChessUtils.isCheckmate(board, whiteKing),
                "White king should not be in checkmate as knight can block rook's attack");
    }

    @Test
    public void testIsCheckmate_DoubleCheck() {
        // Set up a double check (which can't be blocked or captured)
        Rook rook = new Rook(new Position('e', 8), Color.black);
        Bishop bishop = new Bishop(new Position('a', 5), Color.black);
        board[7][4] = null; // Remove black king for this test
        board[7][4] = rook;
        board[4][0] = bishop;

        // Add a piece that could block one attack but not both
        Knight knight = new Knight(new Position('d', 3), Color.white);
        board[2][3] = knight;

        assertTrue(ChessUtils.isCheckmate(board, whiteKing),
                "White king should be in checkmate with double check");
    }

    @Test
    public void testCreatePiece_AllTypes() {
        // Test creating all piece types
        Position pos = new Position('d', 4);

        Piece queen = ChessUtils.createPiece('Q', pos, Color.white);
        assertInstanceOf(Queen.class, queen);
        assertEquals(Color.white, queen.getColor());
        assertEquals(pos, queen.getPosition());

        Piece rook = ChessUtils.createPiece('R', pos, Color.black);
        assertInstanceOf(Rook.class, rook);
        assertEquals(Color.black, rook.getColor());

        Piece bishop = ChessUtils.createPiece('B', pos, Color.white);
        assertInstanceOf(Bishop.class, bishop);

        Piece knight = ChessUtils.createPiece('N', pos, Color.black);
        assertInstanceOf(Knight.class, knight);

        Piece king = ChessUtils.createPiece('K', pos, Color.white);
        assertInstanceOf(King.class, king);

        Piece pawn = ChessUtils.createPiece('P', pos, Color.black);
        assertInstanceOf(Pawn.class, pawn);
    }

    @Test
    public void testCreatePiece_InvalidType() {
        // Test creating an invalid piece type
        Piece invalid = ChessUtils.createPiece('X', new Position('a', 1), Color.white);
        assertNull(invalid, "Invalid piece type should return null");
    }

    @Test
    public void testMovePiece() {
        // Test moving a piece on the board
        Rook rook = new Rook(new Position('a', 1), Color.white);
        board[0][0] = rook;
        Position newPos = new Position('a', 5);

        ChessUtils.movePiece(board, rook, newPos);

        assertNull(board[0][0], "Original position should be empty");
        assertSame(rook, board[4][0], "Piece should be at new position");
        assertEquals(newPos, rook.getPosition(), "Piece's internal position should be updated");
    }
}