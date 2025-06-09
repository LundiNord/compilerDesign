package edu.kit.kastel.vads.compiler.ir.node;

public final class CompNode extends BinaryOperationNode {

    private final CompType type;

    public CompNode(Node lhs, Node rhs, CompType type) {
        super(lhs, rhs);
        this.type = type;
    }

    public CompType getType() {
        return type;
    }

    public enum CompType {
        EQUALS,
        NOT_EQUALS,
        LESS_THAN,
        LESS_THAN_OR_EQUALS,
        GREATER_THAN,
        GREATER_THAN_OR_EQUALS,
    }

}
