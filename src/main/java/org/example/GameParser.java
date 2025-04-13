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

                int length = key.length();
                int i = length-1;

                while (line.charAt(i) == ' '){
                    key.deleteCharAt(i);
                    i--;
                }
            }
            if(line.charAt(index) == '"'){
                index++;
                while (line.charAt(index) != '"'){
                    value.append(line.charAt(index));
                    index++;
                }
            }
            if(line.charAt(index) == '"'){
                break;
            }
            index++;
        }

        return new String[]{key.toString(), value.toString()};
    }


    private List<Move> getMovesFromStringOfMoves(String textMoves){
        return null;
    }

    public List<Move> parsingMoves(String filePath){
        try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
            String line;

            List<Record> games = new ArrayList<>();

            Map<String, String> tags = new HashMap<>();
            StringBuilder moves = new StringBuilder();

            boolean newGame = true;

            while((line = reader.readLine()) != null){
                if(!line.isEmpty()){
                    if(line.charAt(0) == '['){
                        String[] tag = readLine(line);
                        if(tag != null){
                            tags.put(tag[0],tag[1]);
                        }
                    }
                    else {
                        moves.append(line);
                    }
                }

                Thread.sleep(500);
            }
        }
        catch (IOException | InterruptedException e){
            System.out.println("Error during reading file");
        }
        return null;
    }

    public static void main(String[] args) {
        GameParser g = new GameParser();
        g.readLine("[yoyoyo             \"ha\"]");
    }

//    private boolean isValid(String move){
//
//    }
}
