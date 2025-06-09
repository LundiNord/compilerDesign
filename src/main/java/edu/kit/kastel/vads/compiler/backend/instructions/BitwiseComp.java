package edu.kit.kastel.vads.compiler.backend.instructions;

import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import org.jspecify.annotations.Nullable;

public abstract class BitwiseComp extends AsInstruction {

    protected final Register destination;
    protected Register source;

    protected BitwiseComp(Register destination, Register source) {
        this.destination = destination;
        this.source = source;
    }

    public Register getDestination() {
        return destination;
    }
    @Nullable
    public Register getSource() {
        return source;
    }

    public void changeSource(Register reg) {
        this.source = reg;
    }

}
