package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;

public class Subl implements AsInstruction {

    private final Register source;
    protected final Register destination;

    //https://stackoverflow.com/questions/36569282/x86-assembly-how-does-the-subl-command-work-in-att-syntax

    /**
     * Creates a new subl instruction.
     * Calculates destination - source
     * @param source Source register
     * @param destination Destination register
     */
    public Subl(Register source, Register destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return String.format("subl %s, %s", source.toString(), destination.toString());
    }

    public Register getDestination() {
        return destination;
    }
}
