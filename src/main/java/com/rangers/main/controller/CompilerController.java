package com.rangers.main.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.rangers.main.codegenrator.QuadGenerator;
import com.rangers.main.lexer.Lexer;
import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Optimizer;
import com.rangers.main.model.Quadruple;
import com.rangers.main.model.SemanticAnalyzer;
import com.rangers.main.model.TAC;
import com.rangers.main.model.TACGenerator;
import com.rangers.main.model.TargetCodeGenerator;
import com.rangers.main.model.Token;
import com.rangers.main.parser.Parser;
import com.rangers.main.service.CompilerService;

@RestController
@RequestMapping("/compiler")
public class CompilerController {

    @Autowired
    private Lexer lexer;

    @Autowired
    private Parser parser;

    @Autowired
    private SemanticAnalyzer semanticAnalyzer;

    @Autowired
    private CompilerService compilerService;
    
    @Autowired
    private TACGenerator tacGenerator;
    
    @Autowired
    private QuadGenerator quadGenerator;
    
    @Autowired
    private Optimizer optimizer;
    
    @Autowired
    private TargetCodeGenerator targetCodeGenerator;

    // ================= FILE COMPILATION =================
    @PostMapping("/compile-file")
    public ResponseEntity<String> compileFile(@RequestBody String input) {

        if (input == null || input.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("Input Error: Empty code provided");
        }

        String code = input.trim();

        String result = compilerService.compile(code);

        return ResponseEntity.ok(result);
    }

    // ================= PHASE 1: LEXER =================
    @PostMapping("/phase1/lexer")
    public ResponseEntity<List<Token>> lexicalAnalysis(@RequestBody String input) {

        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Input is empty");
        }

        return ResponseEntity.ok(lexer.tokenize(input));
    }

    // ================= PHASE 2: PARSER =================
    @PostMapping("/phase2/parser")
    public ResponseEntity<String> syntaxAnalysis(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        return ResponseEntity.ok(ast.printTree(""));
    }

    // ================= PHASE 3: SEMANTIC =================
    @PostMapping("/phase3/semantic")
    public ResponseEntity<String> semantic(@RequestBody String input) {

        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Input code is empty");
        }

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        String result = semanticAnalyzer.analyze(ast);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/phase4/tac")
    public ResponseEntity<String> generateTAC(@RequestBody String input) {

        if (input == null || input.trim().isEmpty()) {
            throw new RuntimeException("Input code is empty");
        }

        // Phase 1
        List<Token> tokens = lexer.tokenize(input);

        // Phase 2
        ASTNode ast = parser.parse(tokens);

        // Phase 3 (optional)
        semanticAnalyzer.analyze(ast);

        // Phase 4
        List<TAC> tac = tacGenerator.generate(ast);

        StringBuilder sb = new StringBuilder();
        sb.append("===== PHASE 4: THREE ADDRESS CODE =====\n\n");

        if (tac.isEmpty()) {
            sb.append("No TAC generated\n");
        } else {
            for (TAC t : tac) {
                sb.append(t).append("\n");
            }
        }

        return ResponseEntity.ok(sb.toString());
    }
    
    @PostMapping("/phase4/quadruple")
    public ResponseEntity<String> generateQuad(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        List<Quadruple> quad = quadGenerator.generate(ast);

        StringBuilder sb = new StringBuilder();

        sb.append("===== PHASE 4: QUADRUPLE IR =====\n\n");

        for (Quadruple q : quad) {
            sb.append(q).append("\n");
        }

        return ResponseEntity.ok(sb.toString());
    }
    
    @PostMapping("/compile/full")
    public ResponseEntity<?> fullCompile(@RequestBody String input) {

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        String semantic = semanticAnalyzer.analyze(ast);

        List<TAC> tac = tacGenerator.generate(ast);

        List<Quadruple> quad = quadGenerator.generate(ast);

        List<Quadruple> optimized = optimizer.optimize(quad);

        return ResponseEntity.ok(Map.of(
        	    "semantic", semantic,
        	    "tac", tac.toString(),
        	    "quadruples", quad,
        	    "optimized", optimized
        	));
    }
    
    @PostMapping("/phase6/target")
    public ResponseEntity<String> generateTarget(@RequestBody String input) {

        if (input == null || input.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Input code is empty");
        }

        input = input.trim();

        List<Token> tokens = lexer.tokenize(input);
        ASTNode ast = parser.parse(tokens);

        // Phase 4 → TAC
        List<TAC> tac = tacGenerator.generate(ast);

        // Phase 6 → Target Code
        List<String> target = targetCodeGenerator.generate(tac);

        StringBuilder sb = new StringBuilder();

        sb.append("===== PHASE 6: TARGET CODE =====\n\n");

        if (target.isEmpty()) {
            sb.append("No Target Code Generated\n");
        } else {
            for (String line : target) {

                sb.append(line.trim()).append("\n");

                // extra spacing after labels
                if (line.endsWith(":")) {
                    sb.append("\n");
                }
            }
        }

        return ResponseEntity
                .ok()
                .header("Content-Type", "text/plain")
                .body(sb.toString());
    }
}