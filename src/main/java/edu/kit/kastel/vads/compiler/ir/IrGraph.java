package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class IrGraph {
    private final List<Node> nodes;
    private final String name;
    private final Node startNode;
    private final Node endNode;

    public IrGraph(String name, Node start, Node end) {
        this.name = name;
        this.startNode = start;
        this.endNode = end;
        this.nodes = new ArrayList<>();
    }

//    /// {@return the set of nodes that have the given node as one of their inputs}
//    public Set<Node> successors(Node node) {
//        SequencedSet<Node> successors = this.successors.get(node);
//        if (successors == null) {
//            return Set.of();
//        }
//        return Set.copyOf(successors);
//    }

    public Node startNode() {
        return this.startNode;
    }

    public Node endNode() {
        return this.endNode;
    }

    /// {@return the name of this graph}
    public String name() {
        return name;
    }

    public Set<Node> getNodes() {
        //ToDo
        return Set.copyOf(nodes);    }

    /**
     * FixMe ugly code
     * gets called multiple time with same input.
     * @param oldNode
     * @param newNode
     */
    public void replaceNode(Node oldNode, Node newNode) {
        //ToDo
//        if (oldNode.predecessors().size() >=3) {
//            Node projNode = oldNode.predecessors().get(2);
//            if(projNode != null) {
//                newNode.addPredecessor(projNode);
//            }
//        }
//        SequencedSet<Node> oldSuccessors = this.successors.get(oldNode);
//        this.successors.put(newNode, oldSuccessors);
//        for (Node node : this.successors.keySet()) {
//                node.replacePredecessor(oldNode, newNode);
//        }
//        this.successors.remove(oldNode);
    }


}
