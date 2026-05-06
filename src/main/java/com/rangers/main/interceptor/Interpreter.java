package com.rangers.main.interceptor;

import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class Interpreter {

    private Map<String, Integer> memory = new HashMap<>();
    private Map<String, Integer> labels = new HashMap<>();

    private StringBuilder output = new StringBuilder();
    private StringBuilder trace = new StringBuilder();

    public String execute(List<String> code) {

        memory.clear();
        labels.clear();
        output.setLength(0);
        trace.setLength(0);

        // ================= FIRST PASS (LABELS) =================
        for (int i = 0; i < code.size(); i++) {

            String line = clean(code.get(i));

            if (line.endsWith(":")) {
                String label = line.replace(":", "").trim();

                // store NEXT instruction index (IMPORTANT FIX)
                labels.put(label, i + 1);
            }
        }

        // ================= EXECUTION =================
        for (int i = 0; i < code.size(); i++) {

            String instruction = clean(code.get(i));

            trace.append("\n[LINE ").append(i).append("] ").append(instruction);

            if (instruction.isEmpty()) continue;

            // ================= LABEL =================
            if (instruction.endsWith(":")) {
                continue;
            }

            // ================= GOTO / JMP =================
            if (instruction.startsWith("goto") || instruction.startsWith("JMP")) {

                String[] parts = instruction.split("\\s+");
                String label = parts[1];

                trace.append(" → GOTO ").append(label);

                Integer jumpIndex = labels.get(label);

                if (jumpIndex == null) {
                    throw new RuntimeException("Undefined label: " + label);
                }

                i = jumpIndex;
                continue;
            }

            // ================= IF FALSE =================
            if (instruction.startsWith("ifFalse")) {

                String temp = instruction.substring(7).trim(); // remove "ifFalse"

                String condition = temp.substring(0, temp.lastIndexOf("goto")).trim();
                String label = temp.substring(temp.lastIndexOf("goto") + 4).trim();

                int result = evaluateCondition(condition);

                if (result == 0) {

                    trace.append(" → ifFalse TRUE (jump)");

                    Integer jumpIndex = labels.get(label);

                    if (jumpIndex == null) {
                        throw new RuntimeException("Undefined label: " + label);
                    }

                    i = jumpIndex - 1; // adjust for loop increment
                    continue;
                } else {
                    trace.append(" → ifFalse FALSE");
                }
            }

            // ================= ASSIGNMENT =================
            else if (instruction.contains("=")) {

                String[] parts = instruction.split("=");
                String left = parts[0].trim();
                String right = parts[1].trim();

                int value = evaluate(right);
                memory.put(left, value);

                trace.append(" → ").append(left)
                      .append(" = ").append(value);
            }

            // ================= PRINT =================
            else if (instruction.startsWith("PRINT")) {

                String expr = instruction.replace("PRINT", "").trim();

                int val;

                if (memory.containsKey(expr)) {
                    val = memory.get(expr);
                } else if (isNumeric(expr)) {
                    val = Integer.parseInt(expr);
                } else {
                    throw new RuntimeException("Undefined variable in PRINT: " + expr);
                }

                output.append(val).append("\n");
                trace.append(" → PRINT ").append(val);
            }
        }

        return "OUTPUT:\n" + output.toString().trim()
             + "\n\nTRACE:\n" + trace.toString();
    }

    // ================= CLEAN =================
    private String clean(String line) {
        if (line == null) return "";
        return line.replace("\n", "")
                   .replace("\r", "")
                   .trim();
    }

    // ================= EVALUATOR =================
    private int evaluate(String expr) {

        expr = expr.trim();

        if (isNumeric(expr)) return Integer.parseInt(expr);

        if (memory.containsKey(expr)) return memory.get(expr);

        if (expr.contains("+")) {
            String[] p = expr.split("\\+");
            return evaluate(p[0]) + evaluate(p[1]);
        }

        if (expr.contains("-")) {
            String[] p = expr.split("\\-");
            return evaluate(p[0]) - evaluate(p[1]);
        }

        if (expr.contains("*")) {
            String[] p = expr.split("\\*");
            return evaluate(p[0]) * evaluate(p[1]);
        }

        if (expr.contains("/")) {
            String[] p = expr.split("\\/");
            return evaluate(p[0]) / evaluate(p[1]);
        }

        throw new RuntimeException("Invalid expression: " + expr);
    }

    private boolean isNumeric(String str) {
        return str.matches("-?\\d+");
    }
    
    private int evaluateCondition(String expr) {

        expr = expr.trim();

        String[] parts;

        if (expr.contains("<")) {
            parts = expr.split("<");
            return evaluate(parts[0].trim()) < evaluate(parts[1].trim()) ? 1 : 0;
        }

        if (expr.contains(">")) {
            parts = expr.split(">");
            return evaluate(parts[0].trim()) > evaluate(parts[1].trim()) ? 1 : 0;
        }

        if (expr.contains("==")) {
            parts = expr.split("==");
            return evaluate(parts[0].trim()) == evaluate(parts[1].trim()) ? 1 : 0;
        }

        if (expr.contains("!=")) {
            parts = expr.split("!=");
            return evaluate(parts[0].trim()) != evaluate(parts[1].trim()) ? 1 : 0;
        }

        if (expr.contains("<=")) {
            parts = expr.split("<=");
            return evaluate(parts[0].trim()) <= evaluate(parts[1].trim()) ? 1 : 0;
        }

        if (expr.contains(">=")) {
            parts = expr.split(">=");
            return evaluate(parts[0].trim()) >= evaluate(parts[1].trim()) ? 1 : 0;
        }

        throw new RuntimeException("Invalid condition: " + expr);
    }
}