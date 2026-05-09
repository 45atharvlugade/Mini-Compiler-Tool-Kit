package com.rangers.main.codegenrator;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.rangers.main.model.ASTNode;
import com.rangers.main.model.Quadruple;

@Component
public class QuadGenerator {

    private List<Quadruple> quadList;
    private int tempCount;

    public List<Quadruple> generate(ASTNode root) {

        quadList = new ArrayList<>();
        tempCount = 0;

        visit(root);

        return quadList;
    }

    private String newTemp() {
        return "t" + (++tempCount);
    }

    private void visit(ASTNode node) {

        if (node == null) return;

        switch (node.getType()) {

            // ================= ASSIGN =================
            case "ASSIGN":

                String left = node.getChildren().get(0).getValue();
                String right = eval(node.getChildren().get(1));

                quadList.add(new Quadruple("=", right, "-", left));
                break;

            // ================= DECLARATION =================
            case "DECLARATION":

                String var = node.getChildren().get(0).getValue();

                if (node.getChildren().size() > 1) {

                    String val = eval(node.getChildren().get(1));
                    quadList.add(new Quadruple("=", val, "-", var));

                } else {

                    quadList.add(new Quadruple("=", "0", "-", var));
                }

                break;

            default:
                for (ASTNode child : node.getChildren()) {
                    visit(child);
                }
        }
    }

    private String eval(ASTNode node) {

        if (node.getChildren().isEmpty()) {
            return node.getValue();
        }

        if (node.getChildren().size() == 2) {

            String l = eval(node.getChildren().get(0));
            String r = eval(node.getChildren().get(1));

            String temp = newTemp();

            quadList.add(new Quadruple(
                    node.getValue(),
                    l,
                    r,
                    temp
            ));

            return temp;
        }

        return node.getValue();
    }
}