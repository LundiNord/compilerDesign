package edu.kit.kastel.vads.compiler.ir.node;

public non-sealed class ShiftNode extends BinaryOperationNode {

    private final Shift shift;

    public ShiftNode(Node left, Node right, Shift shift) {
        super(left, right);
        this.shift = shift;
    }

    public Shift getShift() {
        return shift;
    }

    public enum Shift {
        RIGHT,
        LEFT
    }

}
