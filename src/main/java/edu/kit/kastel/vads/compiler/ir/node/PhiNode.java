package edu.kit.kastel.vads.compiler.ir.node;

public non-sealed class PhiNode extends Node {

    public PhiNode(Node... predecessors) {
        super(predecessors);
    }

}
