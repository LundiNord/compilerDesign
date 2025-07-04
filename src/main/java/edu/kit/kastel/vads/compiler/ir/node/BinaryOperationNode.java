package edu.kit.kastel.vads.compiler.ir.node;

public sealed class BinaryOperationNode extends Node
    permits AddNode, BitwiseCompNode, CompNode, DivNode, ModNode, MulNode, ShiftNode, SubNode {

    public static final int LEFT = 0;
    public static final int RIGHT = 1;

    public BinaryOperationNode(Node lhs, Node rhs) {
        super(lhs, rhs);
    }

}
