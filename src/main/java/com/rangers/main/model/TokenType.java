package com.rangers.main.model;
public enum TokenType {

    // ================= KEYWORDS =================
    IF,
    ELSE,
    WHILE,
    INT,
    PRINT,
    RANGER,
    STRING,

    // ================= IDENTIFIERS & LITERALS =================
    IDENTIFIER,
    NUMBER,

    // ================= OPERATORS =================
    ASSIGN,

    PLUS,
    MINUS,
    MUL,
    DIV,

    // Relational operators
    LT,
    GT,
    LTE,
    GTE,
    EQ,
    NEQ,

    // ================= DELIMITERS =================
    SEMICOLON,
    LBRACE,
    RBRACE,
    LPAREN,
    RPAREN,

    // ================= SPECIAL =================
    EOF
}