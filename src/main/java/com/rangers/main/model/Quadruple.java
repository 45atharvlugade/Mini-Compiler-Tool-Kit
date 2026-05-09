package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Quadruple {

    // Operation
    private String op;

    // Operands
    private String arg1;
    private String arg2;

    // Destination
    private String result;

    @Override
    public String toString() {

        return String.format(
                "(%s, %s, %s, %s)",
                op,
                arg1,
                arg2,
                result
        );
    }
}