import org.example.parsing.GameParser;
import org.example.parsing.Move;
import org.example.parsing.Record;
import org.example.simulation.pieces.attributes.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the GameParser class.
 * Verifies parsing of PGN files, extracting tags, moves, and detecting syntax errors.
 */
public class GameParserTest {

    private GameParser parser;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new GameParser();
    }

    @Test
    public void testParseValidPGN() throws IOException {
        // Create a temporary PGN file with valid content
        Path pgnFile = tempDir.resolve("valid.pgn");
        String content = """
                [Event "Test Game"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        assertNotNull(records, "Parser should return a list of records");
        assertFalse(records.isEmpty(), "List of records should not be empty");
        assertNotNull(records.getFirst(), "First record should not be null");

        Record record = records.getFirst();


        List<Move> moves = record.getMoves();
        assertNotNull(moves, "Moves should not be null");
        assertEquals(14, moves.size(), "Should have 14 moves (7 for each player)");

        // Check specific moves
        assertEquals("e4", moves.get(0).getAction(), "First move should be e4");
        assertEquals("e5", moves.get(1).getAction(), "Second move should be e5");
        assertEquals("Nf3", moves.get(2).getAction(), "Third move should be Nf3");
    }

    @Test
    public void testParseInvalidPGN() throws IOException {
        // Create a temporary PGN file with invalid content (missing result)
        Path pgnFile = tempDir.resolve("invalid.pgn");
        String content = """
                [Event "Test Game"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 4. Ba4 Nf6 5. O-O Be7 6. Re1 b5 7. Bb3 d6
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should return a list with a null record due to the missing result
        assertNotNull(records, "Parser should return a list");
        assertTrue(records.contains(null), "List should contain a null record for invalid game");
    }

    @Test
    public void testParseMissingRound() throws IOException {
        // Create a temporary PGN file with a missing round
        Path pgnFile = tempDir.resolve("missing_round.pgn");
        String content = """
                [Event "Test Game"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 3. Nf3 Nc6 4. Bb5 a6 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should detect the missing round (2)
        assertNotNull(records, "Parser should return a list");
        assertTrue(records.contains(null), "List should contain a null record due to missing round");
    }

    @Test
    public void testParseDuplicateRound() throws IOException {
        // Create a temporary PGN file with a duplicate round
        Path pgnFile = tempDir.resolve("duplicate_round.pgn");
        String content = """
                [Event "Test Game"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 1. Nf3 Nc6 2. Bb5 a6 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should detect the duplicate round
        assertNotNull(records, "Parser should return a list");
        assertTrue(records.contains(null), "List should contain a null record due to duplicate round");
    }

    @Test
    public void testParseMultipleGames() throws IOException {
        // Create a temporary PGN file with multiple games
        Path pgnFile = tempDir.resolve("multiple_games.pgn");
        String content = """
                [Event "Game 1"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bb5 a6 1-0
                
                [Event "Game 2"]
                [Site "Test Site"]
                [Date "2023.01.02"]
                [Round "2"]
                [White "Player 3"]
                [Black "Player 4"]
                [Result "0-1"]
                
                1. d4 d5 2. c4 e6 3. Nc3 Nf6 0-1
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should find both games
        assertNotNull(records, "Parser should return a list");
        assertEquals(2, records.size(), "Should find two games");
        assertNotNull(records.get(0), "First game should not be null");
        assertNotNull(records.get(1), "Second game should not be null");

    }


    @Test
    public void testParseCastlingMoves() throws IOException {
        // Create a temporary PGN file with castling moves
        Path pgnFile = tempDir.resolve("castling.pgn");
        String content = """
                [Event "Castling Test"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Bc4 Bc5 4. O-O Nf6 5. d3 O-O 6. Bg5 d6 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        assertNotNull(records, "Parser should return a list");
        assertFalse(records.isEmpty(), "List should not be empty");
        assertNotNull(records.getFirst(), "Record should not be null");

        Record record = records.getFirst();
        List<Move> moves = record.getMoves();

        // Check the castling moves
        Move whiteKingsideCastle = moves.get(6);
        Move blackKingsideCastle = moves.get(9);

        assertEquals("O-O", whiteKingsideCastle.getAction(), "White should have kingside castled");
        assertTrue(whiteKingsideCastle.isKingSideCastling(), "Should be identified as kingside castling");
        assertEquals(Color.white, whiteKingsideCastle.getColor(), "Should be white's move");

        assertEquals("O-O", blackKingsideCastle.getAction(), "Black should have kingside castled");
        assertTrue(blackKingsideCastle.isKingSideCastling(), "Should be identified as kingside castling");
        assertEquals(Color.black, blackKingsideCastle.getColor(), "Should be black's move");
    }

    @Test
    public void testParseQueensideCastling() throws IOException {
        // Create a temporary PGN file with queenside castling
        Path pgnFile = tempDir.resolve("queenside_castling.pgn");
        String content = """
                [Event "Queenside Castling Test"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. d4 d5 2. c4 e6 3. Nc3 Nf6 4. Bg5 Be7 5. e3 O-O 6. Nf3 b6 7. Qc2 Bb7 8. O-O-O Nbd7 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        assertNotNull(records, "Parser should return a list");
        assertNotNull(records.getFirst(), "Record should not be null");

        Record record = records.getFirst();
        List<Move> moves = record.getMoves();

        // Check the queenside castling move
        Move whiteQueensideCastle = moves.get(14);
        assertEquals("O-O-O", whiteQueensideCastle.getAction(), "White should have queenside castled");
        assertTrue(whiteQueensideCastle.isQueenSideCastling(), "Should be identified as queenside castling");
        assertEquals(Color.white, whiteQueensideCastle.getColor(), "Should be white's move");
    }

    @Test
    public void testParseCheckAndCheckmate() throws IOException {
        // Create a temporary PGN file with check and checkmate
        Path pgnFile = tempDir.resolve("check_checkmate.pgn");
        String content = """
                [Event "Check and Checkmate Test"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. Qh5 Nc6 3. Bc4 Nf6 4. Qxf7# 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        assertNotNull(records, "Parser should return a list");
        assertNotNull(records.getFirst(), "Record should not be null");

        Record record = records.getFirst();
        List<Move> moves = record.getMoves();

        // Check the checkmate move
        Move checkmate = moves.get(6);
        assertEquals("Qxf7#", checkmate.getAction(), "Should be a checkmate move");
        assertTrue(checkmate.isCheckmate(), "Should be identified as checkmate");
        assertTrue(checkmate.isCapture(), "Should be identified as a capture");
    }

    @Test
    public void testParsePawnPromotion() throws IOException {
        // Create a temporary PGN file with pawn promotion
        Path pgnFile = tempDir.resolve("promotion.pgn");
        String content = """
                [Event "Pawn Promotion Test"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. d4 exd4 3. c3 dxc3 4. Nxc3 d6 5. h4 Be6\s
                6. h5 a5 7. h6 a4 8. hxg7 a3 9. gxh8=Q+ Kd7 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        assertNotNull(records, "Parser should return a list");
        assertNotNull(records.getFirst(), "Record should not be null");

        Record record = records.getFirst();
        List<Move> moves = record.getMoves();

        // Check the promotion move
        Move promotion = moves.get(16);
        assertEquals("gxh8=Q+", promotion.getAction(), "Should be a promotion move");
        assertTrue(promotion.isPromotion(), "Should be identified as promotion");
        assertTrue(promotion.isCapture(), "Should be identified as a capture");
        assertTrue(promotion.isCheck(), "Should be identified as check");
        assertEquals('Q', promotion.getPromoted(), "Should be promoted to a queen");
    }



    @Test
    public void testParseInvalidSyntax() throws IOException {
        // Create a temporary PGN file with invalid syntax
        Path pgnFile = tempDir.resolve("invalid_syntax.pgn");
        String content = """
                [Event "Invalid Syntax Test"]
                [Site "Test Site"]
                [Date "2023.01.01"]
                [Round "1"]
                [White "Player 1"]
                [Black "Player 2"]
                [Result "1-0"]
                
                1. e4 e5 2. Nf3 Nc6 3. Zz4 Bc5 1-0
                """;
        Files.writeString(pgnFile, content);

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should detect the invalid move "Zz4"
        assertNotNull(records, "Parser should return a list");
        assertTrue(records.contains(null), "List should contain a null record due to invalid syntax");
    }

    @Test
    public void testParseEmptyFile() throws IOException {
        // Create an empty PGN file
        Path pgnFile = tempDir.resolve("empty.pgn");
        Files.writeString(pgnFile, "");

        List<Record> records = parser.parsingMoves(pgnFile.toString());

        // The parser should handle empty files
        assertNotNull(records, "Parser should return a list even for empty files");
    }
}