package org.example.parsing;

import org.example.simulation.pieces.attributes.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.*;

/**
 * This class handles the parsing of PGN (Portable Game Notation) files for chess games.
 * It extracts game metadata, moves, annotations, and results from PGN formatted text.
 */
public class GameParser {
    // Regular expression pattern for validating PGN tags
    private final Pattern VALID_TAG_PATTERN = Pattern.compile("^\\[([A-Za-z]+)\\s+\"(.*)\"\\]$");

    /**
     * Reads a line containing a PGN tag and extracts the key-value pair.
     *
     * @param line The line to parse containing a PGN tag
     * @return A string array with the tag name at index 0 and tag value at index 1, or null if invalid
     */
    private String[] readLine(String line){
        if(!VALID_TAG_PATTERN.matcher(line.trim()).matches()){
            return null;
        }

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();

        int index = 0;
        while (index < line.length()){
            if(line.charAt(index) == '['){
                index++;
                while (line.charAt(index) != '"'){
                    key.append(line.charAt(index));
                    index++;
                }
            }
            if(line.charAt(index) == '"'){
                index++;
                while (line.charAt(index) != '"'){
                    value.append(line.charAt(index));
                    index++;
                }
                break;
            }
            index++;
        }

        return new String[]{key.toString(), value.toString()};
    }

    // Regular expression for white moves in PGN notation
    private static final String whiteMoveRegex =
            "\\d+\\.\\s*" +
                    "(?:" +
                    // Pawn moves (e.g., e8=Q, exd8=Q+, gxh1=N#)
                    "[a-h](?:x[a-h][1-8]|[1-8])(?:=\\w)?[+#]?|" +
                    // King moves (no disambiguation)
                    "K(?:x?[a-h][1-8])(?:[+#])?|" +
                    // Other piece moves with optional disambiguation for Q, R, B, N
                    "(?:[QRBN])(?:[a-h]|[1-8]|[a-h][1-8])?x?[a-h][1-8](?:[+#])?|" +
                    // Castling moves
                    "O-O-O|" +
                    "O-O" +
                    ")" +
                    "\\s*(?:\\$\\d{1,3})?\\s*(?:\\{[^}]*\\})?";

    // Regular expression for black moves in PGN notation
    private static final String blackMoveRegex =
            "(?:\\d+\\.\\.\\.)?\\s*" +
                    "(?:" +
                    // Pawn moves (e.g., e8=Q, exd8=Q+, gxh1=N#)
                    "[a-h](?:x[a-h][1-8]|[1-8])(?:=\\w)?[+#]?|" +
                    // King moves (no disambiguation)
                    "K(?:x?[a-h][1-8])(?:[+#])?|" +
                    // Other piece moves with optional disambiguation for Q, R, B, N
                    "(?:[QRBN])(?:[a-h]|[1-8]|[a-h][1-8])?x?[a-h][1-8](?:[+#])?|" +
                    // Castling
                    "O-O-O|O-O" +
                    ")" +
                    "\\s*(?:\\$\\d{1,3})?\\s*(?:\\{[^}]*\\})?";

    // Regular expression for game result
    private final String resultRegex = "^(1-0|0-1|1/2-1/2|\\*)\\s*";

    /**
     * Extracts chess moves from a string of PGN movetext and creates a Record object.
     *
     * @param text The PGN movetext to parse
     * @param tags The metadata tags associated with the game
     * @return A Record object containing the parsed game, or null if parsing fails
     */
    private Record getMovesFromString(String text, Map<String,String> tags) {
        Map<Integer, Move[]> result = new HashMap<>();

        text = text.replaceAll("\n"," ").replaceAll("\\s+", " ").trim();

        Pattern resultPattern = Pattern.compile(resultRegex);

        int previousLevel = 0;

        String winner = null;

        label:
        while(true){
            Matcher whiteMatcher = Pattern.compile(whiteMoveRegex).matcher(text);
            int currentRound;
            Move whiteMove;
            Move blackMove;

            if(whiteMatcher.lookingAt()){
                // create white object Move and add to the result
                String white = text.substring(0,whiteMatcher.end());

                int i = 0;
                currentRound = 0;
                while (Character.isDigit(white.charAt(i))){
                    currentRound = currentRound*10 + Character.getNumericValue(white.charAt(i));
                    i++;
                }

                if(currentRound != previousLevel +1){
                    System.out.println("Missing round, last level identified was "+previousLevel);
                    break;
                }
                else if(result.containsKey(currentRound)){
                    System.out.println("Duplicate rounds, duplicated value: "+currentRound);
                }

                result.put(currentRound, new Move[2]);

                white = white.substring(i+1);

                whiteMove = generateMove(white,Color.white);

                result.get(currentRound)[0] = whiteMove;

                // remove white move
                text = text.substring(whiteMatcher.end()).trim();
            }
            else {
                System.out.println("Error at "+(previousLevel+1)+" during parsing white move");
                break;
            }

            Matcher resultMatcher = resultPattern.matcher(text);

            if(resultMatcher.lookingAt()){
                // meaning we do not have right move
                text = text.replace(" ","").trim();
                switch (text) {
                    case "1-0": winner = "white"; break;
                    case "0-1": winner = "black"; break;
                    case "1/2-1/2": winner = "draw"; break;
                    case "*": winner = "undefined"; break;
                    default: System.out.println("Error during identifying winner"); break label;
                }
                break;
            }

            Matcher blackMatcher = Pattern.compile(blackMoveRegex).matcher(text);

            if(blackMatcher.lookingAt()){
                String black = text.substring(0,blackMatcher.end()).trim();

                // if we have comment in white, we must have digit...
                if(whiteMove.getComment() == null && Character.isDigit(black.charAt(0))){
                    System.out.println("Wrong interpretation in black move, there is not comment in white: "+black);
                    break;
                }

                if(whiteMove.getComment() != null){
                    int blackRound = 0;
                    int i = 0;
                    while (Character.isDigit(black.charAt(i))){
                        blackRound = blackRound*10 + Character.getNumericValue(black.charAt(i));
                        i++;
                    }
                    if(blackRound != currentRound){
                        System.out.println("White round and black round are not same: "+whiteMove+ " != "+blackRound);
                        break;
                    }

                    black = black.substring(i+3);
                }

                blackMove = generateMove(black,Color.black);

                result.get(currentRound)[1] = blackMove;

                text = text.substring(blackMatcher.end()).trim();

                resultMatcher = resultPattern.matcher(text);
                if(resultMatcher.lookingAt()){
                    text = text.replace(" ","").trim();
                    switch (text) {
                        case "1-0": winner = "white"; break;
                        case "0-1": winner = "black"; break;
                        case "1/2-1/2": winner = "draw"; break;
                        case "*": winner = "undefined"; break;
                        default: System.out.println("Error during identifying winner"); break label;
                    }
                    break;
                }
            }
            else {
                System.out.println("Error at round "+currentRound+", undefined symbols");
                break;
            }

            previousLevel = currentRound;
        }

        if(winner != null){
            System.out.println("Successful game");
            System.out.println(winner);
            System.out.println("------------------------------------");
            return new Record(tags, result, winner);
        }

        return null;
    }

    /**
     * Generates a Move object from a PGN move string.
     * Parses the action, comments, and annotations from the move text.
     *
     * @param element The PGN move string to parse
     * @param color The color of the player making the move
     * @return A Move object representing the parsed move
     */
    private static Move generateMove(String element, Color color){
        String comment = null;
        String annotation = null;
        String action;

        if(element.contains("{")){
            comment = element.substring(element.indexOf("{"),element.indexOf("}")+1).trim();
        }

        if(element.contains("$")){
            action = element.substring(0,element.indexOf("$")).trim();
            if(element.contains("{")){
                annotation = element.substring(element.indexOf("$"),element.indexOf("{")).trim();
            }
            else {
                annotation = element.substring(element.indexOf("$"),element.length()-1).trim();
            }
        }
        else {
            if(element.contains("{")){
                action = element.substring(0,element.indexOf("{")).trim();
            }
            else {
                action = element.trim();
            }
        }
        Move move = new Move(action, comment, annotation, color);
        return move;
    }

    /**
     * Parses a PGN file and extracts all games into Record objects.
     *
     * @param filePath Path to the PGN file to parse
     * @return A list of Record objects, one for each game in the file
     */
    public List<Record> parsingMoves(String filePath){
        List<Record> records = new ArrayList<>();

        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;
            String lastLine = null;

            Map<String, String> tags = new HashMap<>();
            StringBuilder moves = new StringBuilder();


            while((line = reader.readLine()) != null){

                if(!line.isEmpty()){

                    if(line.charAt(0) == '['){

                        if(lastLine != null && lastLine.charAt(0) != '['){

                            Map<String, String> tag = new HashMap<>(tags);

                            records.add(getMovesFromString(moves.toString(),tag));


                            tags = new HashMap<>();
                            moves = new StringBuilder();
                        }
                        String[] tag = readLine(line);
                        if(tag != null){
                            tags.put(tag[0],tag[1]);
                        }
                    }
                    else {
                        moves.append(line).append("\n");
                    }
                    lastLine = line;
                }
            }

            Map<String, String> tag = new HashMap<>(tags);
            records.add(getMovesFromString(moves.toString(),tag));
        }
        catch (IOException e){
            System.out.println("Error during reading file");
        }

        return records;
    }
}