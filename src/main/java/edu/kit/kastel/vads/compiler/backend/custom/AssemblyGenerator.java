package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.aasm.AasmRegisterAllocator;
import edu.kit.kastel.vads.compiler.backend.aasm.CodeGenerator;
import edu.kit.kastel.vads.compiler.backend.instructions.Addl;
import edu.kit.kastel.vads.compiler.backend.instructions.AsInstruction;
import edu.kit.kastel.vads.compiler.backend.instructions.Movel;
import edu.kit.kastel.vads.compiler.backend.instructions.MovlConst;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;
import org.jspecify.annotations.Nullable;

import java.util.*;

import static edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter.print;
import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class AssemblyGenerator {

    private ArrayList<AsInstruction> assemblyCode;
    private static final String template = """
            .global main
            .global _main
            .text
            main:
            call _main
            
            movq %rax, %rdi
            movq $0x3C, %rax
            syscall
            
            _main:

            """;

    private int nextRegister = 8;

    public AssemblyGenerator() {
        assemblyCode = new ArrayList<AsInstruction>();
    }

    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();
        //assemblyCode.append(template);

        System.out.println("-----------------");
        System.out.print(print(program));
        System.out.println();
        System.out.println("-----------------");

        Node endNode = program.endBlock();
        Node returnNode = endNode.predecessors().getFirst();
        maxMunch(returnNode);

        return assemblyCode.stream()
                .map(AsInstruction::toString)
                .reduce(template, (acc, instruction) -> acc + instruction + "\n");
    }


    @Nullable
    private Register maxMunch(Node node) {

        switch (node) {

            case ConstIntNode constIntNode -> {
                Register dest = getFreshRegister();
                assemblyCode.add(new MovlConst(constIntNode.value(), dest));
                //assemblyCode.append("MOVL ").append("$0x").append(constIntNode.value()).append(", ").append(dest).append("\n");
                return dest;
            }

            case AddNode add -> {
                Node successor1 = add.predecessors().get(0);
                Node successor2 = add.predecessors().get(1);
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = getFreshRegister();
                //move succ1 into fresh dest
                //add succ2 to dest
                assemblyCode.add(new Movel(succ1, dest));
                assert succ2 != null;
                assemblyCode.add(new Addl(succ2, dest));
                //assemblyCode.append("MOVL ").append(succ1).append(", ").append(dest).append("\n");
                //assemblyCode.append("ADDL ").append(succ2).append(", ").append(dest).append("\n");
                return dest;
            }
            case ReturnNode returnNode -> {
                Register succ = null;
                for (Node predecessor : returnNode.predecessors()) {
                    if (predecessor.getClass() == ProjNode.class) {     //get rid of project node
                        continue;
                    }
                    succ = maxMunch(predecessor);
                }
                //succ = succ.replace("d", "");
                //return value should be in %rax

                assemblyCode.add(new Movel(succ, new InfiniteRegister("%eax", false)));
                //assemblyCode.append("MOVL ").append(succ).append(", ").append("%eax").append("\n");
                return null;
            }
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private Register getFreshRegister() {
        String nextRegisterString = "%r" + nextRegister;
        nextRegister++;
        return new InfiniteRegister(nextRegisterString, true);
    }
}
