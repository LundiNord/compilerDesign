package edu.kit.kastel.vads.compiler.ir.node;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/// The base class for all nodes.
public abstract sealed class Node
    permits BinaryOperationNode, ConstIntNode, EndNode, IfNode, PhiNode, RegionNode, ReturnNode, StartNode, WhileNode {
    private final List<Node> predecessors = new ArrayList<>();
    private final List<Node> successors = new ArrayList<>();
    private final DebugInfo debugInfo;
    @Nullable
    private Register destination;   //used for instruction selection

    protected Node(Node... predecessors) {
        this.predecessors.addAll(List.of(predecessors));
        for(Node n : predecessors) {
            if (n != null) {
                n.addSuccessor(this);
            }
        }
        this.debugInfo = DebugInfoHelper.getDebugInfo();
    }

    public final List<? extends Node> predecessors() {
        return this.predecessors;
    }

    public final List<? extends Node> successors() {
        return this.successors;
    }

    public final void setPredecessor(int idx, Node node) {
        this.predecessors.set(idx, node);
    }

    public final void addPredecessor(Node node) {
        this.predecessors.add(node);
    }
    public final void addSuccessor(Node node) {
        this.successors.add(node);
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

    public @Nullable Register getDestination() {
        return destination;
    }
    public void setDestination(@Nullable Register destination) {
        this.destination = destination;
    }
}
