package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Record {
    private Map<String, String> tags;
    private List<Move> moves;

    public Record(Map<String, String> tags, List<Move> moves){
        this.tags = tags;
        this.moves = moves;
    }

    public List<Move> getMoves(){
        return this.moves;
    }
    public Map<String,String> getTags(){
        return tags;
    }
}
