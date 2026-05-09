package com.rangers.main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Optimizer {

    public List<Quadruple> optimize(List<Quadruple> input) {

        // Stores constant values
        Map<String, Object> constants = new HashMap<>();

        List<Quadruple> optimized = new ArrayList<>();

        for (Quadruple q : input) {

            String op = q.getOp();
            String arg1 = q.getArg1();
            String arg2 = q.getArg2();
            String result = q.getResult();

            // =========================================
            // CONSTANT ASSIGNMENT
            // =========================================
            if (op.equals("=")) {

                Object value = parseValue(arg1);

                if (value != null) {
                    constants.put(result, value);
                }

                optimized.add(q);
            }

            // =========================================
            // CONSTANT FOLDING
            // =========================================
            else if (constants.containsKey(arg1)
                    && constants.containsKey(arg2)) {

                Object a = constants.get(arg1);
                Object b = constants.get(arg2);

                Object foldedResult = evaluate(op, a, b);

                if (foldedResult != null) {

                    constants.put(result, foldedResult);

                    optimized.add(
                        new Quadruple(
                            "=",
                            foldedResult.toString(),
                            "-",
                            result
                        )
                    );
                } else {
                    optimized.add(q);
                }
            }

            // =========================================
            // DEFAULT CASE
            // =========================================
            else {
                optimized.add(q);
            }
        }

        return optimized;
    }

    // =====================================================
    // PARSE VALUE
    // =====================================================
    private Object parseValue(String value) {

        if (value == null) {
            return null;
        }

        // Integer
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
        }

        // String Literal
        if (value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }

        return null;
    }

    // =====================================================
    // EVALUATE EXPRESSIONS
    // =====================================================
    private Object evaluate(String op, Object a, Object b) {

        // ================= INTEGER OPERATIONS =================
        if (a instanceof Integer && b instanceof Integer) {

            int x = (Integer) a;
            int y = (Integer) b;

            return switch (op) {

                case "+" -> x + y;
                case "-" -> x - y;
                case "*" -> x * y;
                case "/" -> y != 0 ? x / y : null;

                // Relational Operators
                case "<" -> (x < y) ? 1 : 0;
                case ">" -> (x > y) ? 1 : 0;
                case "<=" -> (x <= y) ? 1 : 0;
                case ">=" -> (x >= y) ? 1 : 0;
                case "==" -> (x == y) ? 1 : 0;
                case "!=" -> (x != y) ? 1 : 0;

                default -> null;
            };
        }

        // ================= STRING OPERATIONS =================
        if (a instanceof String && b instanceof String) {

            String x = (String) a;
            String y = (String) b;

            return switch (op) {

                // String Concatenation
                case "+" -> "\"" + x + y + "\"";

                // String Comparisons
                case "==" -> x.equals(y) ? 1 : 0;
                case "!=" -> !x.equals(y) ? 1 : 0;

                default -> null;
            };
        }

        return null;
    }
}