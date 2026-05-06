package com.rangers.main.parser;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Token;
import com.rangers.main.model.TokenType;

@Component
public class Parser {

    private List<Token> tokens;
    private int pos;
    private Token currentToken;

    // ================= ENTRY =================
    public ASTNode parse(List<Token> tokens) {

        if (tokens == null || tokens.isEmpty()) {
            throw new RuntimeException("Syntax Error: Empty input");
        }

        this.tokens = tokens;
        this.pos = 0;
        this.currentToken = tokens.get(0);

        ASTNode root = parseProgram();

        if (currentToken.getType() != TokenType.EOF) {
            throw new RuntimeException("Syntax Error: Unexpected tokens after program end");
        }

        return root;
    }

    // ================= UTIL =================

    private void advance() {
        pos++;
        if (pos < tokens.size()) {
            currentToken = tokens.get(pos);
        }
    }

    private void eat(TokenType type) {
        if (currentToken.getType() == type) {
            advance();
        } else {
            throw new RuntimeException(
                "Syntax Error: Expected " + type +
                " but found " + currentToken.getType() +
                " at position " + currentToken.getPosition()
            );
        }
    }

    // ================= STATEMENTS =================
    
    
    private ASTNode parseProgram() {

        // expect "ranger"
    	if (currentToken.getType() != TokenType.RANGER) {
    	    throw new RuntimeException("Syntax Error: Program must start with 'ranger'");
    	}
    	advance();

        // program name (Hello, Test, etc.)
        if (currentToken.getType() != TokenType.IDENTIFIER) {
            throw new RuntimeException(
                "Syntax Error: Expected program name after 'ranger' but found " +
                currentToken.getType()
            );
        }

        String programName = currentToken.getValue();
        ASTNode root = new ASTNode("PROGRAM", programName);
        eat(TokenType.IDENTIFIER);

        // opening {
        eat(TokenType.LBRACE);

        // body
        root.addChild(parseStatementList());

        // closing }
        eat(TokenType.RBRACE);

        return root;
    }

    private ASTNode parseStatementList() {

        ASTNode root = new ASTNode("STATEMENTS");

        while (currentToken.getType() != TokenType.EOF &&
               currentToken.getType() != TokenType.RBRACE) {

            root.addChild(parseStatement());
        }

        return root;
    }

    private ASTNode parseStatement() {

        switch (currentToken.getType()) {

            case IDENTIFIER:
                return parseAssignment();

            case IF:
                return parseIf();

            case WHILE:
                return parseWhile();

            case PRINT:
                return parsePrint();   // 🔥 ADD THIS

            case INT:
                return parseDeclaration(); // 🔥 ADD THIS (optional)

            default:
                throw new RuntimeException(
                    "Syntax Error: Invalid statement near '" +
                    currentToken.getValue() +
                    "' at position " + currentToken.getPosition()
                );
        }
    }

    // ================= ASSIGNMENT =================

    private ASTNode parseAssignment() {

        ASTNode node = new ASTNode("ASSIGN");

        // LEFT SIDE
        node.addChild(new ASTNode("IDENTIFIER", currentToken.getValue()));
        eat(TokenType.IDENTIFIER);

        eat(TokenType.ASSIGN);

        // 🔥 RIGHT SIDE MUST BE FULL EXPRESSION TREE
        ASTNode expr = parseExpression();

        node.addChild(expr);

        eat(TokenType.SEMICOLON);

        return node;
    }
    // ================= IF =================

    private ASTNode parseIf() {

        ASTNode node = new ASTNode("IF");

        eat(TokenType.IF);
        eat(TokenType.LPAREN);

        node.addChild(parseCondition());   // 0 → CONDITION

        eat(TokenType.RPAREN);
        eat(TokenType.LBRACE);

        node.addChild(parseStatementList()); // 1 → IF BLOCK

        eat(TokenType.RBRACE);

        // ================= ELSE (FIXED) =================
        if (currentToken.getType() == TokenType.ELSE) {

            eat(TokenType.ELSE);
            eat(TokenType.LBRACE);

            // 🔥 DIRECTLY ADD STATEMENTS (NO ELSE NODE WRAPPER)
            node.addChild(parseStatementList()); // 2 → ELSE BLOCK

            eat(TokenType.RBRACE);
        }

        return node;
    }

    // ================= WHILE =================

    private ASTNode parseWhile() {

        ASTNode node = new ASTNode("WHILE");

        eat(TokenType.WHILE);
        eat(TokenType.LPAREN);

        node.addChild(parseCondition());

        eat(TokenType.RPAREN);
        eat(TokenType.LBRACE);

        node.addChild(parseStatementList());

        eat(TokenType.RBRACE);

        return node;
    }

    // ================= CONDITION =================

    private ASTNode parseCondition() {

        ASTNode node = new ASTNode("CONDITION");

        node.addChild(parseExpression());

        Token op = currentToken;

        if (op.getType() == TokenType.GT ||
            op.getType() == TokenType.LT ||
            op.getType() == TokenType.EQ ||
            op.getType() == TokenType.NEQ ||
            op.getType() == TokenType.LTE ||
            op.getType() == TokenType.GTE) {

            node.addChild(new ASTNode("OP", op.getValue()));
            advance();

        } else {
            throw new RuntimeException(
                "Syntax Error: Expected condition operator but found '" +
                op.getValue() + "' at position " + op.getPosition()
            );
        }

        node.addChild(parseExpression());

        return node;
    }

    // ================= EXPRESSION =================

    private ASTNode parseExpression() {

        ASTNode node = parseTerm();

        while (currentToken.getType() == TokenType.PLUS ||
               currentToken.getType() == TokenType.MINUS) {

            Token op = currentToken;
            advance();

            ASTNode newNode = new ASTNode("EXPR", op.getValue());
            newNode.addChild(node);
            newNode.addChild(parseTerm());

            node = newNode;
        }

        return node;
    }

    private ASTNode parseTerm() {

        ASTNode node = parseFactor();

        while (currentToken.getType() == TokenType.MUL ||
               currentToken.getType() == TokenType.DIV) {

            Token op = currentToken;
            advance();

            ASTNode newNode = new ASTNode("TERM", op.getValue());
            newNode.addChild(node);
            newNode.addChild(parseFactor());

            node = newNode;
        }

        return node;
    }

    private ASTNode parseFactor() {

        Token token = currentToken;

        if (token.getType() == TokenType.NUMBER) {
            advance();
            return new ASTNode("NUMBER", token.getValue());
        }

        if (token.getType() == TokenType.IDENTIFIER) {
            advance();
            return new ASTNode("IDENTIFIER", token.getValue());
        }
        if (token.getType() == TokenType.STRING) {
            advance();
            return new ASTNode("STRING", token.getValue());
        }

        if (token.getType() == TokenType.LPAREN) {
            advance();
            ASTNode node = parseExpression();
            eat(TokenType.RPAREN);
            return node;
        }

        throw new RuntimeException(
            "Syntax Error: Invalid factor near '" +
            token.getValue() +
            "' at position " + token.getPosition()
        );
    }
    
    // -------------------------------------
    
    private ASTNode parsePrint() {

        eat(TokenType.PRINT);
        eat(TokenType.LPAREN);

        ASTNode node = new ASTNode("PRINT");

        if (currentToken.getType() != TokenType.RPAREN) {
            node.addChild(parseExpression());
        } else {
            node.addChild(new ASTNode("EMPTY"));
        }

        eat(TokenType.RPAREN);
        eat(TokenType.SEMICOLON);

        return node;
    }
    
    // ----------------------------------------------
    
    private ASTNode parseDeclaration() {

        eat(TokenType.INT);

        ASTNode node = new ASTNode("DECLARATION");

        node.addChild(new ASTNode("IDENTIFIER", currentToken.getValue()));
        eat(TokenType.IDENTIFIER);

        eat(TokenType.ASSIGN);

        node.addChild(parseExpression());

        eat(TokenType.SEMICOLON);

        return node;
    }
    
    private ASTNode parseExpressionOrCondition() {

        ASTNode left = parseExpression();

        if (currentToken.getType() == TokenType.LT ||
            currentToken.getType() == TokenType.GT ||
            currentToken.getType() == TokenType.EQ ||
            currentToken.getType() == TokenType.NEQ ||
            currentToken.getType() == TokenType.LTE ||
            currentToken.getType() == TokenType.GTE) {

            Token op = currentToken;
            advance();

            ASTNode node = new ASTNode("CONDITION");
            node.addChild(left);
            node.addChild(new ASTNode("OP", op.getValue()));
            node.addChild(parseExpression());

            return node;
        }

        return left;
    }
    
  
    
    
   
}