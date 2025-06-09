package edu.kit.kastel.vads.compiler.ir.node;

public final class BitwiseCompNode extends BinaryOperationNode {

    private final BitwiseCompType type;

    public BitwiseCompNode(Node lhs, Node rhs, BitwiseCompType type) {
        super(lhs, rhs);
        this.type = type;
    }

    public BitwiseCompType getType() {
        return type;
    }

    public enum BitwiseCompType {
        AND,
        OR,
        XOR,
    }
}
