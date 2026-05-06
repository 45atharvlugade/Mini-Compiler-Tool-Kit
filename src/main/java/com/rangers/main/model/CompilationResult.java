package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CompilationResult {

    private final List<Token> tokens;
    private final String intermediateCode;
    private final List<CompilerError> errors;
}