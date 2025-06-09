package edu.kit.kastel.vads.compiler.lexer;

import edu.kit.kastel.vads.compiler.Span;

public record Operator(OperatorType type, Span span) implements Token {

    @Override
    public boolean isOperator(OperatorType operatorType) {
        return type() == operatorType;
    }

    @Override
    public String asString() {
        return type().toString();
    }

    public enum OperatorType {
        ASSIGN_MINUS("-="),
        MINUS("-"),
        ASSIGN_PLUS("+="),
        PLUS("+"),
        MUL("*"),
        ASSIGN_MUL("*="),
        ASSIGN_DIV("/="),
        DIV("/"),
        ASSIGN_MOD("%="),
        MOD("%"),
        ASSIGN("="),
        LEFT_SHIFT("<<"),
        LEFT_SHIFT_ASSIGN("<<="),
        RIGHT_SHIFT(">>"),
        RIGHT_SHIFT_ASSIGN(">>="),
        GREATER_THAN(">"),
        GREATER_THAN_OR_EQUAL(">="),
        LESS_THAN("<"),
        LESS_THAN_OR_EQUAL("<="),
        EQUALS("=="),
        NOT_EQUALS("!="),
        LOGICAL_NOT("!"),
        BITWISE_NOT("~"),
        //UNARY_MINUS("-"),
        BITWISE_AND("&"),
        BITWISE_EXCLUSIVE_OR("^"),
        BITWISE_EXCLUSIVE_OR_ASSIGN("^="),
        BITWISE_OR("|"),
        LOGICAL_AND("&&"),
        LOGICAL_OR("||"),
        BITWISE_OR_ASSIGN("|="),
        BITWISE_AND_ASSIGN("&=");

        private final String value;

        OperatorType(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }
}
