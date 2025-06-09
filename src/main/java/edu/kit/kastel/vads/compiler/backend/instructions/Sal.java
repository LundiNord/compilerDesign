package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class Sal extends AsInstruction {
    //https://en.wikibooks.org/wiki/X86_Assembly/Shift_and_Rotate#Arithmetic_Shift_Instructions

    protected final Register destination;
    private final int amount;

    /**
     * Arithmetic shift to left.
     * @param destination Destination register
     */
    public Sal(int amount, Register destination) {
        this.amount = amount;
        this.destination = destination;
    }

    @Override
    public String toString() {
        return String.format("shl $%d, %s", amount, destination.toString());
    }

    public Register getDestination() {
        return destination;
    }
    @Nullable
    public Register getSource() {
        return destination;
    }
    @Override
    public void changeSource(Register reg) {
        throw new UnsupportedOperationException("Sal does not have a source register " + reg);
    }
}
