package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TACGenerator {

    // =========================================
    // TAC STORAGE
    // =========================================
    private List<TAC> code;

    // =========================================
    // TEMP & LABEL COUNTERS
    // =========================================
    private int tempCount;

    private int labelCount;

    // =========================================
    // MAIN GENERATION METHOD
    // =========================================
    public List<TAC> generate(ASTNode root) {

        code = new ArrayList<>();

        tempCount = 0;
        labelCount = 0;

        traverse(root);

        return code;
    }

    // =========================================
    // GENERATE TEMP VARIABLE
    // =========================================
    private String newTemp() {

        return "t" + (++tempCount);
    }

    // =========================================
    // GENERATE LABEL
    // =========================================
    private String newLabel() {

        return "L" + (++labelCount);
    }

    // =========================================
    // AST TRAVERSAL
    // =========================================
    private void traverse(ASTNode node) {

        if (node == null) {
            return;
        }

        switch (node.getType()) {

            // =================================
            // ROOT NODES
            // =================================
            case "PROGRAM":
            case "STATEMENTS":

                for (ASTNode child : node.getChildren()) {
                    traverse(child);
                }

                break;

            // =================================
            // DECLARATION
            // =================================
            case "DECLARATION":

                handleDeclaration(node);

                break;

            // =================================
            // ASSIGNMENT
            // =================================
            case "ASSIGN":

                handleAssign(node);

                break;

            // =================================
            // IF STATEMENT
            // =================================
            case "IF":

                handleIf(node);

                break;

            // =================================
            // WHILE LOOP
            // =================================
            case "WHILE":

                handleWhile(node);

                break;

            // =================================
            // PRINT STATEMENT
            // =================================
            case "PRINT":

                handlePrint(node);

                break;

            // =================================
            // DEFAULT RECURSION
            // =================================
            default:

                for (ASTNode child : node.getChildren()) {
                    traverse(child);
                }
        }
    }

    // =========================================
    // HANDLE DECLARATION
    // int a = 10;
    // =========================================
    private void handleDeclaration(ASTNode node) {

        // no initialization
        if (node.getChildren().size() < 2) {
            return;
        }

        String variableName =
                node.getChildren().get(0).getValue();

        ASTNode expressionNode =
                node.getChildren().get(1);

        String expressionResult =
                evaluate(expressionNode);

        // a = t1
        code.add(
                new TAC(
                        "=",
                        expressionResult,
                        "-",
                        variableName
                )
        );
    }

    // =========================================
    // HANDLE ASSIGNMENT
    // a = b + c
    // =========================================
    private void handleAssign(ASTNode node) {

        String variableName =
                node.getChildren().get(0).getValue();

        ASTNode expressionNode =
                node.getChildren().get(1);

        String expressionResult =
                evaluate(expressionNode);

        code.add(
                new TAC(
                        "=",
                        expressionResult,
                        "-",
                        variableName
                )
        );
    }

    // =========================================
    // HANDLE PRINT
    // print(a)
    // =========================================
    private void handlePrint(ASTNode node) {

        if (node.getChildren().isEmpty()) {
            return;
        }

        String value =
                evaluate(node.getChildren().get(0));

        code.add(
                new TAC(
                        "PRINT",
                        value,
                        "-",
                        "-"
                )
        );
    }

    // =========================================
    // HANDLE IF
    // =========================================
    private void handleIf(ASTNode node) {

        // labels
        String trueLabel = newLabel();

        String falseLabel = newLabel();

        String endLabel = newLabel();

        // =====================================
        // CONDITION
        // =====================================
        ASTNode conditionNode =
                node.getChildren().get(0);

        String conditionResult =
                evaluate(conditionNode);

        // IF condition TRUE GOTO trueLabel
        code.add(
                new TAC(
                        "IF",
                        conditionResult,
                        "-",
                        trueLabel
                )
        );

        // ELSE jump
        code.add(
                new TAC(
                        "GOTO",
                        "-",
                        "-",
                        falseLabel
                )
        );

        // =====================================
        // TRUE BLOCK
        // =====================================
        code.add(
                new TAC(
                        "LABEL",
                        "-",
                        "-",
                        trueLabel
                )
        );

        traverse(node.getChildren().get(1));

        // jump to end
        code.add(
                new TAC(
                        "GOTO",
                        "-",
                        "-",
                        endLabel
                )
        );

        // =====================================
        // FALSE BLOCK
        // =====================================
        code.add(
                new TAC(
                        "LABEL",
                        "-",
                        "-",
                        falseLabel
                )
        );

        // else block
        if (node.getChildren().size() > 2) {

            traverse(node.getChildren().get(2));
        }

        // =====================================
        // END LABEL
        // =====================================
        code.add(
                new TAC(
                        "LABEL",
                        "-",
                        "-",
                        endLabel
                )
        );
    }

    // =========================================
    // HANDLE WHILE LOOP
    // =========================================
    private void handleWhile(ASTNode node) {

        String startLabel = newLabel();

        String endLabel = newLabel();

        // =====================================
        // LOOP START
        // =====================================
        code.add(
                new TAC(
                        "LABEL",
                        "-",
                        "-",
                        startLabel
                )
        );

        // =====================================
        // CONDITION
        // =====================================
        ASTNode conditionNode =
                node.getChildren().get(0);

        String conditionResult =
                evaluate(conditionNode);

        // IF FALSE -> EXIT LOOP
        code.add(
                new TAC(
                        "IFFALSE",
                        conditionResult,
                        "-",
                        endLabel
                )
        );

        // =====================================
        // LOOP BODY
        // =====================================
        traverse(node.getChildren().get(1));

        // =====================================
        // JUMP TO START
        // =====================================
        code.add(
                new TAC(
                        "GOTO",
                        "-",
                        "-",
                        startLabel
                )
        );

        // =====================================
        // LOOP END
        // =====================================
        code.add(
                new TAC(
                        "LABEL",
                        "-",
                        "-",
                        endLabel
                )
        );
    }

    // =========================================
    // EXPRESSION EVALUATION
    // =========================================
    private String evaluate(ASTNode node) {

        if (node == null) {
            return "";
        }

        // =====================================
        // LEAF NODE
        // =====================================
        if (node.getChildren().isEmpty()) {

            return node.getValue();
        }

        // =====================================
        // UNARY OPERATION
        // =====================================
        if (node.getChildren().size() == 1) {

            String operand =
                    evaluate(node.getChildren().get(0));

            String temp = newTemp();

            code.add(
                    new TAC(
                            node.getValue(),
                            operand,
                            "-",
                            temp
                    )
            );

            return temp;
        }

        // =====================================
        // BINARY OPERATION
        // =====================================
        if (node.getChildren().size() >= 2) {

            String left =
                    evaluate(node.getChildren().get(0));

            String right =
                    evaluate(node.getChildren().get(1));

            String temp = newTemp();

            code.add(
                    new TAC(
                            node.getValue(),
                            left,
                            right,
                            temp
                    )
            );

            return temp;
        }

        return node.getValue();
    }
}