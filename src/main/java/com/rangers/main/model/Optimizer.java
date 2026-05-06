package com.rangers.main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Optimizer {

    public List<Quadruple> optimize(List<Quadruple> input) {

        Map<String, Integer> constants = new HashMap<>();
        List<Quadruple> optimized = new ArrayList<>();

        for (Quadruple q : input) {

            // ===== CONSTANT ASSIGNMENT =====
            if (q.getOp().equals("=")) {

                try {
                    int value = Integer.parseInt(q.getArg1());
                    constants.put(q.getResult(), value);

                    optimized.add(q);

                } catch (Exception e) {
                    optimized.add(q);
                }
            }

            // ===== CONSTANT FOLDING =====
            else if (constants.containsKey(q.getArg1()) &&
                     constants.containsKey(q.getArg2())) {

                int a = constants.get(q.getArg1());
                int b = constants.get(q.getArg2());

                int result = 0;

                switch (q.getOp()) {
                    case "+" -> result = a + b;
                    case "-" -> result = a - b;
                    case "*" -> result = a * b;
                    case "/" -> result = a / b;
                }

                // create optimized result
                constants.put(q.getResult(), result);

                optimized.add(new Quadruple("=", String.valueOf(result), "-", q.getResult()));
            }

            // ===== DEFAULT CASE =====
            else {
                optimized.add(q);
            }
        }

        return optimized;
    }
}