package org.example;
import org.example.parsing.GameParser;
import org.example.parsing.Record;
import org.example.simulation.GameSimulator;

import java.util.List;

public class Main {

    // counting successful moves
    public static int countMoves = 0;

    // core of the program
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a path to a PGN file");
            return;
        }

        String filePath = args[0]; // Gets the first command-line argument
        GameParser gameParser = new GameParser();

        List<Record> records = gameParser.parsingMoves(filePath);

        for(Record record : records){
            GameSimulator gameSimulator = new GameSimulator(record);
            gameSimulator.runGame();
        }

        System.out.println(countMoves);
    }

    // testing
//    public static void main(String[] args) {
////        String filePath = "C:\\autocode-demo\\ChessGame\\src\\main\\java\\org\\example\\badOne.pgn";
//        String filePath = "src/test/java/real_PGN_examples/badOne.pgn";
//        GameParser gameParser = new GameParser();
//
//        // return all games, parsed into objects, each game in Record.
//        List<Record> records = gameParser.parsingMoves(filePath);
//
//        for(Record record : records){
//            GameSimulator gameSimulator = new GameSimulator(record);
//            gameSimulator.runGame();
//        }
//
//        System.out.println(countMoves);
//    }

}