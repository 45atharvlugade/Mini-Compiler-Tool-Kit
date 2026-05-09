package com.rangers.main.model;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class SymbolTable {

    // =========================================
    // SYMBOL STORAGE
    // variable → datatype
    // =========================================
    private final Map<String, String> table =
            new HashMap<>();

    // =========================================
    // DECLARE VARIABLE
    // =========================================
    public void declare(String name, String type) {

        if (name == null || type == null) {
            return;
        }

        table.put(name, type);
    }

    // =========================================
    // CHECK DECLARATION
    // =========================================
    public boolean isDeclared(String name) {

        return table.containsKey(name);
    }

    // =========================================
    // LOOKUP VARIABLE TYPE
    // =========================================
    public String lookup(String name) {

        return table.getOrDefault(name, "unknown");
    }

    // =========================================
    // GET COMPLETE TABLE
    // =========================================
    public Map<String, String> getTable() {

        return table;
    }

    // =========================================
    // CLEAR TABLE
    // =========================================
    public void clear() {

        table.clear();
    }
}