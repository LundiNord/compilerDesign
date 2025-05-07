package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Addl implements AsInstruction {

    private final Register source;
    protected final Register destination;

    public Addl(Register source, Register destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return String.format("addl %s, %s", source.toString(), destination.toString());
    }

    public Register getDestination() {
        return destination;
    }
}
