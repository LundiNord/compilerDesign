package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class Label extends AsInstruction {
    private final String name;

    public Label(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name + ":";
    }

    @Override
    @Nullable
    public Register getDestination() {
        return null;
    }

    @Override
    @Nullable
    public Register getSource() {
        return null;
    }

    public String getName() {
        return name;
    }
}