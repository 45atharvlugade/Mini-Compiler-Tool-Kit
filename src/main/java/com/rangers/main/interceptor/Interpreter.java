package com.rangers.main.interceptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class Interpreter {

    // =========================================
    // MEMORY
    // =========================================
    private Map<String, Object> memory =
            new HashMap<>();

    // =========================================
    // LABEL TABLE
    // =========================================
    private Map<String, Integer> labels =
            new HashMap<>();

    // =========================================
    // OUTPUT & TRACE
    // =========================================
    private StringBuilder output =
            new StringBuilder();

    private StringBuilder trace =
            new StringBuilder();

    // =========================================
    // MAIN EXECUTOR
    // =========================================
    public String execute(List<String> code) {

        memory.clear();
        labels.clear();

        output.setLength(0);
        trace.setLength(0);

        // =====================================
        // FIRST PASS -> STORE LABELS
        // =====================================
        for (int i = 0; i < code.size(); i++) {

            String line =
                    clean(code.get(i));

            if (line.endsWith(":")) {

                String label =
                        line.replace(":", "")
                            .trim();

                labels.put(label, i);
            }
        }

        int executionCount = 0;
        int MAX_EXECUTIONS = 10000;

        // =====================================
        // EXECUTION
        // =====================================
        for (int i = 0; i < code.size(); i++) {

            if (executionCount++ > MAX_EXECUTIONS) {
                output.append("\n[ERROR] Execution limit reached. Infinite loop detected!\n");
                trace.append("\n[ERROR] Execution limit reached. Infinite loop detected!");
                break;
            }

            String instruction =
                    clean(code.get(i));

            if (instruction.isBlank()) {
                continue;
            }

            trace.append("\n[LINE ")
                 .append(i)
                 .append("] ")
                 .append(instruction);

            // =================================
            // LABEL
            // =================================
            if (instruction.endsWith(":")) {

                trace.append(" → LABEL");

                continue;
            }

            // =================================
            // JMP
            // =================================
            if (instruction.startsWith("JMP")) {

                String[] parts =
                        instruction.split("\\s+");

                String label = parts[1];

                trace.append(" → JUMP ")
                     .append(label);

                Integer jump =
                        labels.get(label);

                if (jump == null) {

                    throw new RuntimeException(
                            "Undefined label: "
                            + label
                    );
                }

                i = jump;

                continue;
            }

            // =================================
            // JNZ
            // =================================
            if (instruction.startsWith("JNZ")) {

                String temp =
                        instruction.substring(3)
                                   .trim();

                String[] parts =
                        temp.split(",");

                String condition =
                        parts[0].trim();

                String label =
                        parts[1].trim();

                int value =
                        toInt(
                                evaluate(condition)
                        );

                if (value != 0) {

                    trace.append(" → TRUE JUMP ")
                         .append(label);

                    Integer jump =
                            labels.get(label);

                    if (jump == null) {

                        throw new RuntimeException(
                                "Undefined label: "
                                + label
                        );
                    }

                    i = jump;

                    continue;
                }

                trace.append(" → FALSE");
            }

            // =================================
            // JZ
            // =================================
            else if (instruction.startsWith("JZ")) {

                String temp =
                        instruction.substring(2)
                                   .trim();

                String[] parts =
                        temp.split(",");

                String condition =
                        parts[0].trim();

                String label =
                        parts[1].trim();

                int value =
                        toInt(
                                evaluate(condition)
                        );

                if (value == 0) {

                    trace.append(" → ZERO JUMP ")
                         .append(label);

                    Integer jump =
                            labels.get(label);

                    if (jump == null) {

                        throw new RuntimeException(
                                "Undefined label: "
                                + label
                        );
                    }

                    i = jump;

                    continue;
                }

                trace.append(" → NON ZERO");
            }

            // =================================
            // PRINT
            // =================================
            else if (instruction.startsWith("PRINT")) {

                String expr =
                        instruction
                        .replace("PRINT", "")
                        .trim();

                Object value =
                        evaluate(expr);

                output.append(value)
                      .append("\n");

                trace.append(" → OUTPUT ")
                     .append(value);
            }

            // =================================
            // READ
            // =================================
            else if (instruction.startsWith("READ")) {

                String var =
                        instruction
                        .replace("READ", "")
                        .trim();

                memory.put(var, 0);

                trace.append(" → INPUT ")
                     .append(var);
            }

            // =================================
            // CMP
            // =================================
            else if (instruction.startsWith("CMP")) {

                trace.append(" → COMPARE");
            }

            // =================================
            // MOV
            // =================================
            else if (instruction.startsWith("MOV")) {

                String temp =
                        instruction
                        .replace("MOV", "")
                        .trim();

                String[] parts =
                        temp.split(",");

                String left =
                        parts[0].trim();

                String right =
                        parts[1].trim();

                Object value =
                        evaluate(right);

                memory.put(left, value);

                trace.append(" → ")
                     .append(left)
                     .append(" = ")
                     .append(value);
            }

            // =================================
            // ADD
            // =================================
            else if (instruction.startsWith("ADD")) {

                arithmetic(
                        instruction,
                        "+"
                );
            }

            // =================================
            // SUB
            // =================================
            else if (instruction.startsWith("SUB")) {

                arithmetic(
                        instruction,
                        "-"
                );
            }

            // =================================
            // MUL
            // =================================
            else if (instruction.startsWith("MUL")) {

                arithmetic(
                        instruction,
                        "*"
                );
            }

            // =================================
            // DIV
            // =================================
            else if (instruction.startsWith("DIV")) {

                arithmetic(
                        instruction,
                        "/"
                );
            }
        }

        // =====================================
        // FINAL RESULT
        // =====================================
        return
                "============= OUTPUT =============\n\n"
                +
                output.toString()
                +
                "\n============= TRACE ============="
                +
                trace.toString()
                +
                "\n\n============= MEMORY =============\n"
                +
                memory;
    }

    // =========================================
    // ARITHMETIC EXECUTOR
    // =========================================
    private void arithmetic(
            String instruction,
            String op
    ) {

        String temp =
                instruction
                .substring(3)
                .trim();

        String[] parts =
                temp.split(",");

        String register =
                parts[0].trim();

        String valueExpr =
                parts[1].trim();

        int left =
                toInt(
                        evaluate(register)
                );

        int right =
                toInt(
                        evaluate(valueExpr)
                );

        int result = 0;

        switch (op) {

            case "+":
                result = left + right;
                break;

            case "-":
                result = left - right;
                break;

            case "*":
                result = left * right;
                break;

            case "/":
                result = left / right;
                break;
        }

        memory.put(register, result);

        trace.append(" → ")
             .append(register)
             .append(" = ")
             .append(result);
    }

    // =========================================
    // EVALUATE
    // =========================================
    private Object evaluate(String expr) {

        expr = expr.trim();

        // =====================================
        // STRING
        // =====================================
        if (
                expr.startsWith("\"")
                &&
                expr.endsWith("\"")
        ) {

            return expr.substring(
                    1,
                    expr.length() - 1
            );
        }

        // =====================================
        // NUMBER
        // =====================================
        if (isNumeric(expr)) {

            return Integer.parseInt(expr);
        }

        // =====================================
        // BOOLEAN
        // =====================================
        if ("true".equals(expr)) {
            return 1;
        }

        if ("false".equals(expr)) {
            return 0;
        }

        // =====================================
        // VARIABLE
        // =====================================
        if (memory.containsKey(expr)) {

            return memory.get(expr);
        }

        return expr;
    }

    // =========================================
    // TO INTEGER
    // =========================================
    private int toInt(Object obj) {

        if (obj instanceof Integer) {

            return (Integer) obj;
        }

        return Integer.parseInt(
                obj.toString()
        );
    }

    // =========================================
    // NUMERIC CHECK
    // =========================================
    private boolean isNumeric(String str) {

        return str.matches("-?\\d+");
    }

    // =========================================
    // CLEAN
    // =========================================
    private String clean(String line) {

        if (line == null) {
            return "";
        }

        return line
                .replace("\n", "")
                .replace("\r", "")
                .trim();
    }
}