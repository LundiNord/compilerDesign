package edu.kit.kastel.vads.compiler.ir.optimize;

import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BinaryOperationNode;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;

public class ConstantFolding implements Optimizer {
    //ToDo: use this class
    @Override
    public Node transform(Node node) {
        if (node instanceof BinaryOperationNode) {
            if (node.predecessor(BinaryOperationNode.LEFT) instanceof ConstIntNode
                && node.predecessor(BinaryOperationNode.RIGHT) instanceof ConstIntNode) {
                int cL = ((ConstIntNode) node.predecessor(BinaryOperationNode.LEFT)).value();
                int cR = ((ConstIntNode) node.predecessor(BinaryOperationNode.RIGHT)).value();

                return switch(node) {
                    case AddNode _ -> new ConstIntNode(node.block(), cL + cR);
                    case SubNode _ -> new ConstIntNode(node.block(), cL - cR);
                    case MulNode _ -> new ConstIntNode(node.block(), cL * cR);
                    case DivNode _ -> {
                        if (cR == 0) yield node;
                        if (cL == Integer.MIN_VALUE && cR == -1) yield node;
                        yield new ConstIntNode(node.block(), cL / cR);
                    }
                    case ModNode _ -> {
                        if (cR == 0) yield node;
                        if (cL == Integer.MIN_VALUE && cR == -1) yield node;
                        yield new ConstIntNode(node.block(), cL % cR);
                    }
                    default -> node;
                };
            }
        }
        return node;
    }
}
