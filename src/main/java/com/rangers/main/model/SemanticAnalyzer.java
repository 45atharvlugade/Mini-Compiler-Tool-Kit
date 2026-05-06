package com.rangers.main.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SemanticAnalyzer {

    @Autowired
    private SymbolTable symbolTable;

    // ===== Tables =====
    private List<String> expressions = new ArrayList<>();
    private List<String> typeTable = new ArrayList<>();
    private Set<String> expressionSet = new HashSet<>();
    private Set<String> checked = new HashSet<>();
    private Set<String> errorSet = new HashSet<>();

    private int errorCount = 0;
    private List<String> errorTable = new ArrayList<>();

    // ===== MAIN METHOD =====
    public String analyze(ASTNode root) {

        // 🔥 RESET EVERYTHING (MANDATORY)
        expressions.clear();
        typeTable.clear();
        expressionSet.clear();
        checked.clear();
        errorSet.clear();
        errorTable.clear();
        errorCount = 0;

        // 🔥 optional (only if you want fresh symbol table every time)
        symbolTable.getTable().clear();

        // now start analysis
        visit(root);

        StringBuilder sb = new StringBuilder();

        sb.append("===== PHASE 3: SEMANTIC ANALYSIS =====\n\n");

        // ===== ERRORS =====
        sb.append("ERRORS:\n");
        if (errorTable.isEmpty()) {
            sb.append("No Semantic Errors\n");
        } else {
            for (String e : errorTable) {
                sb.append(e).append("\n");
            }
        }

        // ===== SYMBOL TABLE =====
        sb.append("\nSYMBOL TABLE:\n");
        if (symbolTable.getTable().isEmpty()) {
            sb.append("{}\n");
        } else {
            symbolTable.getTable().forEach((k, v) ->
                    sb.append(k).append(" → ").append(v).append("\n"));
        }

        // ===== TYPE TABLE =====
        sb.append("\nTYPE TABLE:\n");
        if (typeTable.isEmpty()) {
            sb.append("Empty\n");
        } else {
            for (String t : typeTable) {
                sb.append(t).append("\n");
            }
        }

        // ===== EXPRESSION TABLE =====
        sb.append("\nEXPRESSION TABLE:\n");
        if (expressions.isEmpty()) {
            sb.append("Empty\n");
        } else {
            for (String ex : expressions) {
                sb.append(ex).append("\n");
            }
        }

        return sb.toString();
    }

    // ===== ERROR HANDLER (IMPORTANT) =====
    private void addError(String msg) {

        if (errorSet.contains(msg)) return; // avoid duplicates

        errorSet.add(msg);

        errorCount++;

        errorTable.add("ERROR " + errorCount + ": " + msg);
    }

    // ===== AST VISITOR =====
    private void visit(ASTNode node) {

        if (node == null) return;

        switch (node.getType()) {

            case "PROGRAM":
            case "STATEMENTS":
                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }
                break;

            // 🔥 HANDLE IF PROPERLY
            case "IF":
                handleIf(node);
                return;

            // 🔥 HANDLE WHILE PROPERLY
            case "WHILE":
                handleWhile(node);
                return;

            // 🔥 DECLARATION
            case "DECLARATION":
                handleDeclaration(node);
                break;

            // 🔥 ASSIGNMENT (post-order)
            case "ASSIGN":
                if (node.getChildren().size() > 1) {
                    visit(node.getChildren().get(1)); // RHS first
                }
                handleAssign(node);
                break;

            // 🔥 IDENTIFIER
            case "IDENTIFIER":
                checkVariable(node.getValue());
                break;

            default:
                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }
                break;
        }
    }

    // ===== DECLARATION =====
    private void handleDeclaration(ASTNode node) {

        String varName = node.getChildren().get(0).getValue();

        // 🔥 prevent duplicate declaration processing
        if (symbolTable.isDeclared(varName)) return;

        // add to symbol table
        symbolTable.declare(varName, "int");

        // add to type table
        typeTable.add(varName + " → int");

        // 🔥 HANDLE INITIAL VALUE (VERY IMPORTANT)
        if (node.getChildren().size() > 1) {

            ASTNode exprNode = node.getChildren().get(1);

            String expr = buildExpression(exprNode);

            String fullExpr = varName + " = " + expr;

            // 🔥 avoid duplicates
            if (!expressionSet.contains(fullExpr)) {
                expressions.add(fullExpr);
                expressionSet.add(fullExpr);
            }
        }
    }

    // ===== ASSIGNMENT =====
    private void handleAssign(ASTNode node) {

        String varName = node.getChildren().get(0).getValue();

        if (!symbolTable.isDeclared(varName)) {
            addError("Variable '" + varName + "' not declared");
        }

        if (node.getChildren().size() > 1) {

            ASTNode exprNode = node.getChildren().get(1);
            String expr = buildExpression(exprNode);

            String fullExpr = varName + " = " + expr;

            // 🔥 prevent duplicates
            if (!expressionSet.contains(fullExpr)) {
                expressions.add(fullExpr);
                expressionSet.add(fullExpr);
            }
        }
    }

    // ===== VARIABLE CHECK =====
    private void checkVariable(String name) {

        if (checked.contains(name)) return;

        checked.add(name);

        if (!symbolTable.isDeclared(name)) {
            addError("Variable '" + name + "' not declared");
        }
    }

    // ===== EXPRESSION BUILDER =====
    private String buildExpression(ASTNode node) {

        if (node == null) return "";

        if (node.getType().equals("IDENTIFIER") ||
            node.getType().equals("NUMBER") ||
            node.getType().equals("STRING")) {
            return node.getValue();
        }

        if (node.getChildren().size() == 2) {

            String left = buildExpression(node.getChildren().get(0));
            String right = buildExpression(node.getChildren().get(1));

            return "(" + left + " " + node.getValue() + " " + right + ")";
        }

        return node.getValue();
    }
    
    private void handleIf(ASTNode node) {

        // condition check
        checkCondition(node.getChildren().get(0));

        // IF block
        visit(node.getChildren().get(1));

        // ELSE block (if exists)
        if (node.getChildren().size() > 2) {
            visit(node.getChildren().get(2));
        }
    }
    
    private void handleWhile(ASTNode node) {

        // condition check
        checkCondition(node.getChildren().get(0));

        // loop body
        visit(node.getChildren().get(1));
    }
    
    private void checkCondition(ASTNode node) {

        String left = node.getChildren().get(0).getValue();
        String right = node.getChildren().get(2).getValue();

        checkVariable(left);
        checkVariable(right);
    }
}