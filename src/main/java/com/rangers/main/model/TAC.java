package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TAC {

    private String op;
    private String arg1;
    private String arg2;
    private String result;

    @Override
    public String toString() {

        if ("IF".equals(op)) {
            return "IF " + arg1 + " GOTO " + result;
        }

        if ("GOTO".equals(op)) {
            return "GOTO " + result;
        }

        if ("LABEL".equals(op)) {
            return result + ":";
        }

        if ("=".equals(op)) {
            return result + " = " + arg1;
        }

        return result + " = " + arg1 + " " + op + " " + arg2;
    }
}