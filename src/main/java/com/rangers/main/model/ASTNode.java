package com.rangers.main.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ASTNode {

    private String type;
    private String value;
    private List<ASTNode> children;

    public ASTNode(String type) {
        this.type = type;
        this.children = new ArrayList<>();
    }

    public ASTNode(String type, String value) {
        this.type = type;
        this.value = value;
        this.children = new ArrayList<>();
    }

    public void addChild(ASTNode node) {
        if (node != null) {
            children.add(node);
        }
    }
    
    public String printTree(String indent) {
        StringBuilder sb = new StringBuilder();

        sb.append(indent).append(type);

        if (value != null) {
            sb.append(" : ").append(value);
        }
        sb.append("\n");

        for (ASTNode child : children) {
            sb.append(child.printTree(indent + "  "));
        }

        return sb.toString();
    }

}