package edu.kit.kastel.vads.compiler.parser.ast;

public sealed interface StatementTree extends Tree
    permits AssignmentTree, BlockTree, ConditionalJumpTree, DeclarationTree, LoopCtrlTree, ReturnTree, WhileTree {
}
