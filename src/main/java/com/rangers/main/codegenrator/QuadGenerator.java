package com.rangers.main.codegenrator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Quadruple;

@Component
public class QuadGenerator {

    private List<Quadruple> code;
    private int tempCount;

    public List<Quadruple> generate(ASTNode root) {

        code = new ArrayList<>();
        tempCount = 0;

        visit(root);

        return code;
    }

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private void visit(ASTNode node) {

        if (node == null) return;

        if (node.getType().equals("ASSIGN") ||
            node.getType().equals("DECLARATION")) {

            handleAssign(node);
        }

        for (ASTNode child : node.getChildren()) {
            visit(child);
        }
    }

    private void handleAssign(ASTNode node) {

        String left = node.getChildren().get(0).getValue();
        ASTNode expr = node.getChildren().get(1);

        String result = eval(expr);

        code.add(new Quadruple("=", result, "-", left));
    }

    private String eval(ASTNode node) {

        if (node.getChildren().isEmpty()) {
            return node.getValue();
        }

        if (node.getChildren().size() == 2) {

            String l = eval(node.getChildren().get(0));
            String r = eval(node.getChildren().get(1));

            String temp = newTemp();

            code.add(new Quadruple(node.getValue(), l, r, temp));

            return temp;
        }

        return node.getValue();
    }
}