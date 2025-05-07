package edu.kit.kastel.vads.compiler.backend.regalloc;

public class InterferenceNode {

    private final Register reg;
    private int mcsWeight;          //Maximum Cardinality Search Weight
    private int color;

    InterferenceNode(Register reg) {
        this.reg = reg;
        mcsWeight = 0;
        color = 0;      //0: no color
    }

    @Override
    public int hashCode() {
        return reg.hashCode();
    }
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof InterferenceNode interferenceNode) {
            if (this.reg == null) {
                return false;
            }
            return reg.equals(interferenceNode.reg);
        }
        return false;
    }
    public int getMcsWeight() {
        return mcsWeight;
    }
    public void setMcsWeight(int weight) {
        this.mcsWeight = weight;
    }
    public Register getReg() {
        return reg;
    }
    public int getColor() {
        return color;
    }
    public void setColor(int color) {
        this.color = color;
    }

}
