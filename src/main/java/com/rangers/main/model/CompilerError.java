package com.rangers.main.model;

import javax.lang.model.type.ErrorType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class CompilerError {

    private final ErrorType type;   // Enum (LEXICAL, SYNTAX, SEMANTIC)
    private final String message;
    private final int position;
}