package com.rangers.main.model;

import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class IndirectTripleGenerator {

    private List<Triple> triples;
    private List<Integer> pointerTable;

    public Map<String, Object> generate(List<Triple> tripleList) {

        this.triples = new ArrayList<>();
        this.pointerTable = new ArrayList<>();

        int index = 0;

        for (Triple t : tripleList) {

            this.triples.add(t);
            this.pointerTable.add(index);
            index++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pointerTable", this.pointerTable);
        result.put("triples", this.triples);

        return result;
    }
}