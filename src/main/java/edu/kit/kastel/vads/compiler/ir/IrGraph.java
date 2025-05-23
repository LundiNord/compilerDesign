package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.ProjNode;

import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;

public class IrGraph {
    private final Map<Node, SequencedSet<Node>> successors = new IdentityHashMap<>();
    private final Block startBlock;
    private final Block endBlock;
    private final String name;

    public IrGraph(String name) {
        this.name = name;
        this.startBlock = new Block(this);
        this.endBlock = new Block(this);
    }

    public void registerSuccessor(Node node, Node successor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).add(successor);
    }

    public void removeSuccessor(Node node, Node oldSuccessor) {
        this.successors.computeIfAbsent(node, _ -> new LinkedHashSet<>()).remove(oldSuccessor);
    }

    /// {@return the set of nodes that have the given node as one of their inputs}
    public Set<Node> successors(Node node) {
        SequencedSet<Node> successors = this.successors.get(node);
        if (successors == null) {
            return Set.of();
        }
        return Set.copyOf(successors);
    }

    public Block startBlock() {
        return this.startBlock;
    }

    public Block endBlock() {
        return this.endBlock;
    }

    /// {@return the name of this graph}
    public String name() {
        return name;
    }

    public Set<Node> getNodes() {
        return successors.keySet();
    }

    /**
     * FixMe ugly code
     * gets called multiple time with same input.
     * @param oldNode
     * @param newNode
     */
    public void replaceNode(Node oldNode, Node newNode) {
        if (oldNode.predecessors().size() >=3) {
            Node projNode = oldNode.predecessors().get(2);
            if(projNode != null) {
                newNode.addPredecessor(projNode);
            }
        }
        SequencedSet<Node> oldSuccessors = this.successors.get(oldNode);
        this.successors.put(newNode, oldSuccessors);
        for (Node node : this.successors.keySet()) {
                node.replacePredecessor(oldNode, newNode);
        }
        this.successors.remove(oldNode);
    }


}
