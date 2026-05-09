package com.rangers.main.parser;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Token;
import com.rangers.main.model.TokenType;

@Component
public class Parser {

    // =========================================
    // TOKENS
    // =========================================
    private List<Token> tokens;

    private int pos;

    private Token currentToken;

    // =========================================
    // ENTRY POINT
    // =========================================
    public ASTNode parse(List<Token> tokens) {

        if (tokens == null || tokens.isEmpty()) {

            throw new RuntimeException(
                    "Syntax Error: Empty input"
            );
        }

        this.tokens = tokens;

        this.pos = 0;

        this.currentToken = tokens.get(0);

        ASTNode root =
                parseProgram();

        if (currentToken.getType() != TokenType.EOF) {

            throw new RuntimeException(
                    "Syntax Error: Unexpected tokens after program end"
            );
        }

        return root;
    }

    // =========================================
    // ADVANCE TOKEN
    // =========================================
    private void advance() {

        pos++;

        if (pos < tokens.size()) {

            currentToken =
                    tokens.get(pos);
        }
    }

    // =========================================
    // EAT TOKEN
    // =========================================
    private void eat(TokenType type) {

        if (currentToken.getType() == type) {

            advance();
        }
        else {

            throw new RuntimeException(

                    "Syntax Error: Expected "
                    +
                    type
                    +
                    " but found "
                    +
                    currentToken.getType()
                    +
                    " at position "
                    +
                    currentToken.getPosition()
            );
        }
    }

    // =========================================
    // PROGRAM
    // =========================================
    private ASTNode parseProgram() {

        // program keyword
        if (currentToken.getType() != TokenType.PROGRAM) {

            throw new RuntimeException(
                    "Syntax Error: Program must start with 'program'"
            );
        }

        eat(TokenType.PROGRAM);

        // program name
        if (currentToken.getType() != TokenType.IDENTIFIER) {

            throw new RuntimeException(
                    "Syntax Error: Expected program name"
            );
        }

        String programName =
                currentToken.getValue();

        ASTNode root =
                new ASTNode(
                        "PROGRAM",
                        programName
                );

        eat(TokenType.IDENTIFIER);

        // {
        eat(TokenType.LBRACE);

        // statements
        root.addChild(
                parseStatementList()
        );

        // }
        eat(TokenType.RBRACE);

        return root;
    }

    // =========================================
    // STATEMENT LIST
    // =========================================
    private ASTNode parseStatementList() {

        ASTNode root =
                new ASTNode("STATEMENTS");

        while (
                currentToken.getType() != TokenType.EOF
                &&
                currentToken.getType() != TokenType.RBRACE
        ) {

            root.addChild(
                    parseStatement()
            );
        }

        return root;
    }

    // =========================================
    // SINGLE STATEMENT
    // =========================================
    private ASTNode parseStatement() {

        switch (currentToken.getType()) {

            case IDENTIFIER:
                return parseAssignment();

            case IF:
                return parseIf();

            case WHILE:
                return parseWhile();

            case PRINT:
                return parsePrint();

            case INT:
            case STRING:
            case BOOLEAN:
                return parseDeclaration();

            default:

                throw new RuntimeException(

                        "Syntax Error: Invalid statement near '"
                        +
                        currentToken.getValue()
                        +
                        "' at position "
                        +
                        currentToken.getPosition()
                );
        }
    }

    // =========================================
    // DECLARATION
    // int a = 10;
    // string s = "hello";
    // =========================================
    private ASTNode parseDeclaration() {

        Token datatype =
                currentToken;

        advance();

        ASTNode node =
                new ASTNode(
                        "DECLARATION",
                        datatype.getValue()
                );

        // variable
        node.addChild(
                new ASTNode(
                        "IDENTIFIER",
                        currentToken.getValue()
                )
        );

        eat(TokenType.IDENTIFIER);

        // optional assignment
        if (currentToken.getType() == TokenType.ASSIGN) {

            eat(TokenType.ASSIGN);

            node.addChild(
                    parseExpression()
            );
        }

        eat(TokenType.SEMICOLON);

        return node;
    }

    // =========================================
    // ASSIGNMENT
    // =========================================
    private ASTNode parseAssignment() {

        ASTNode node =
                new ASTNode("ASSIGN");

        // variable
        node.addChild(
                new ASTNode(
                        "IDENTIFIER",
                        currentToken.getValue()
                )
        );

        eat(TokenType.IDENTIFIER);

        eat(TokenType.ASSIGN);

        // expression
        node.addChild(
                parseExpression()
        );

        eat(TokenType.SEMICOLON);

        return node;
    }

    // =========================================
    // IF
    // =========================================
    private ASTNode parseIf() {

        ASTNode node =
                new ASTNode("IF");

        eat(TokenType.IF);

        eat(TokenType.LPAREN);

        node.addChild(
                parseCondition()
        );

        eat(TokenType.RPAREN);

        eat(TokenType.LBRACE);

        node.addChild(
                parseStatementList()
        );

        eat(TokenType.RBRACE);

        // else
        if (currentToken.getType() == TokenType.ELSE) {

            eat(TokenType.ELSE);

            eat(TokenType.LBRACE);

            node.addChild(
                    parseStatementList()
            );

            eat(TokenType.RBRACE);
        }

        return node;
    }

    // =========================================
    // WHILE
    // =========================================
    private ASTNode parseWhile() {

        ASTNode node =
                new ASTNode("WHILE");

        eat(TokenType.WHILE);

        eat(TokenType.LPAREN);

        node.addChild(
                parseCondition()
        );

        eat(TokenType.RPAREN);

        eat(TokenType.LBRACE);

        node.addChild(
                parseStatementList()
        );

        eat(TokenType.RBRACE);

        return node;
    }

    // =========================================
    // PRINT
    // =========================================
    private ASTNode parsePrint() {

        eat(TokenType.PRINT);

        eat(TokenType.LPAREN);

        ASTNode node =
                new ASTNode("PRINT");

        node.addChild(
                parseExpression()
        );

        eat(TokenType.RPAREN);

        eat(TokenType.SEMICOLON);

        return node;
    }

    // =========================================
    // CONDITION
    // a < b
    // =========================================
    private ASTNode parseCondition() {

        ASTNode left =
                parseExpression();

        Token op =
                currentToken;

        if (
                op.getType() != TokenType.LT
                &&
                op.getType() != TokenType.GT
                &&
                op.getType() != TokenType.LTE
                &&
                op.getType() != TokenType.GTE
                &&
                op.getType() != TokenType.EQ
                &&
                op.getType() != TokenType.NEQ
        ) {

            throw new RuntimeException(

                    "Syntax Error: Expected relational operator"
            );
        }

        advance();

        ASTNode conditionNode =
                new ASTNode(
                        "CONDITION",
                        op.getValue()
                );

        // LEFT
        conditionNode.addChild(left);

        // RIGHT
        conditionNode.addChild(
                parseExpression()
        );

        return conditionNode;
    }

    // =========================================
    // EXPRESSION
    // + -
    // =========================================
    private ASTNode parseExpression() {

        ASTNode node =
                parseTerm();

        while (

                currentToken.getType() == TokenType.PLUS
                ||
                currentToken.getType() == TokenType.MINUS
        ) {

            Token op =
                    currentToken;

            advance();

            ASTNode newNode =
                    new ASTNode(
                            "EXPR",
                            op.getValue()
                    );

            newNode.addChild(node);

            newNode.addChild(
                    parseTerm()
            );

            node = newNode;
        }

        return node;
    }

    // =========================================
    // TERM
    // * /
    // =========================================
    private ASTNode parseTerm() {

        ASTNode node =
                parseFactor();

        while (

                currentToken.getType() == TokenType.MUL
                ||
                currentToken.getType() == TokenType.DIV
        ) {

            Token op =
                    currentToken;

            advance();

            ASTNode newNode =
                    new ASTNode(
                            "TERM",
                            op.getValue()
                    );

            newNode.addChild(node);

            newNode.addChild(
                    parseFactor()
            );

            node = newNode;
        }

        return node;
    }

    // =========================================
    // FACTOR
    // =========================================
    private ASTNode parseFactor() {

        Token token =
                currentToken;

        // ================= NUMBER
        if (token.getType() == TokenType.NUMBER) {

            advance();

            return new ASTNode(
                    "NUMBER",
                    token.getValue()
            );
        }

        // ================= STRING
        if (token.getType() == TokenType.STRING_LITERAL) {

            advance();

            return new ASTNode(
                    "STRING",
                    token.getValue()
            );
        }

        // ================= TRUE
        if (token.getType() == TokenType.TRUE) {

            advance();

            return new ASTNode(
                    "BOOLEAN",
                    "true"
            );
        }

        // ================= FALSE
        if (token.getType() == TokenType.FALSE) {

            advance();

            return new ASTNode(
                    "BOOLEAN",
                    "false"
            );
        }

        // ================= IDENTIFIER
        if (token.getType() == TokenType.IDENTIFIER) {

            advance();

            return new ASTNode(
                    "IDENTIFIER",
                    token.getValue()
            );
        }

        // ================= (
        if (token.getType() == TokenType.LPAREN) {

            eat(TokenType.LPAREN);

            ASTNode node =
                    parseExpression();

            eat(TokenType.RPAREN);

            return node;
        }

        throw new RuntimeException(

                "Syntax Error: Invalid factor near '"
                +
                token.getValue()
                +
                "' at position "
                +
                token.getPosition()
        );
    }
}