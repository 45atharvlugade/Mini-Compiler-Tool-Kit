package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TripleGenerator {

    private List<Triple> triples = new ArrayList<>();
    private int index = 0;

    public List<Triple> generate(List<TAC> tacList) {

        triples.clear();
        index = 0;

        for (TAC tac : tacList) {

            String op = tac.getOp();
            String arg1 = tac.getArg1();
            String arg2 = tac.getArg2();

            triples.add(new Triple(
                    index++,
                    op,
                    arg1,
                    arg2
            ));
        }

        return triples;
    }
}