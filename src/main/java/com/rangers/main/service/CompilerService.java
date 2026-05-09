package com.rangers.main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rangers.main.interceptor.Interpreter;
import com.rangers.main.lexer.Lexer;
import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Optimizer;
import com.rangers.main.model.TAC;
import com.rangers.main.model.TACGenerator;
import com.rangers.main.model.TargetCodeGenerator;
import com.rangers.main.model.Token;
import com.rangers.main.model.SemanticAnalyzer;
import com.rangers.main.parser.Parser;

@Service
public class CompilerService {

    @Autowired
    private Lexer lexer;

    @Autowired
    private Parser parser;

    @Autowired
    private SemanticAnalyzer semanticAnalyzer;

    @Autowired
    private TACGenerator tacGenerator;

    @Autowired
    private Optimizer optimizer;

    @Autowired
    private TargetCodeGenerator targetCodeGenerator;

    @Autowired
    private Interpreter interpreter;

    public String compile(String code) {

        try {

            if (code == null || code.trim().isEmpty()) {
                return "Compilation Error: Input program is empty";
            }

            StringBuilder result = new StringBuilder();

            // =================================================
            // PHASE 1 : LEXICAL ANALYSIS
            // =================================================
            List<Token> tokens = lexer.tokenize(code);

            result.append("===== PHASE 1 : LEXICAL ANALYSIS =====\n\n");

            for (Token token : tokens) {
                result.append(token).append("\n");
            }

            // =================================================
            // PHASE 2 : SYNTAX ANALYSIS
            // =================================================
            ASTNode ast = parser.parse(tokens);

            result.append("\n\n===== PHASE 2 : SYNTAX ANALYSIS =====\n\n");
            result.append(ast);

            // =================================================
            // PHASE 3 : SEMANTIC ANALYSIS
            // =================================================
            result.append("\n\n");
            result.append(semanticAnalyzer.analyze(ast));

            // =================================================
            // PHASE 4 : TAC GENERATION
            // =================================================
            List<TAC> tacList = tacGenerator.generate(ast);

            result.append("\n\n===== PHASE 4 : THREE ADDRESS CODE =====\n\n");

            for (TAC tac : tacList) {
                result.append(tac).append("\n");
            }

            // =================================================
            // PHASE 5 : OPTIMIZATION
            // =================================================
            result.append("\n\n===== PHASE 5 : CODE OPTIMIZATION =====\n\n");

            List<TAC> optimized =
                    optimizer.optimize(
                            tacList.stream()
                                   .map(t -> new com.rangers.main.model.Quadruple(
                                           t.getOp(),
                                           t.getArg1(),
                                           t.getArg2(),
                                           t.getResult()
                                   ))
                                   .toList()
                    ).stream()
                     .map(q -> new TAC(
                             q.getOp(),
                             q.getArg1(),
                             q.getArg2(),
                             q.getResult()
                     ))
                     .toList();

            for (TAC tac : optimized) {
                result.append(tac).append("\n");
            }

            // =================================================
            // PHASE 6 : TARGET CODE GENERATION
            // =================================================
            List<String> targetCode =
                    targetCodeGenerator.generate(optimized);

            result.append("\n\n===== PHASE 6 : TARGET CODE =====\n\n");

            for (String line : targetCode) {
                result.append(line).append("\n");
            }

            // =================================================
            // PHASE 7 : EXECUTION
            // =================================================
            result.append("\n\n===== PHASE 7 : PROGRAM EXECUTION =====\n\n");

            String execution =
                    interpreter.execute(targetCode);

            result.append(execution);

            return result.toString();

        } catch (Exception e) {

            e.printStackTrace();

            return "Compilation Error : " + e.getMessage();
        }
    }
}