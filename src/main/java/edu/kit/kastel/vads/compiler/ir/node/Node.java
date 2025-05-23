package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// The base class for all nodes.
public abstract sealed class Node
    permits BinaryOperationNode, Block, ConstIntNode, JmpNode, Phi, ProjNode, ReturnNode, StartNode {
    private final IrGraph graph;
    private final Block block;
    private final List<Node> predecessors = new ArrayList<>();
    private final DebugInfo debugInfo;
    @Nullable
    private Register destination;

    protected Node(Block block, Node... predecessors) {
        this.graph = block.graph();
        this.block = block;
        this.predecessors.addAll(List.of(predecessors));
        for (Node predecessor : predecessors) {
            graph.registerSuccessor(predecessor, this);
        }
        this.debugInfo = DebugInfoHelper.getDebugInfo();
    }

    protected Node(IrGraph graph) {
        assert this.getClass() == Block.class : "must be used by Block only";
        this.graph = graph;
        this.block = (Block) this;
        this.debugInfo = DebugInfo.NoInfo.INSTANCE;
    }

    public final IrGraph graph() {
        return this.graph;
    }

    public final Block block() {
        return this.block;
    }

    public final List<? extends Node> predecessors() {
        return List.copyOf(this.predecessors);
    }

    public final void setPredecessor(int idx, Node node) {
        this.graph.removeSuccessor(this.predecessors.get(idx), this);
        this.predecessors.set(idx, node);
        this.graph.registerSuccessor(node, this);
    }

    public final void addPredecessor(Node node) {
        this.predecessors.add(node);
        this.graph.registerSuccessor(node, this);
    }

    /**
     *  Replaces a node from the predecessor list.
     *  Has no side effects on the graph.
     * @param oldNode
     * @param newNode
     */
    public final void replacePredecessor(Node oldNode, Node newNode) {
        if (!this.predecessors.contains(oldNode)) {
            return;
        }
        this.predecessors.set(this.predecessors.indexOf(oldNode), newNode);
    }

    public final Node predecessor(int idx) {
        return this.predecessors.get(idx);
    }

    @Override
    public final String toString() {
        return (this.getClass().getSimpleName().replace("Node", "") + " " + info()).stripTrailing();
    }

    protected String info() {
        return "";
    }

    public DebugInfo debugInfo() {
        return debugInfo;
    }

    protected static int predecessorHash(Node node, int predecessor) {
        return System.identityHashCode(node.predecessor(predecessor));
    }
    public @Nullable Register getDestination() {
        return destination;
    }
    public void setDestination(@Nullable Register destination) {
        this.destination = destination;
    }
}
