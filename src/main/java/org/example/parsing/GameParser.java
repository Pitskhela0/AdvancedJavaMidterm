package org.example.parsing;

import org.example.simulation.pieces.attributes.Color;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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


    private final String resultRegex = "^(1-0|0-1|1/2-1/2|\\*)\\s*";


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
//                System.out.println(white);

                int i = 0;
                currentRound = 0;
                while (Character.isDigit(white.charAt(i))){
                    currentRound = currentRound*10 + Character.getNumericValue(white.charAt(i));
                    i++;
                }
//                System.out.println(currentRound);

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

//                System.out.println(whiteMove.toString());
//
//
//                System.out.println(whiteMove.getAction());
//                System.out.println(whiteMove.getAnnotation());
//                System.out.println(whiteMove.getComment());
//                System.out.println("-----------------");


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
//                System.out.println(black);

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

//                System.out.println(blackMove.toString());

                result.get(currentRound)[1] = blackMove;

//                System.out.println(blackMove.getAction());
//                System.out.println(blackMove.getAnnotation());
//                System.out.println(blackMove.getComment());
//
//                System.out.println("--------------end of a round--------------");


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

//    public static void main(String[] args) {
//        GameParser g = new GameParser();
////        String filePath = "C:\\autocode-demo\\ChessGame\\src\\main\\java\\org\\example\\Tbilisi2015.pgn";
//        String badOne = "src/main/java/org/example/badOne.pgn";
//        List<Record> records = g.parsingMoves(badOne);
//
//        records.forEach((element)->{
//            if(element == null){
//                System.out.println("fail, something missing");
//            }
//            else {
//                System.out.println(element.getTags());
//            }
//        });
//
//
//        System.out.println(records.size());
//    }
}
