package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class TACGenerator {

    private List<TAC> code;
    private int tempCount;
    private int labelCount;

    public List<TAC> generate(ASTNode root) {
        code = new ArrayList<>();
        tempCount = 0;
        labelCount = 0;
        traverse(root);
        return code;
    }

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private String newLabel() {
        return "L" + (++labelCount);
    }

    private void traverse(ASTNode node) {
        if (node == null) return;

        switch (node.getType()) {

            case "DECLARATION":
                handleDeclaration(node);
                break;

            case "ASSIGN":
                handleAssign(node);
                break;

            case "IF":
                handleIf(node);
                return;

            case "WHILE":
                handleWhile(node);
                return;

            default:
                for (ASTNode child : node.getChildren()) {
                    traverse(child);
                }
        }
    }

    // ================= DECLARATION =================
    private void handleDeclaration(ASTNode node) {

        if (node.getChildren().size() < 2) return;

        String var = node.getChildren().get(0).getValue();
        ASTNode expr = node.getChildren().get(1);

        String right = eval(expr);

        code.add(new TAC("=", right, "-", var));
    }

    // ================= ASSIGN =================
    private void handleAssign(ASTNode node) {

        String left = node.getChildren().get(0).getValue();
        ASTNode expr = node.getChildren().get(1);

        String right = eval(expr);

        code.add(new TAC("=", right, "-", left));
    }

    // ================= IF (FIXED) =================
    private void handleIf(ASTNode node) {

        String L1 = newLabel();
        String L2 = newLabel();
        String L3 = newLabel();

        ASTNode condNode = node.getChildren().get(0);

        String left = eval(condNode.getChildren().get(0));
        String op = condNode.getChildren().get(1).getValue();
        String right = eval(condNode.getChildren().get(2));

        String temp = newTemp();

        // 🔥 CONDITION EVALUATION (IMPORTANT FIX)
        code.add(new TAC(op, left, right, temp));

        // IF temp == TRUE goto L1
        code.add(new TAC("IF", temp, "-", L1));
        code.add(new TAC("GOTO", "-", "-", L2));

        code.add(new TAC("LABEL", "-", "-", L1));
        traverse(node.getChildren().get(1));

        code.add(new TAC("GOTO", "-", "-", L3));

        code.add(new TAC("LABEL", "-", "-", L2));

        if (node.getChildren().size() > 2) {
            traverse(node.getChildren().get(2));
        }

        code.add(new TAC("LABEL", "-", "-", L3));
    }

    // ================= WHILE (FIXED) =================
    private void handleWhile(ASTNode node) {

        String start = newLabel();
        String end = newLabel();

        code.add(new TAC("LABEL", "-", "-", start));

        ASTNode condNode = node.getChildren().get(0);

        String left = eval(condNode.getChildren().get(0));
        String op = condNode.getChildren().get(1).getValue();
        String right = eval(condNode.getChildren().get(2));

        String temp = newTemp();

        // 🔥 CONDITION EVALUATION
        code.add(new TAC(op, left, right, temp));

        // if FALSE → exit loop
        code.add(new TAC("IF", temp, "-", end));

        traverse(node.getChildren().get(1));

        code.add(new TAC("GOTO", "-", "-", start));
        code.add(new TAC("LABEL", "-", "-", end));
    }

    // ================= EXPRESSION =================
    private String eval(ASTNode node) {

        if (node == null) return "";

        if (node.getChildren().isEmpty()) {
            return node.getValue();
        }

        if (node.getChildren().size() == 2) {

            String left = eval(node.getChildren().get(0));
            String right = eval(node.getChildren().get(1));

            String temp = newTemp();

            code.add(new TAC(node.getValue(), left, right, temp));

            return temp;
        }

        return node.getValue();
    }
}