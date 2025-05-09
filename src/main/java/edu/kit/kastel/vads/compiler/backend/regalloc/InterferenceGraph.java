package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.*;

/**
 * Graph as an adjacence List.
 * Node {@link InterferenceNode}.
 * @author ujiqk
 * @version 1.0 */
public class InterferenceGraph {
    //https://www.baeldung.com/java-graphs

    private final Map<InterferenceLabel, InterferenceNode> adjacencyList;

    public InterferenceGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addVertex(InfiniteRegister label) {
        if (label == null) {
            throw new IllegalArgumentException("Label cannot be null");
        }
        adjacencyList.putIfAbsent(new InterferenceLabel(label.getNumber()), new InterferenceNode(label));
    }
//    public void removeVertex(Register label) {
//        InterferenceNode v = new InterferenceNode(label);
//        adjacencyList.values().forEach(e -> e.remove(v));
//        adjacencyList.remove(new InterferenceNode(label));
//    }

    public InterferenceNode getNode(InfiniteRegister label) {
        return adjacencyList.get(new InterferenceLabel(label.getNumber()));
    }
    public void addEdge(InfiniteRegister label1, InfiniteRegister label2) {
        InterferenceNode v1 = adjacencyList.get(new InterferenceLabel(label1.getNumber()));
        InterferenceNode v2 = adjacencyList.get(new InterferenceLabel(label2.getNumber()));
        if (v1 == null) {
            addVertex(label1);
            v1 = adjacencyList.get(new InterferenceLabel(label1.getNumber()));
        }
        if (v2 == null) {
            addVertex(label2);
            v2 = adjacencyList.get(new InterferenceLabel(label2.getNumber()));
        }
        v1.addAdjacent(new InterferenceLabel(label2.getNumber()));
        v2.addAdjacent(new InterferenceLabel(label1.getNumber()));
    }
//    public void removeEdge(Register label1, Register label2) {
//        InterferenceNode v1 = new InterferenceNode(label1);
//        InterferenceNode v2 = new InterferenceNode(label2);
//        List<InterferenceNode> eV1 = adjacencyList.get(v1);
//        List<InterferenceNode> eV2 = adjacencyList.get(v2);
//        if (eV1 != null)
//            eV1.remove(v2);
//        if (eV2 != null)
//            eV2.remove(v1);
//    }
    public List<InterferenceNode> getVertexes() {
        return new ArrayList<>(adjacencyList.values());
    }

    /**
     * Does Maximum Cardinality Search.
     * @return all nodes ordered by cardinality.
     */
    private List<InterferenceNode> doMCS() {
        // |V| = n
        int n = adjacencyList.size();
        //For all v ∈ V set wt(v) ← 0
        //done
        //Let W ← V
        List<InterferenceNode> w = new ArrayList<>(adjacencyList.values());
        w.sort(Comparator.comparingInt(InterferenceNode::getMcsWeight).reversed());
        List <InterferenceNode> viList = new ArrayList<>();
        //For i ← 1 to n do
        for (int i = 1; i <= n; i++) {
            //Let v be a node of maximal weight in W
            InterferenceNode v = w.getFirst();
            //Set vi ← v
            viList.add(v);
            //For all u ∈ W ∩ N (v) set wt(u) ← wt(u) + 1
            for (InterferenceLabel uu : adjacencyList.get(new InterferenceLabel(v.getReg().getNumber())).getAdjacent()) {
                InterferenceNode u = adjacencyList.get(uu);
                u.setMcsWeight(u.getMcsWeight() + 1);
            }
            //Set W ← W \ {v}
            w.remove(v);
        }
        return viList;
    }

    /**
     * Colors using minimal Colors.
     * @return maximal color used.
     */
    public int doColoring() {
        int maxColor = 1;
        int n = adjacencyList.size();
        List <InterferenceNode> viList = doMCS();
        //For i ← 1 to n do
        for (int i = 0; i < n ; i++) {
            //Let c be the lowest color not used in N (vi)
            InterferenceNode vi = viList.get(i);
            Set<Integer> usedColors = new HashSet<>();
            for (InterferenceLabel uu : adjacencyList.get(new InterferenceLabel(vi.getReg().getNumber())).getAdjacent()) {
                InterferenceNode u = adjacencyList.get(uu);
                if (u.getColor() != 0) {
                    usedColors.add(u.getColor());
                }
            }
            int color = 1;  //0: uncolored
            while (usedColors.contains(color)) {
                color++;
                maxColor = Math.max(maxColor, color);
            }
            //Set col(vi) ← c
            vi.setColor(color);
        }
        return maxColor;
    }

}
