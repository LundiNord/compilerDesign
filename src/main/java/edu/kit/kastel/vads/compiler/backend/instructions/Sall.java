package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public class Sall extends AsInstruction {

    protected final Register destination;

    public Sall(Register destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
       return String.format("sall %%cl, %s", destination.toString());
    }

    public Register getDestination() {
        return destination;
    }
    @Nullable
    public Register getSource() {
        return destination;
    }

    public void changeSource(Register reg) {
        throw new UnsupportedOperationException("Sall does not have a source register " + reg);
    }

}
