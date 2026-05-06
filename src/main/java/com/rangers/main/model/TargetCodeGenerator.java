package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TargetCodeGenerator {

    private int registerCount = 0;

    private String newRegister() {
        return "R" + (++registerCount);
    }

    private String clean(String s) {
        if (s == null) return "";
        return s.replace("\n", "")
                .replace("\r", "")
                .replace(",", "")
                .trim();
    }

    public List<String> generate(List<TAC> tacList) {

        registerCount = 0;

        List<String> code = new ArrayList<>();

        for (TAC t : tacList) {

            String op = clean(t.getOp());
            String arg1 = clean(t.getArg1());
            String arg2 = clean(t.getArg2());
            String result = clean(t.getResult());

            // ================= ASSIGN =================
            if ("=".equals(op)) {
                code.add("MOV " + result + ", " + arg1);
            }

            // ================= ARITHMETIC =================
            else if ("+".equals(op) || "-".equals(op) ||
                     "*".equals(op) || "/".equals(op)) {

                String r = newRegister();

                code.add("MOV " + r + ", " + arg1);

                switch (op) {
                    case "+" -> code.add("ADD " + r + ", " + arg2);
                    case "-" -> code.add("SUB " + r + ", " + arg2);
                    case "*" -> code.add("MUL " + r + ", " + arg2);
                    case "/" -> code.add("DIV " + r + ", " + arg2);
                }

                code.add("MOV " + result + ", " + r);
            }

            // ================= IF =================
            else if ("IF".equals(op)) {

                // SAFE FORMAT HANDLING
                if (arg2.isEmpty()) {
                    // format: IF condition GOTO label
                    code.add("IF " + arg1 + " GOTO " + result);
                } else {
                    // format: IF temp GOTO label
                    code.add("IF " + arg1 + " GOTO " + result);
                }
            }

            // ================= GOTO =================
            else if ("GOTO".equals(op)) {
                code.add("JMP " + result);
            }

            // ================= LABEL =================
            else if ("LABEL".equals(op)) {
                code.add(result + ":");
            }
        }

        return code;
    }
}