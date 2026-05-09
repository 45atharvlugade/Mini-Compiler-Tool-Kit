package com.rangers.main.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private Set<String> constantTable = new HashSet<>();
    private Map<String, String> stringTable = new HashMap<>();
    private List<String> tempTable = new ArrayList<>();

    private int errorCount = 0;
    private List<String> errorTable = new ArrayList<>();

    // ===== MAIN METHOD =====
    public String analyze(ASTNode root) {

        // =========================================
        // RESET EVERYTHING
        // =========================================
        expressions.clear();
        typeTable.clear();

        expressionSet.clear();
        checked.clear();
        errorSet.clear();

        constantTable.clear();
        stringTable.clear();
        tempTable.clear();

        errorTable.clear();
        errorCount = 0;

        // optional fresh symbol table
        symbolTable.getTable().clear();

        // =========================================
        // START SEMANTIC ANALYSIS
        // =========================================
        visit(root);

        StringBuilder sb = new StringBuilder();

        sb.append("====================================\n");
        sb.append("     PHASE 3 : SEMANTIC ANALYSIS\n");
        sb.append("====================================\n\n");

        // =========================================
        // ERROR TABLE
        // =========================================
        sb.append("[ ERROR TABLE ]\n");

        if (errorTable.isEmpty()) {

            sb.append("No Semantic Errors\n");

        } else {

            for (String error : errorTable) {
                sb.append(error).append("\n");
            }

            sb.append("\nTotal Errors : ")
              .append(errorCount)
              .append("\n");
        }

        // =========================================
        // SYMBOL TABLE
        // =========================================
        sb.append("\n[ SYMBOL TABLE ]\n");

        if (symbolTable.getTable().isEmpty()) {

            sb.append("Empty\n");

        } else {

            symbolTable.getTable().forEach((key, value) -> {

                sb.append(key)
                  .append(" → ")
                  .append(value)
                  .append("\n");
            });
        }

        // =========================================
        // TYPE TABLE
        // =========================================
        sb.append("\n[ TYPE TABLE ]\n");

        if (typeTable.isEmpty()) {

            sb.append("Empty\n");

        } else {

            for (String type : typeTable) {
                sb.append(type).append("\n");
            }
        }

        // =========================================
        // EXPRESSION TABLE
        // =========================================
        sb.append("\n[ EXPRESSION TABLE ]\n");

        if (expressions.isEmpty()) {

            sb.append("Empty\n");

        } else {

            for (String expr : expressions) {
                sb.append(expr).append("\n");
            }
        }

        // =========================================
        // CONSTANT TABLE
        // =========================================
        sb.append("\n[ CONSTANT TABLE ]\n");

        if (constantTable.isEmpty()) {

            sb.append("Empty\n");

        } else {

            for (String constant : constantTable) {
                sb.append(constant).append("\n");
            }
        }

        // =========================================
        // STRING LITERAL TABLE
        // =========================================
        sb.append("\n[ STRING LITERAL TABLE ]\n");

        if (stringTable.isEmpty()) {

            sb.append("Empty\n");

        } else {

            stringTable.forEach((key, value) -> {

                sb.append(key)
                  .append(" → ")
                  .append(value)
                  .append("\n");
            });
        }

        // =========================================
        // TEMPORARY VARIABLE TABLE
        // =========================================
        sb.append("\n[ TEMPORARY VARIABLE TABLE ]\n");

        if (tempTable.isEmpty()) {

            sb.append("Empty\n");

        } else {

            for (String temp : tempTable) {
                sb.append(temp).append("\n");
            }
        }

        sb.append("\n====================================\n");
        sb.append("  SEMANTIC ANALYSIS COMPLETED\n");
        sb.append("====================================\n");

        return sb.toString();
    }

    // ===== ERROR HANDLER (IMPORTANT) =====
    private void addError(String message) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (message == null || message.isBlank()) {
            return;
        }

        // =========================================
        // AVOID DUPLICATE ERRORS
        // =========================================
        if (errorSet.contains(message)) {
            return;
        }

        errorSet.add(message);

        // =========================================
        // ERROR COUNT
        // =========================================
        errorCount++;

        // =========================================
        // FORMAT ERROR MESSAGE
        // =========================================
        String formattedError =
                String.format(
                        "ERROR %d : %s",
                        errorCount,
                        message
                );

        // =========================================
        // STORE ERROR
        // =========================================
        errorTable.add(formattedError);
    }

    // ===== AST VISITOR =====
    private void visit(ASTNode node) {

        // =========================================
        // NULL SAFETY
        // =========================================
        if (node == null) {
            return;
        }

        switch (node.getType()) {

            // =========================================
            // ROOT / BLOCK NODES
            // =========================================
            case "PROGRAM":
            case "STATEMENTS":

                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }

                break;

            // =========================================
            // DECLARATION
            // =========================================
            case "DECLARATION":

                handleDeclaration(node);

                break;

            // =========================================
            // ASSIGNMENT
            // =========================================
            case "ASSIGN":

                // Visit RHS first
                if (node.getChildren().size() > 1) {
                    visit(node.getChildren().get(1));
                }

                handleAssign(node);

                break;

            // =========================================
            // IF STATEMENT
            // =========================================
            case "IF":

                handleIf(node);

                break;

            // =========================================
            // WHILE LOOP
            // =========================================
            case "WHILE":

                handleWhile(node);

                break;

            // =========================================
            // IDENTIFIER
            // =========================================
            case "IDENTIFIER":

                checkVariable(node.getValue());

                break;

            // =========================================
            // NUMBER CONSTANT
            // =========================================
            case "NUMBER":

                constantTable.add(node.getValue());

                break;

            // =========================================
            // STRING CONSTANT
            // =========================================
            case "STRING":

                constantTable.add(node.getValue());

                String label = "S" + (stringTable.size() + 1);

                stringTable.put(label, node.getValue());

                break;

            // =========================================
            // DEFAULT RECURSIVE VISIT
            // =========================================
            default:

                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }

                break;
        }
    }

    // ===== DECLARATION =====
    private void handleDeclaration(ASTNode node) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (node == null || node.getChildren().isEmpty()) {
            return;
        }

        // =========================================
        // GET VARIABLE INFO
        // =========================================
        String dataType = node.getValue(); // int / string

        String varName =
                node.getChildren()
                    .get(0)
                    .getValue();

        // =========================================
        // DUPLICATE DECLARATION CHECK
        // =========================================
        if (symbolTable.isDeclared(varName)) {

            addError(
                    "Variable '" +
                    varName +
                    "' already declared"
            );

            return;
        }

        // =========================================
        // ADD TO SYMBOL TABLE
        // =========================================
        symbolTable.declare(varName, dataType);

        // =========================================
        // ADD TO TYPE TABLE
        // =========================================
        typeTable.add(
                varName +
                " → " +
                dataType
        );

        // =========================================
        // HANDLE INITIAL VALUE
        // =========================================
        if (node.getChildren().size() > 1) {

            ASTNode exprNode = node.getChildren().get(1);

            // Validate identifiers inside expression
            validateExpression(exprNode);

            // =====================================
            // TYPE CHECKING
            // =====================================
            String exprType = inferType(exprNode);

            if (!dataType.equals(exprType)
                    && !exprType.equals("unknown")) {

                addError(
                        "Type mismatch : cannot assign '" +
                        exprType +
                        "' to '" +
                        dataType +
                        "'"
                );
            }

            // =====================================
            // BUILD EXPRESSION
            // =====================================
            String expr = buildExpression(exprNode);

            String fullExpression =
                    varName +
                    " = " +
                    expr;

            // =====================================
            // STORE EXPRESSION
            // =====================================
            if (!expressionSet.contains(fullExpression)) {

                expressions.add(fullExpression);

                expressionSet.add(fullExpression);
            }

            // =====================================
            // STORE CONSTANTS
            // =====================================
            if (exprNode.getType().equals("NUMBER") ||
                exprNode.getType().equals("STRING")) {

                constantTable.add(exprNode.getValue());
            }
        }
    }

    // ===== ASSIGNMENT =====
    private void handleAssign(ASTNode node) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (node == null || node.getChildren().size() < 2) {
            return;
        }

        // =========================================
        // GET VARIABLE NAME
        // =========================================
        String varName =
                node.getChildren()
                    .get(0)
                    .getValue();

        // =========================================
        // VARIABLE MUST BE DECLARED
        // =========================================
        if (!symbolTable.isDeclared(varName)) {

            addError(
                    "Variable '" +
                    varName +
                    "' not declared"
            );

            return;
        }

        // =========================================
        // GET RHS EXPRESSION
        // =========================================
        ASTNode exprNode =
                node.getChildren().get(1);

        // =========================================
        // VALIDATE IDENTIFIERS INSIDE EXPRESSION
        // =========================================
        validateExpression(exprNode);

        // =========================================
        // TYPE CHECKING
        // =========================================
        String variableType =
                symbolTable.lookup(varName);

        String expressionType =
                inferType(exprNode);

        if (!variableType.equals(expressionType)
                && !expressionType.equals("unknown")) {

            addError(
                    "Type mismatch : cannot assign '" +
                    expressionType +
                    "' to '" +
                    variableType +
                    "'"
            );
        }

        // =========================================
        // BUILD EXPRESSION
        // =========================================
        String expr =
                buildExpression(exprNode);

        String fullExpression =
                varName +
                " = " +
                expr;

        // =========================================
        // STORE EXPRESSION
        // =========================================
        if (!expressionSet.contains(fullExpression)) {

            expressions.add(fullExpression);

            expressionSet.add(fullExpression);
        }

        // =========================================
        // STORE CONSTANTS
        // =========================================
        if (exprNode.getType().equals("NUMBER") ||
            exprNode.getType().equals("STRING")) {

            constantTable.add(exprNode.getValue());
        }
    }

    // ===== VARIABLE CHECK =====
    private void checkVariable(String name) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (name == null || name.isBlank()) {
            return;
        }

        // =========================================
        // AVOID DUPLICATE CHECKS
        // =========================================
        if (checked.contains(name)) {
            return;
        }

        checked.add(name);

        // =========================================
        // VARIABLE DECLARATION CHECK
        // =========================================
        if (!symbolTable.isDeclared(name)) {

            addError(
                    "Variable '" +
                    name +
                    "' not declared"
            );
        }
    }

    // ===== EXPRESSION BUILDER =====
    private String buildExpression(ASTNode node) {

        // =========================================
        // NULL SAFETY
        // =========================================
        if (node == null) {
            return "";
        }

        // =========================================
        // LEAF NODES
        // =========================================
        switch (node.getType()) {

            case "IDENTIFIER":
            case "NUMBER":
            case "STRING":

                return node.getValue();
        }

        // =========================================
        // UNARY EXPRESSIONS
        // =========================================
        if (node.getChildren().size() == 1) {

            String childExpression =
                    buildExpression(
                            node.getChildren().get(0)
                    );

            return "(" +
                    node.getValue() +
                    childExpression +
                    ")";
        }

        // =========================================
        // BINARY EXPRESSIONS
        // =========================================
        if (node.getChildren().size() >= 2) {

            String left =
                    buildExpression(
                            node.getChildren().get(0)
                    );

            String right =
                    buildExpression(
                            node.getChildren().get(1)
                    );

            return "(" +
                    left +
                    " " +
                    node.getValue() +
                    " " +
                    right +
                    ")";
        }

        // =========================================
        // DEFAULT
        // =========================================
        return node.getValue();
    }    
    private void handleIf(ASTNode node) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (node == null || node.getChildren().size() < 2) {

            addError("Invalid IF statement");

            return;
        }

        // =========================================
        // CONDITION
        // =========================================
        ASTNode conditionNode =
                node.getChildren().get(0);

        // semantic validation
        checkCondition(conditionNode);

        // =========================================
        // IF BLOCK
        // =========================================
        ASTNode ifBlock =
                node.getChildren().get(1);

        visit(ifBlock);

        // =========================================
        // ELSE BLOCK
        // =========================================
        if (node.getChildren().size() > 2) {

            ASTNode elseBlock =
                    node.getChildren().get(2);

            visit(elseBlock);
        }

        // =========================================
        // STORE TEMP LABEL INFO (OPTIONAL)
        // =========================================
        String tempLabel =
                "IF_TEMP_" + (tempTable.size() + 1);

        tempTable.add(tempLabel);
    }
    
    private void handleWhile(ASTNode node) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (node == null || node.getChildren().size() < 2) {

            addError("Invalid WHILE statement");

            return;
        }

        // =========================================
        // CONDITION
        // =========================================
        ASTNode conditionNode =
                node.getChildren().get(0);

        // semantic validation
        checkCondition(conditionNode);

        // =========================================
        // LOOP BODY
        // =========================================
        ASTNode loopBody =
                node.getChildren().get(1);

        visit(loopBody);

        // =========================================
        // STORE LOOP TEMP INFO
        // =========================================
        String tempLabel =
                "WHILE_TEMP_" + (tempTable.size() + 1);

        tempTable.add(tempLabel);
    }
    
    private void checkCondition(ASTNode node) {

        // =========================================
        // SAFETY CHECK
        // =========================================
        if (node == null) {

            addError("Invalid condition");

            return;
        }

        // =========================================
        // VALIDATE IDENTIFIERS
        // =========================================
        validateExpression(node);

        // =========================================
        // CONDITION MUST BE BINARY
        // =========================================
        if (node.getChildren().size() < 2) {

            addError("Incomplete condition expression");

            return;
        }

        // =========================================
        // GET CONDITION TYPES
        // =========================================
        ASTNode leftNode =
                node.getChildren().get(0);

        ASTNode rightNode =
                node.getChildren().get(1);

        String leftType =
                inferType(leftNode);

        String rightType =
                inferType(rightNode);

        // =========================================
        // TYPE COMPATIBILITY CHECK
        // =========================================
        if (!leftType.equals(rightType)
                && !leftType.equals("unknown")
                && !rightType.equals("unknown")) {

            addError(
                    "Condition type mismatch : '" +
                    leftType +
                    "' and '" +
                    rightType +
                    "'"
            );
        }

        // =========================================
        // STORE CONDITION EXPRESSION
        // =========================================
        String conditionExpression =
                buildExpression(node);

        if (!expressionSet.contains(conditionExpression)) {

            expressions.add(conditionExpression);

            expressionSet.add(conditionExpression);
        }
    }
    
    
    private void validateExpression(ASTNode node) {

        // =========================================
        // NULL SAFETY
        // =========================================
        if (node == null) {
            return;
        }

        // =========================================
        // IDENTIFIER VALIDATION
        // =========================================
        if (node.getType().equals("IDENTIFIER")) {

            checkVariable(node.getValue());
        }

        // =========================================
        // CONSTANT HANDLING
        // =========================================
        else if (node.getType().equals("NUMBER")) {

            constantTable.add(node.getValue());
        }

        else if (node.getType().equals("STRING")) {

            constantTable.add(node.getValue());

            // avoid duplicate string entries
            boolean exists =
                    stringTable.containsValue(node.getValue());

            if (!exists) {

                String label =
                        "S" + (stringTable.size() + 1);

                stringTable.put(label, node.getValue());
            }
        }

        // =========================================
        // TEMP VARIABLE TRACKING
        // =========================================
        if (node.getChildren().size() >= 2) {

            String tempName =
                    "t" + (tempTable.size() + 1);

            if (!tempTable.contains(tempName)) {

                tempTable.add(tempName);
            }
        }

        // =========================================
        // RECURSIVE VALIDATION
        // =========================================
        for (ASTNode child : node.getChildren()) {

            validateExpression(child);
        }
    }
    
    private String inferType(ASTNode node) {

        // =========================================
        // NULL SAFETY
        // =========================================
        if (node == null) {
            return "unknown";
        }

        switch (node.getType()) {

            // =====================================
            // INTEGER TYPE
            // =====================================
            case "NUMBER":
                return "int";

            // =====================================
            // STRING TYPE
            // =====================================
            case "STRING":
                return "string";

            // =====================================
            // IDENTIFIER TYPE
            // =====================================
            case "IDENTIFIER":

                if (symbolTable.isDeclared(node.getValue())) {

                    return symbolTable.lookup(
                            node.getValue()
                    );
                }

                return "unknown";

            // =====================================
            // BOOLEAN / RELATIONAL EXPRESSIONS
            // =====================================
            case "LT":
            case "GT":
            case "LTE":
            case "GTE":
            case "EQ":
            case "NEQ":

                return "boolean";

            // =====================================
            // OPERATOR EXPRESSIONS
            // =====================================
            default:

                // unary expression
                if (node.getChildren().size() == 1) {

                    return inferType(
                            node.getChildren().get(0)
                    );
                }

                // binary expression
                if (node.getChildren().size() >= 2) {

                    String leftType =
                            inferType(
                                    node.getChildren().get(0)
                            );

                    String rightType =
                            inferType(
                                    node.getChildren().get(1)
                            );

                    // =================================
                    // SAME TYPES
                    // =================================
                    if (leftType.equals(rightType)) {

                        // relational operators
                        if (isRelationalOperator(
                                node.getValue())) {

                            return "boolean";
                        }

                        return leftType;
                    }

                    // =================================
                    // INVALID TYPE COMBINATION
                    // =================================
                    addError(
                            "Invalid expression between '" +
                            leftType +
                            "' and '" +
                            rightType +
                            "'"
                    );

                    return "unknown";
                }

                return "unknown";
        }
    }
    private boolean isRelationalOperator(String operator) {

        return operator.equals("<")  ||
               operator.equals(">")  ||
               operator.equals("<=") ||
               operator.equals(">=") ||
               operator.equals("==") ||
               operator.equals("!=");
    }
}