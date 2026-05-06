package com.rangers.main.lexer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.rangers.main.model.Token;
import com.rangers.main.model.TokenType;

@Component
public class Lexer {

	private static final Set<String> KEYWORDS = Set.of(
	        "if", "else", "while", "int", "print", "ranger"
	);
	

    public List<Token> tokenize(String input) {

        List<Token> tokens = new ArrayList<>();

        int pos = 0;
        char currentChar = input.length() > 0 ? input.charAt(0) : '\0';

        while (currentChar != '\0') {

            // Skip whitespace
            if (Character.isWhitespace(currentChar)) {
                pos++;
                currentChar = pos < input.length() ? input.charAt(pos) : '\0';
                continue;
            }

            // Identifier / Keyword
            if (Character.isLetter(currentChar) || currentChar == '_') {
                int start = pos;
                StringBuilder sb = new StringBuilder();

                while (pos < input.length() &&
                        (Character.isLetterOrDigit(input.charAt(pos)) || input.charAt(pos) == '_')) {
                    sb.append(input.charAt(pos));
                    pos++;
                }

                String word = sb.toString();

                TokenType type;

                if (KEYWORDS.contains(word)) {
                    type = TokenType.valueOf(word.toUpperCase());
                } else {
                    type = TokenType.IDENTIFIER;
                }

                tokens.add(new Token(type, word, start));

                currentChar = pos < input.length() ? input.charAt(pos) : '\0';
                continue;
            }
            
            if (currentChar == '"') {

                int start = pos;
                pos++; // skip opening "

                StringBuilder sb = new StringBuilder();

                while (pos < input.length() && input.charAt(pos) != '"') {
                    sb.append(input.charAt(pos));
                    pos++;
                }

                if (pos >= input.length()) {
                    throw new RuntimeException("Unterminated string at position " + start);
                }

                pos++; // skip closing "

                tokens.add(new Token(TokenType.STRING, sb.toString(), start));

                currentChar = pos < input.length() ? input.charAt(pos) : '\0';
                continue;
            }

            // Number
            if (Character.isDigit(currentChar)) {
                int start = pos;
                StringBuilder sb = new StringBuilder();

                while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
                    sb.append(input.charAt(pos));
                    pos++;
                }

                tokens.add(new Token(TokenType.NUMBER, sb.toString(), start));

                currentChar = pos < input.length() ? input.charAt(pos) : '\0';
                continue;
            }

            int start = pos;

            // Operators
            switch (currentChar) {

                case '=':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(TokenType.EQ, "==", start));
                        pos += 2;
                    } else {
                        tokens.add(new Token(TokenType.ASSIGN, "=", start));
                        pos++;
                    }
                    break;

                case '!':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(TokenType.NEQ, "!=", start));
                        pos += 2;
                    } else {
                        throw new RuntimeException("Invalid character '!' at position " + start);
                    }
                    break;

                case '+':
                    tokens.add(new Token(TokenType.PLUS, "+", start));
                    pos++;
                    break;

                case '-':
                    tokens.add(new Token(TokenType.MINUS, "-", start));
                    pos++;
                    break;

                case '*':
                    tokens.add(new Token(TokenType.MUL, "*", start));
                    pos++;
                    break;

                case '/':
                    tokens.add(new Token(TokenType.DIV, "/", start));
                    pos++;
                    break;

                case '>':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(TokenType.GTE, ">=", start));
                        pos += 2;
                    } else {
                        tokens.add(new Token(TokenType.GT, ">", start));
                        pos++;
                    }
                    break;

                case '<':
                    if (pos + 1 < input.length() && input.charAt(pos + 1) == '=') {
                        tokens.add(new Token(TokenType.LTE, "<=", start));
                        pos += 2;
                    } else {
                        tokens.add(new Token(TokenType.LT, "<", start));
                        pos++;
                    }
                    break;

                case ';':
                    tokens.add(new Token(TokenType.SEMICOLON, ";", start));
                    pos++;
                    break;

                case '{':
                    tokens.add(new Token(TokenType.LBRACE, "{", start));
                    pos++;
                    break;

                case '}':
                    tokens.add(new Token(TokenType.RBRACE, "}", start));
                    pos++;
                    break;

                case '(':
                    tokens.add(new Token(TokenType.LPAREN, "(", start));
                    pos++;
                    break;

                case ')':
                    tokens.add(new Token(TokenType.RPAREN, ")", start));
                    pos++;
                    break;
                    
                    

                default:
                    throw new RuntimeException(
                            "Lexical Error: Invalid character '" + currentChar + "' at position " + start
                    );
            }

            currentChar = pos < input.length() ? input.charAt(pos) : '\0';
        }

        tokens.add(new Token(TokenType.EOF, "", pos));

        

        return tokens;
    }
}