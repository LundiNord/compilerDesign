package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class Movel implements AsInstruction {

    @Nullable
    private final Register source;
    protected final Register destination;

    public Movel(@Nullable  Register source, Register destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public String toString() {
        assert source != null;
        return String.format("movl %s, %s", source.toString(), destination.toString());
    }
    public Register getDestination() {
        return destination;
    }
}
