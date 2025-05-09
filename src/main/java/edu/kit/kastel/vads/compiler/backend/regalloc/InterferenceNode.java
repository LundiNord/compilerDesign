package edu.kit.kastel.vads.compiler.backend.regalloc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Node in a {@link InterferenceGraph}.
 * @author ujiqk
 * @version 1.0 */
public class InterferenceNode {

    private final InfiniteRegister reg;
    private int mcsWeight;          //Maximum Cardinality Search Weight
    private int color;
    private final HashSet<InterferenceLabel> adjacent;

    InterferenceNode(InfiniteRegister reg) {
        this.reg = reg;
        mcsWeight = 0;
        color = 0;      //0: no color
        adjacent = new HashSet<>();
    }
    public List<InterferenceLabel> getAdjacent() {
        return adjacent.stream().toList();
    }
    public void addAdjacent(InterferenceLabel label) {
        adjacent.add(label);
    }
    public int getMcsWeight() {
        return mcsWeight;
    }
    public void setMcsWeight(int weight) {
        this.mcsWeight = weight;
    }
    public InfiniteRegister getReg() {
        return reg;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }

}
