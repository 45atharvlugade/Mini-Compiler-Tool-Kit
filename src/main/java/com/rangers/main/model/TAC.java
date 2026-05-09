package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TAC {

    // =========================================
    // THREE ADDRESS CODE FIELDS
    // =========================================
    private String op;

    private String arg1;

    private String arg2;

    private String result;

    // =========================================
    // TAC STRING FORMAT
    // =========================================
    @Override
    public String toString() {

        // null safety
        String operation = safe(op);
        String a1 = safe(arg1);
        String a2 = safe(arg2);
        String res = safe(result);

        switch (operation) {

            // =================================
            // LABEL
            // =================================
            case "LABEL":
                return res + ":";

            // =================================
            // UNCONDITIONAL JUMP
            // =================================
            case "GOTO":
                return "GOTO " + res;

            // =================================
            // CONDITIONAL JUMP
            // =================================
            case "IF":
                return "IF " + a1 + " GOTO " + res;

            // =================================
            // CONDITIONAL FALSE JUMP
            // =================================
            case "IFFALSE":
                return "IF FALSE " + a1 + " GOTO " + res;

            // =================================
            // ASSIGNMENT
            // =================================
            case "=":
                return res + " = " + a1;

            // =================================
            // PRINT
            // =================================
            case "PRINT":
                return "PRINT " + a1;

            // =================================
            // READ INPUT
            // =================================
            case "READ":
                return "READ " + res;

            // =================================
            // FUNCTION PARAMETER
            // =================================
            case "PARAM":
                return "PARAM " + a1;

            // =================================
            // FUNCTION CALL
            // =================================
            case "CALL":
                return res + " = CALL " + a1;

            // =================================
            // RETURN
            // =================================
            case "RETURN":
                return "RETURN " + a1;
        }

        // =====================================
        // UNARY OPERATIONS
        // =====================================
        if (a2.isBlank() || a2.equals("-")) {

            return res +
                    " = " +
                    operation +
                    " " +
                    a1;
        }

        // =====================================
        // BINARY OPERATIONS
        // =====================================
        return res +
                " = " +
                a1 +
                " " +
                operation +
                " " +
                a2;
    }

    // =========================================
    // NULL SAFETY
    // =========================================
    private String safe(String value) {

        return value == null ? "" : value;
    }
}