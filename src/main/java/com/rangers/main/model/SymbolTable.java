package com.rangers.main.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SymbolTable {

    private Map<String, String> table = new HashMap<>();

    public void declare(String name, String type) {
        table.put(name, type);
    }

    public boolean isDeclared(String name) {
        return table.containsKey(name);
    }

    public Map<String, String> getTable() {
        return table;
    }
}