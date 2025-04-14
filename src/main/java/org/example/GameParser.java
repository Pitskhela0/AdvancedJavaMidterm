package org.example;

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

    private List<Move> getMovesFromString(String textMoves){
        System.out.println(textMoves);
        textMoves = textMoves.replace("\n"," ");
        String[] moves = textMoves.split("\\.");

        // todo check every move

        return null;
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
                            List<Move> moveList = getMovesFromString(moves.toString());
                            Map<String, String> tag = new HashMap<>(tags);
                            games.add(new Record(tag,moveList));
                            tags = new HashMap<>();
                            moves = new StringBuilder();
                        }
                        String[] tag = readLine(line);
                        if(tag != null){
                            tags.put(tag[0],tag[1]);
                        }
                    }
                    else {
                        moves.append(line);
                    }
                    lastLine = line;
                }
            }
            List<Move> moveList = getMovesFromString(moves.toString());
            Map<String, String> tag = new HashMap<>(tags);
            games.add(new Record(tag,moveList));

        }
        catch (IOException e){
            System.out.println("Error during reading file");
        }
        return games;
    }

    public static void main(String[] args) {
        GameParser g = new GameParser();
        String filePath = "C:\\autocode-demo\\ChessGame\\src\\main\\java\\org\\example\\Tbilisi2015.pgn";
        g.parsingMoves(filePath).forEach((element)->{
            System.out.println(element.getTags());
        });
    }
}
