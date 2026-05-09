package com.rangers.main.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Triple {

    private int index;
    private String operator;
    private String arg1;
    private String arg2;
}