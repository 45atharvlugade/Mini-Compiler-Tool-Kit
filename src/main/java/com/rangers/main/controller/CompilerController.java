package com.rangers.main.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rangers.main.codegenrator.QuadGenerator;
import com.rangers.main.lexer.Lexer;
import com.rangers.main.model.ASTNode;
import com.rangers.main.model.IndirectTripleGenerator;
import com.rangers.main.model.Optimizer;
import com.rangers.main.model.Quadruple;
import com.rangers.main.model.SemanticAnalyzer;
import com.rangers.main.model.TAC;
import com.rangers.main.model.TACGenerator;
import com.rangers.main.model.TargetCodeGenerator;
import com.rangers.main.model.Token;
import com.rangers.main.model.Triple;
import com.rangers.main.model.TripleGenerator;
import com.rangers.main.parser.Parser;
import com.rangers.main.service.CompilerService;

@RestController
@RequestMapping("/compiler")
public class CompilerController {

    // =====================================================
    // DEPENDENCIES
    // =====================================================

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
    private CompilerService compilerService;
    
    @Autowired
    private TripleGenerator tripleGenerator;
    
    @Autowired
    private IndirectTripleGenerator indirectTripleGenerator;
    


    // =====================================================
    // FULL COMPILATION
    // =====================================================

    @PostMapping("/compile")
    public ResponseEntity<String> compile(
            @RequestBody String input
    ) {

        if (input == null ||
            input.trim().isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body("Compilation Error : Empty Input");
        }

        String result =
                compilerService.compile(input);

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // PHASE 1 : LEXICAL ANALYSIS
    // =====================================================

    @PostMapping("/phase1/lexer")
    public ResponseEntity<List<Token>> lexer(
            @RequestBody String input
    ) {

        List<Token> tokens =
                lexer.tokenize(input);

        return ResponseEntity.ok(tokens);
    }
    

    // =====================================================
    // PHASE 2 : SYNTAX ANALYSIS
    // =====================================================

    @PostMapping("/phase2/parser/text")
    public ResponseEntity<String> syntaxAnalysis(
            @RequestBody String input
    ) {

        List<Token> tokens = lexer.tokenize(input);

        ASTNode ast = parser.parse(tokens);

        return ResponseEntity.ok(ast.printTree());
    }
    
    @PostMapping("/phase2/parser/json")
    public ResponseEntity<?> parseJson(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        return ResponseEntity.ok(ast.toJson());
    }

    // =====================================================
    // PHASE 3 : SEMANTIC ANALYSIS
    // =====================================================

    @PostMapping("/phase3/semantic")
    public ResponseEntity<String> semantic(
            @RequestBody String input
    ) {

        List<Token> tokens =
                lexer.tokenize(input);

        ASTNode ast =
                parser.parse(tokens);

        String result =
                semanticAnalyzer.analyze(ast);

        return ResponseEntity.ok(result);
    }

    // =====================================================
    // PHASE 4 : TAC GENERATION
    // =====================================================

    @PostMapping("/phase4/ir-tables")
    public ResponseEntity<?> generateAllIR(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        // TAC
        List<TAC> tac = tacGenerator.generate(ast);

        // Quadruples
        List<Quadruple> quad = tac.stream()
                .map(t -> new Quadruple(
                        t.getOp(),
                        t.getArg1(),
                        t.getArg2(),
                        t.getResult()
                ))
                .toList();

        // Triples
        List<Triple> triples = tripleGenerator.generate(tac);

        // Indirect Triples
        Object indirect = indirectTripleGenerator.generate(triples);

        // FINAL RESPONSE
        return ResponseEntity.ok(
                Map.of(
                        "tac", tac,
                        "quadruples", quad,
                        "triples", triples,
                        "indirectTriples", indirect
                )
        );
    }

    // =====================================================
    // PHASE 5 : OPTIMIZATION
    // =====================================================

    @PostMapping("/phase5/optimization")
    public ResponseEntity<?> optimize(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        List<TAC> tacList = tacGenerator.generate(ast);

        List<Quadruple> quadruples = tacList.stream()
                .map(t -> new Quadruple(
                        t.getOp(),
                        t.getArg1(),
                        t.getArg2(),
                        t.getResult()
                ))
                .toList();

        List<Quadruple> optimized = optimizer.optimize(quadruples);

        return ResponseEntity.ok(
                Map.of(
                        "originalQuadruples", quadruples,
                        "optimizedQuadruples", optimized
                )
        );
    }

    // =====================================================
    // PHASE 6 : TARGET CODE
    // =====================================================

    @PostMapping("/phase6/target")
    public ResponseEntity<String> target(
            @RequestBody String input
    ) {

        List<Token> tokens =
                lexer.tokenize(input);

        ASTNode ast =
                parser.parse(tokens);

        List<TAC> tacList =
                tacGenerator.generate(ast);

        List<String> targetCode =
                targetCodeGenerator.generate(
                        tacList
                );

        StringBuilder sb =
                new StringBuilder();

        sb.append(
                "===== PHASE 6 : TARGET CODE =====\n\n"
        );

        for (String line : targetCode) {

            sb.append(line)
              .append("\n");
        }

        return ResponseEntity.ok(
                sb.toString()
        );
    }

    // =====================================================
    // ALL PHASES JSON RESPONSE
    // =====================================================

    @PostMapping("/all")
    public ResponseEntity<?> all(
            @RequestBody String input
    ) {

        List<Token> tokens =
                lexer.tokenize(input);

        ASTNode ast =
                parser.parse(tokens);

        String semantic =
                semanticAnalyzer.analyze(ast);

        List<TAC> tac =
                tacGenerator.generate(ast);

        List<Quadruple> quadruples =
                tac.stream()
                   .map(t -> new Quadruple(
                           t.getOp(),
                           t.getArg1(),
                           t.getArg2(),
                           t.getResult()
                   ))
                   .toList();

        List<Quadruple> optimized =
                optimizer.optimize(quadruples);

        List<String> target =
                targetCodeGenerator.generate(tac);

        return ResponseEntity.ok(
                Map.of(
                        "tokens", tokens,
                        "ast", ast.printTree(""),
                        "semantic", semantic,
                        "tac", tac,
                        "optimized", optimized,
                        "target", target
                )
        );
    }
}