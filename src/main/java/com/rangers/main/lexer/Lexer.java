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

	        // control flow
	        "if",
	        "else",
	        "while",

	        // datatypes
	        "int",
	        "string",
	        "boolean",

	        // functions / special
	        "print",
	        "program",

	        // boolean literals
	        "true",
	        "false"
	);
	

	public List<Token> tokenize(String input) {

	    List<Token> tokens = new ArrayList<>();

	    int pos = 0;

	    char currentChar =
	            input.length() > 0
	            ? input.charAt(0)
	            : '\0';

	    while (currentChar != '\0') {

	        // =====================================
	        // SKIP WHITESPACE
	        // =====================================
	        if (Character.isWhitespace(currentChar)) {

	            pos++;

	            currentChar =
	                    pos < input.length()
	                    ? input.charAt(pos)
	                    : '\0';

	            continue;
	        }

	        // =====================================
	        // SINGLE LINE COMMENTS
	        // =====================================
	        if (currentChar == '/' &&
	            pos + 1 < input.length() &&
	            input.charAt(pos + 1) == '/') {

	            while (pos < input.length() &&
	                   input.charAt(pos) != '\n') {

	                pos++;
	            }

	            currentChar =
	                    pos < input.length()
	                    ? input.charAt(pos)
	                    : '\0';

	            continue;
	        }

	        // =====================================
	        // IDENTIFIER / KEYWORD
	        // =====================================
	        if (Character.isLetter(currentChar) ||
	            currentChar == '_') {

	            int start = pos;

	            StringBuilder sb =
	                    new StringBuilder();

	            while (
	                    pos < input.length()
	                    &&
	                    (
	                        Character.isLetterOrDigit(
	                                input.charAt(pos)
	                        )
	                        ||
	                        input.charAt(pos) == '_'
	                    )
	            ) {

	                sb.append(input.charAt(pos));

	                pos++;
	            }

	            String word = sb.toString();

	            TokenType type;

	            if (KEYWORDS.contains(word)) {

	                type =
	                        TokenType.valueOf(
	                                word.toUpperCase()
	                        );
	            }
	            else {

	                type = TokenType.IDENTIFIER;
	            }

	            tokens.add(
	                    new Token(
	                            type,
	                            word,
	                            start
	                    )
	            );

	            currentChar =
	                    pos < input.length()
	                    ? input.charAt(pos)
	                    : '\0';

	            continue;
	        }

	        // =====================================
	        // STRING LITERAL
	        // =====================================
	        if (currentChar == '"') {

	            int start = pos;

	            pos++;

	            StringBuilder sb =
	                    new StringBuilder();

	            while (
	                    pos < input.length()
	                    &&
	                    input.charAt(pos) != '"'
	            ) {

	                sb.append(input.charAt(pos));

	                pos++;
	            }

	            // unterminated string
	            if (pos >= input.length()) {

	                throw new RuntimeException(
	                        "Unterminated string at position "
	                        + start
	                );
	            }

	            pos++;

	            tokens.add(
	                    new Token(
	                            TokenType.STRING_LITERAL,
	                            sb.toString(),
	                            start
	                    )
	            );

	            currentChar =
	                    pos < input.length()
	                    ? input.charAt(pos)
	                    : '\0';

	            continue;
	        }

	        // =====================================
	        // NUMBER
	        // supports:
	        // 10
	        // 10.5
	        // =====================================
	        if (Character.isDigit(currentChar)) {

	            int start = pos;

	            StringBuilder sb =
	                    new StringBuilder();

	            boolean hasDot = false;

	            while (pos < input.length()) {

	                char ch = input.charAt(pos);

	                if (Character.isDigit(ch)) {

	                    sb.append(ch);
	                }
	                else if (ch == '.' && !hasDot) {

	                    hasDot = true;

	                    sb.append(ch);
	                }
	                else {
	                    break;
	                }

	                pos++;
	            }

	            tokens.add(
	                    new Token(
	                            TokenType.NUMBER,
	                            sb.toString(),
	                            start
	                    )
	            );

	            currentChar =
	                    pos < input.length()
	                    ? input.charAt(pos)
	                    : '\0';

	            continue;
	        }

	        int start = pos;

	        // =====================================
	        // OPERATORS & SYMBOLS
	        // =====================================
	        switch (currentChar) {

	            // ================= ASSIGN / EQ
	            case '=':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '='
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.EQ,
	                                    "==",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    tokens.add(
	                            new Token(
	                                    TokenType.ASSIGN,
	                                    "=",
	                                    start
	                            )
	                    );

	                    pos++;
	                }

	                break;

	            // ================= NOT / NEQ
	            case '!':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '='
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.NEQ,
	                                    "!=",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    tokens.add(
	                            new Token(
	                                    TokenType.NOT,
	                                    "!",
	                                    start
	                            )
	                    );

	                    pos++;
	                }

	                break;

	            // ================= AND
	            case '&':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '&'
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.AND,
	                                    "&&",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    throw new RuntimeException(
	                            "Invalid '&' at position "
	                            + start
	                    );
	                }

	                break;

	            // ================= OR
	            case '|':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '|'
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.OR,
	                                    "||",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    throw new RuntimeException(
	                            "Invalid '|' at position "
	                            + start
	                    );
	                }

	                break;

	            // ================= PLUS
	            case '+':

	                tokens.add(
	                        new Token(
	                                TokenType.PLUS,
	                                "+",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= MINUS
	            case '-':

	                tokens.add(
	                        new Token(
	                                TokenType.MINUS,
	                                "-",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= MUL
	            case '*':

	                tokens.add(
	                        new Token(
	                                TokenType.MUL,
	                                "*",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= DIV
	            case '/':

	                tokens.add(
	                        new Token(
	                                TokenType.DIV,
	                                "/",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= GT / GTE
	            case '>':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '='
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.GTE,
	                                    ">=",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    tokens.add(
	                            new Token(
	                                    TokenType.GT,
	                                    ">",
	                                    start
	                            )
	                    );

	                    pos++;
	                }

	                break;

	            // ================= LT / LTE
	            case '<':

	                if (
	                        pos + 1 < input.length()
	                        &&
	                        input.charAt(pos + 1) == '='
	                ) {

	                    tokens.add(
	                            new Token(
	                                    TokenType.LTE,
	                                    "<=",
	                                    start
	                            )
	                    );

	                    pos += 2;
	                }
	                else {

	                    tokens.add(
	                            new Token(
	                                    TokenType.LT,
	                                    "<",
	                                    start
	                            )
	                    );

	                    pos++;
	                }

	                break;

	            // ================= ;
	            case ';':

	                tokens.add(
	                        new Token(
	                                TokenType.SEMICOLON,
	                                ";",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= ,
	            case ',':

	                tokens.add(
	                        new Token(
	                                TokenType.COMMA,
	                                ",",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= {
	            case '{':

	                tokens.add(
	                        new Token(
	                                TokenType.LBRACE,
	                                "{",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= }
	            case '}':

	                tokens.add(
	                        new Token(
	                                TokenType.RBRACE,
	                                "}",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= (
	            case '(':

	                tokens.add(
	                        new Token(
	                                TokenType.LPAREN,
	                                "(",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= )
	            case ')':

	                tokens.add(
	                        new Token(
	                                TokenType.RPAREN,
	                                ")",
	                                start
	                        )
	                );

	                pos++;

	                break;

	            // ================= INVALID CHARACTER
	            default:

	                throw new RuntimeException(
	                        "Lexical Error: Invalid character '"
	                        + currentChar +
	                        "' at position " +
	                        start
	                );
	        }

	        currentChar =
	                pos < input.length()
	                ? input.charAt(pos)
	                : '\0';
	    }

	    // =========================================
	    // EOF TOKEN
	    // =========================================
	    tokens.add(
	            new Token(
	                    TokenType.EOF,
	                    "",
	                    pos
	            )
	    );

	    return tokens;
	}
}