package edu.kit.kastel.vads.compiler.ir.node;

public non-sealed class ConstIntNode extends Node {

    public final int value;

    public ConstIntNode(int value) {
        super();
        this.value = value;
    }

    public int value() {
        return this.value;
    }
}
