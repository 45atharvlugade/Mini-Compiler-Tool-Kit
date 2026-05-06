package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
public class Token {

    private final TokenType type;
    private final String value;
    private final int position;
}