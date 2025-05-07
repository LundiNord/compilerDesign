package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.*;

public class InterferenceGraph {
    //https://www.baeldung.com/java-graphs

    private final Map<InterferenceNode, List<InterferenceNode>> adjacencyList;

    public InterferenceGraph() {
        adjacencyList = new HashMap<>();
    }

    public void addVertex(Register label) {
        if (label == null){
            throw new IllegalArgumentException("Label cannot be null");
        }
        adjacencyList.putIfAbsent(new InterferenceNode(label), new ArrayList<>());
    }
//    public void removeVertex(Register label) {
//        InterferenceNode v = new InterferenceNode(label);
//        adjacencyList.values().forEach(e -> e.remove(v));
//        adjacencyList.remove(new InterferenceNode(label));
//    }
    //Helper method to get the actual node instance from the map
    public InterferenceNode getNode(Register label) {      //hope this is ok for performance
        InterferenceNode key = new InterferenceNode(label);
        return Objects.requireNonNull(adjacencyList.keySet().stream()
                .filter(key::equals)
                .findFirst()
                .orElse(null));
    }
    public void addEdge(Register label1, Register label2) {
        InterferenceNode v1 = getNode(label1);
        InterferenceNode v2 =  getNode(label2);
        if (!adjacencyList.get(v1).contains(v2)) {
            adjacencyList.get(v1).add(v2);
        }
        if (!adjacencyList.get(v2).contains(v1)) {
            adjacencyList.get(v2).add(v1);
        }
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
        return new ArrayList<>(adjacencyList.keySet());
    }

    private List<InterferenceNode> doMCS() {
        // |V| = n
        int n = adjacencyList.size();
        //For all v ∈ V set wt(v) ← 0
        //done
        //Let W ← V
        List<InterferenceNode> w = new ArrayList<>(adjacencyList.keySet());
        w.sort(Comparator.comparingInt(InterferenceNode::getMcsWeight).reversed());
        List <InterferenceNode> viList = new ArrayList<>();
        //For i ← 1 to n do
        for (int i = 1; i <= n; i++) {
            //Let v be a node of maximal weight in W
            InterferenceNode v = w.getFirst();
            //Set vi ← v
            viList.add(v);
            //For all u ∈ W ∩ N (v) set wt(u) ← wt(u) + 1
            for (InterferenceNode u : adjacencyList.get(v)) {
                u.setMcsWeight(u.getMcsWeight() + 1);
            }
            //Set W ← W \ {v}
            w.remove(v);
        }
        return viList;
    }

    public int doColoring() {
        int maxColor = 1;
        int n = adjacencyList.size();
        List <InterferenceNode> viList = doMCS();
        //For i ← 1 to n do
        for (int i = 0; i < n ; i++) {
            //Let c be the lowest color not used in N (vi)
            InterferenceNode vi = viList.get(i);
            Set<Integer> usedColors = new HashSet<>();
            for (InterferenceNode u : adjacencyList.get(vi)) {
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
