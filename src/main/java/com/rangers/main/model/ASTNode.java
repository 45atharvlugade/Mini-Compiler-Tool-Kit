package com.rangers.main.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ASTNode {

    // =========================================
    // NODE DATA
    // =========================================
    private String type;

    private String value;

    private List<ASTNode> children;

    // =========================================
    // CONSTRUCTOR
    // =========================================
    public ASTNode(String type) {

        this.type = type;

        this.children = new ArrayList<>();
    }

    // =========================================
    // CONSTRUCTOR WITH VALUE
    // =========================================
    public ASTNode(String type, String value) {

        this.type = type;

        this.value = value;

        this.children = new ArrayList<>();
    }

    // =========================================
    // ADD CHILD
    // =========================================
    public void addChild(ASTNode node) {

        if (node != null) {

            children.add(node);
        }
    }

    // =========================================
    // SIMPLE TREE (OLD STYLE)
    // =========================================
    public String printTree(String indent) {

        StringBuilder sb = new StringBuilder();

        sb.append(indent)
          .append(type);

        if (value != null &&
            !value.isEmpty()) {

            sb.append(" : ")
              .append(value);
        }

        sb.append("\n");

        for (ASTNode child : children) {

            sb.append(
                    child.printTree(indent + "  ")
            );
        }

        return sb.toString();
    }

    // =========================================
    // BEAUTIFUL HIERARCHICAL TREE
    // =========================================
    public String printTree() {

        return printTree("", true);
    }

    // =========================================
    // TREE HELPER
    // =========================================
    private String printTree(
            String prefix,
            boolean isLast
    ) {

        StringBuilder sb =
                new StringBuilder();

        // current node
        sb.append(prefix);

        sb.append(
                isLast
                ? "└── "
                : "├── "
        );

        sb.append(type);

        if (value != null &&
            !value.isEmpty()) {

            sb.append(" : ")
              .append(value);
        }

        sb.append("\n");

        // children
        for (int i = 0;
             i < children.size();
             i++) {

            boolean last =
                    (i == children.size() - 1);

            sb.append(
                    children.get(i)
                            .printTree(
                                    prefix +
                                    (
                                        isLast
                                        ? "    "
                                        : "│   "
                                    ),
                                    last
                            )
            );
        }

        return sb.toString();
    }
    
    public Map<String, Object> toJson() {

        Map<String, Object> map = new LinkedHashMap<>();

        map.put("type", type);

        if (value != null) {
            map.put("value", value);
        }

        List<Map<String, Object>> childList = new ArrayList<>();

        for (ASTNode child : children) {
            childList.add(child.toJson());
        }

        map.put("children", childList);

        return map;
    }
}