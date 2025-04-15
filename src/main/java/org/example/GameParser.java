package org.example;

import org.example.piece_attributes.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.*;
import java.util.regex.*;

public class GameParser {
    private final Pattern VALID_TAG_PATTERN = Pattern.compile("^\\[([A-Za-z]+)\\s+\"(.*)\"\\]$");


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

    private static final String whiteMoveRegex =
            "\\d+\\.\\s*" +
                    "(?:[a-h][a-h1-8x=+#]*|" +        // Pawn moves: starts with lowercase a–h
                    "[KQRBN][a-h1-8x=+#]*|" +         // Piece moves: starts with uppercase K, Q, R, B, N
                    "O-O-O|O-O)" +                    // Castling
                    "(?:=\\w)?[+#]?\\s*" +            // Optional promotion, check/mate
                    "(?:\\$\\d{1,3})?\\s*" +          // Optional annotation
                    "(?:\\{[^}]*\\})?" ;              // Optional comment




    private final String blackMoveRegex =
            "(?:\\d+\\.\\.\\.)?\\s*" +                           // Optional move number (e.g., 3...)
                    "(?:[a-h][a-h1-8x=+#]*|" +        // Pawn moves: starts with lowercase a–h
                    "[KQRBN][a-h1-8x=+#]*|" +         // Piece moves: starts with uppercase K, Q, R, B, N
                    "O-O-O|O-O)" +                    // Castling
                    "(?:=\\w)?[+#]?\\s*" +            // Optional promotion, check/mate
                    "(?:\\$\\d{1,3})?\\s*" +          // Optional annotation
                    "(?:\\{[^}]*\\})?" ;              // Optional comment


    private final String resultRegex = "^(1-0|0-1|1/2-1/2|\\*)\\s*";


    public Record getMovesFromString(String text,Map<String,String> tags) {
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
                System.out.println(white);

                int i = 0;
                currentRound = 0;
                while (Character.isDigit(white.charAt(i))){
                    currentRound = currentRound*10 + Character.getNumericValue(white.charAt(i));
                    i++;
                }
                System.out.println(currentRound);

                if(currentRound != previousLevel +1){
                    System.out.println("Missing round, last level identified was "+previousLevel);
                    break;
                }
                else if(result.containsKey(currentRound)){
                    System.out.println("Duplicate rounds, duplicated value: "+currentRound);
                }

                result.put(currentRound, new Move[2]);

                white = white.substring(i+1);

                whiteMove = generateMove(white);

                System.out.println(whiteMove.getAction());
                System.out.println(whiteMove.getAnnotation());
                System.out.println(whiteMove.getComment());
                System.out.println("-----------------");


                if(!whiteMove.isValid()){
                    System.out.println("Invalid move at "+ previousLevel+1);
                    break;
                }
                result.get(currentRound)[0] = whiteMove;

                // remove white move
                text = text.substring(whiteMatcher.end()).trim();
            }
            else {
                System.out.println("Error at "+previousLevel+1+" during parsing white move");
                break;
            }

            Matcher resultMatcher = resultPattern.matcher(text);

            if(resultMatcher.lookingAt()){
                // meaning we do not have right move
                text = text.replace(" ","").trim();
                switch (text) {
                    case "1-0": winner = "White"; break;
                    case "0-1": winner = "Black"; break;
                    case "1/2-1/2": winner = "Draw"; break;
                    case "*": winner = "undifiend"; break;
                    default: System.out.println("Error during identifying winner"); break label;
                }
                break;
            }

            Matcher blackMatcher = Pattern.compile(blackMoveRegex).matcher(text);

            if(blackMatcher.lookingAt()){
                String black = text.substring(0,blackMatcher.end()).trim();
                System.out.println(black);

                if(whiteMove.getComment() != null){
                    int blackRound = 0;
                    int i = 0;
                    while (Character.isDigit(black.charAt(i))){
                        blackRound = blackRound*10 + Character.getNumericValue(black.charAt(i));
                        i++;
                    }
                    if(blackRound != currentRound){
                        System.out.println("White round and black round are not same: "+whiteMove+ " != "+blackRound);
                    }

                    black = black.substring(i+3);
                }

                blackMove = generateMove(black);

                result.get(currentRound)[1] = blackMove;

                System.out.println(blackMove.getAction());
                System.out.println(blackMove.getAnnotation());
                System.out.println(blackMove.getComment());

                System.out.println("--------------end of a round--------------");

                if(!blackMove.isValid()){
                    System.out.println("Invalid move at "+ previousLevel+1);
                    break;
                }

                text = text.substring(blackMatcher.end()).trim();

                resultMatcher = resultPattern.matcher(text);
                if(resultMatcher.lookingAt()){
                    text = text.replace(" ","").trim();
                    switch (text) {
                        case "1-0": winner = "White"; break;
                        case "0-1": winner = "Black"; break;
                        case "1/2-1/2": winner = "Draw"; break;
                        case "*": winner = "undifiend"; break;
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
        System.out.println(winner);
        return new Record(tags, result, winner);
    }

    private static Move generateMove(String black){
        String comment = null;
        String annotation = null;
        String action;

        if(black.contains("{")){
            comment = black.substring(black.indexOf("{"),black.indexOf("}")+1).trim();
        }

        if(black.contains("$")){
            action = black.substring(0,black.indexOf("$")).trim();
            if(black.contains("{")){
                annotation = black.substring(black.indexOf("$"),black.indexOf("{")).trim();
            }
            else {
                annotation = black.substring(black.indexOf("$"),black.length()-1).trim();
            }
        }
        else {
            if(black.contains("{")){
                action = black.substring(0,black.indexOf("{")).trim();
            }
            else {
                action = black.trim();
            }
        }
        return new Move(action,comment,annotation);
    }

    public List<Record> parsingMoves(String filePath){
        List<Record> games = new ArrayList<>();

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
                            games.add(getMovesFromString(moves.toString(),tag));
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
            games.add(getMovesFromString(moves.toString(),tag));
        }
        catch (IOException e){
            System.out.println("Error during reading file");
        }
        return games;
    }

    public static void main(String[] args) {
        GameParser g = new GameParser();
        String filePath = "C:\\autocode-demo\\ChessGame\\src\\main\\java\\org\\example\\Tbilisi2015.pgn";
        // todo test code
//        g.parsingMoves(filePath).forEach((element)->{
//            System.out.println(element.getTags());
//        });
    }
}
