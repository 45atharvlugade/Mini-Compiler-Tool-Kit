package com.rangers.main.codegenrator;

import org.springframework.stereotype.Component;
import com.rangers.main.model.ASTNode;

@Component
public class CodeGenerator {

    private int tempCount;
    private int labelCount;
    private StringBuilder code;

    public String generate(ASTNode root) {

        tempCount = 0;
        labelCount = 0;
        code = new StringBuilder();

        generateNode(root);

        return code.toString();
    }

    private String generateNode(ASTNode node) {

        if (node == null) return "";

        switch (node.getType()) {

            case "PROGRAM":
            case "STATEMENTS":
                for (ASTNode child : node.getChildren()) {
                    generateNode(child);
                }
                break;

            // ================= DECLARATION =================
            case "DECLARATION":
                String var = node.getChildren().get(0).getValue();

                if (node.getChildren().size() > 1) {
                    String val = generateNode(node.getChildren().get(1));
                    emit(var + " = " + val);
                } else {
                    emit(var + " = 0");
                }
                break;

            // ================= ASSIGN =================
            case "ASSIGN":
                String left = node.getChildren().get(0).getValue();
                String right = generateNode(node.getChildren().get(1));

                emit(left + " = " + right);
                return left;

            // ================= IF =================
            case "IF":
                String cond = generateNode(node.getChildren().get(0));

                String L1 = newLabel();
                String L2 = newLabel();

                emit("ifFalse " + cond + " goto " + L1);

                generateNode(node.getChildren().get(1));

                emit("goto " + L2);

                emit(L1 + ":");

                if (node.getChildren().size() > 2) {
                    generateNode(node.getChildren().get(2));
                }

                emit(L2 + ":");
                break;

            // ================= WHILE =================
            case "WHILE":
                String start = newLabel();
                String end = newLabel();

                emit(start + ":");

                String condition = generateNode(node.getChildren().get(0));

                emit("ifFalse " + condition + " goto " + end);

                generateNode(node.getChildren().get(1));

                emit("goto " + start);

                emit(end + ":");
                break;

            // ================= PRINT =================
            case "PRINT":
                String value = generateNode(node.getChildren().get(0));

                if ("STRING".equals(node.getChildren().get(0).getType())) {
                    value = "\"" + node.getChildren().get(0).getValue() + "\"";
                }

                emit("PRINT " + value);
                break;

            // ================= CONDITION =================
            case "CONDITION":
                String l = generateNode(node.getChildren().get(0));
                String op = node.getChildren().get(1).getValue();
                String r = generateNode(node.getChildren().get(2));

                String temp = newTemp();

                emit(temp + " = " + l + " " + op + " " + r);

                return temp;

            // ================= EXPRESSION =================
            case "EXPR":
            case "TERM":
                String leftExp = generateNode(node.getChildren().get(0));
                String rightExp = generateNode(node.getChildren().get(1));

                String t = newTemp();

                emit(t + " = " + leftExp + " " + node.getValue() + " " + rightExp);

                return t;

            // ================= LEAF =================
            case "NUMBER":
            case "IDENTIFIER":
            case "STRING":
                return node.getValue();

            default:
                throw new RuntimeException("Unknown AST Node: " + node.getType());
        }

        return "";
    }

    // ================= HELPERS =================

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private String newLabel() {
        return "L" + (++labelCount);
    }

    private void emit(String instruction) {
        if (instruction == null || instruction.isEmpty()) return;

        code.append(instruction).append("\n");
    }
}