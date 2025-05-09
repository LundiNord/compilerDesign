package edu.kit.kastel.vads.compiler.backend.regalloc;

public class InterferenceLabel {

    private final int number;

    public InterferenceLabel(int number) {
        this.number = number;
    }

    @Override
    public int hashCode() {
        return number;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof InterferenceLabel interferenceLabel) {
            return number == (interferenceLabel.number);
        }
        return false;
    }

}
