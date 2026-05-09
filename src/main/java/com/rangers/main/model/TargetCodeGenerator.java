package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TargetCodeGenerator {

    // =========================================
    // REGISTER COUNTER
    // =========================================
    private int registerCount = 0;

    // =========================================
    // GENERATE NEW REGISTER
    // =========================================
    private String newRegister() {

        return "R" + (++registerCount);
    }

    // =========================================
    // CLEAN STRING
    // =========================================
    private String clean(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\n", "")
                .replace("\r", "")
                .replace(",", "")
                .trim();
    }

    // =========================================
    // MAIN TARGET CODE GENERATOR
    // =========================================
    public List<String> generate(List<TAC> tacList) {

        registerCount = 0;

        List<String> targetCode =
                new ArrayList<>();

        // =====================================
        // PROCESS EACH TAC
        // =====================================
        for (TAC tac : tacList) {

            String op =
                    clean(tac.getOp());

            String arg1 =
                    clean(tac.getArg1());

            String arg2 =
                    clean(tac.getArg2());

            String result =
                    clean(tac.getResult());

            // =================================
            // ASSIGNMENT
            // a = b
            // =================================
            if ("=".equals(op)) {

                targetCode.add(
                        "MOV " +
                        result +
                        ", " +
                        arg1
                );
            }

            // =================================
            // ADDITION
            // t1 = a + b
            // =================================
            else if ("+".equals(op)) {

                handleArithmetic(
                        targetCode,
                        "ADD",
                        arg1,
                        arg2,
                        result
                );
            }

            // =================================
            // SUBTRACTION
            // =================================
            else if ("-".equals(op)) {

                handleArithmetic(
                        targetCode,
                        "SUB",
                        arg1,
                        arg2,
                        result
                );
            }

            // =================================
            // MULTIPLICATION
            // =================================
            else if ("*".equals(op)) {

                handleArithmetic(
                        targetCode,
                        "MUL",
                        arg1,
                        arg2,
                        result
                );
            }

            // =================================
            // DIVISION
            // =================================
            else if ("/".equals(op)) {

                handleArithmetic(
                        targetCode,
                        "DIV",
                        arg1,
                        arg2,
                        result
                );
            }

            // =================================
            // RELATIONAL OPERATIONS
            // =================================
            else if (
                    "<".equals(op)  ||
                    ">".equals(op)  ||
                    "<=".equals(op) ||
                    ">=".equals(op) ||
                    "==".equals(op) ||
                    "!=".equals(op)
            ) {

                String register =
                        newRegister();

                targetCode.add(
                        "MOV " +
                        register +
                        ", " +
                        arg1
                );

                targetCode.add(
                        "CMP " +
                        register +
                        ", " +
                        arg2
                );

                targetCode.add(
                        "MOV " +
                        result +
                        ", " +
                        register
                );
            }

            // =================================
            // IF TRUE
            // =================================
            else if ("IF".equals(op)) {

                targetCode.add(
                        "JNZ " +
                        arg1 +
                        ", " +
                        result
                );
            }

            // =================================
            // IF FALSE
            // =================================
            else if ("IFFALSE".equals(op)) {

                targetCode.add(
                        "JZ " +
                        arg1 +
                        ", " +
                        result
                );
            }

            // =================================
            // GOTO
            // =================================
            else if ("GOTO".equals(op)) {

                targetCode.add(
                        "JMP " +
                        result
                );
            }

            // =================================
            // LABEL
            // =================================
            else if ("LABEL".equals(op)) {

                targetCode.add(
                        result + ":"
                );
            }

            // =================================
            // PRINT
            // =================================
            else if ("PRINT".equals(op)) {

                targetCode.add(
                        "PRINT " +
                        arg1
                );
            }

            // =================================
            // READ
            // =================================
            else if ("READ".equals(op)) {

                targetCode.add(
                        "READ " +
                        result
                );
            }

            // =================================
            // RETURN
            // =================================
            else if ("RETURN".equals(op)) {

                targetCode.add(
                        "RETURN " +
                        arg1
                );
            }
        }

        return targetCode;
    }

    // =========================================
    // HANDLE ARITHMETIC OPERATIONS
    // =========================================
    private void handleArithmetic(
            List<String> code,
            String instruction,
            String arg1,
            String arg2,
            String result
    ) {

        String register =
                newRegister();

        // MOV R1, a
        code.add(
                "MOV " +
                register +
                ", " +
                arg1
        );

        // ADD/SUB/MUL/DIV
        code.add(
                instruction +
                " " +
                register +
                ", " +
                arg2
        );

        // MOV t1, R1
        code.add(
                "MOV " +
                result +
                ", " +
                register
        );
    }
}