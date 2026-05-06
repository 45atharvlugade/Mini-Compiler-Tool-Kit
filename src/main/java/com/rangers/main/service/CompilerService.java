package com.rangers.main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.rangers.main.codegenrator.CodeGenerator;
import com.rangers.main.interceptor.Interpreter;
import com.rangers.main.lexer.Lexer;
import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Token;
import com.rangers.main.parser.Parser;

@Service
public class CompilerService {

    @Autowired
    private Lexer lexer;

    @Autowired
    private Parser parser;

    @Autowired
    private CodeGenerator codeGenerator;
    
    @Autowired
    private Interpreter interpreter;

    public String compile(String code) {

        try {

            if (code == null || code.trim().isEmpty()) {
                return "Compilation Error: Input program is empty";
            }

            // 1. Lexical Analysis
            List<Token> tokens = lexer.tokenize(code);

            // 2. Syntax Analysis
            ASTNode ast = parser.parse(tokens);

            // 3. Code Generation (IR)
            String ir = codeGenerator.generate(ast);

            // 4. Execute IR using Interpreter
            List<String> instructions = java.util.Arrays.asList(ir.split("\n"));

            String output = interpreter.execute(instructions);
            return output;

        } catch (Exception e) {
            e.printStackTrace();
            return "Compilation Error: " + e.getMessage();
        }
        
        
    }
}