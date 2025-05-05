package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.aasm.AasmRegisterAllocator;
import edu.kit.kastel.vads.compiler.backend.aasm.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class AssemblyGenerator {


    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();

        //Set<Node> visited = new HashSet<>();
        //scan(program.startBlock(), visited);







        return "";
    }


    private void scan(Node node, Set<Node> visited) {
        System.out.println(node);

        
        switch (node) {

            case AddNode add -> {

            }

            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return;
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }

    }


}
