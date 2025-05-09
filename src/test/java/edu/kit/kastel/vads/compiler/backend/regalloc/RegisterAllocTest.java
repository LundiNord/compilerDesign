package edu.kit.kastel.vads.compiler.backend.regalloc;

import edu.kit.kastel.vads.compiler.backend.instructions.Addl;
import edu.kit.kastel.vads.compiler.backend.instructions.AsInstruction;
import edu.kit.kastel.vads.compiler.backend.instructions.MovlConst;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class RegisterAllocTest {

    @Test
    void doRegAlloc() {
        List<AsInstruction> assemblyCode = new ArrayList<>();
        Register xEins = new InfiniteRegister(1);
        MovlConst line1 = new MovlConst(1, xEins);
        Addl line2 = new Addl(xEins, xEins);
        Addl line3 = new Addl(xEins, xEins);


    }



}
