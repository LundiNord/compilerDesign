package edu.kit.kastel.vads.compiler.ir.node;

public final class JmpNode extends Node {

    public JmpNode(Block block, Node predecessor, Node condition) {
        super(block, predecessor);
        //addPredecessor(jump);
    }

}
