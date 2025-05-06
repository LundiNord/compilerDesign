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

import static edu.kit.kastel.vads.compiler.ir.util.GraphVizPrinter.print;
import static edu.kit.kastel.vads.compiler.ir.util.NodeSupport.predecessorSkipProj;

public class AssemblyGenerator {

    private StringBuilder assemblyCode;
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
        assemblyCode = new StringBuilder();
    }

    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();
        assemblyCode = new StringBuilder();
        assemblyCode.append(template);

        System.out.println("-----------------");
        System.out.print(print(program));
        System.out.println();
        System.out.println("-----------------");

        Node endNode = program.endBlock();
        Node returnNode = endNode.predecessors().getFirst();
        maxMunch(returnNode);

        return assemblyCode.toString();
    }


    private String maxMunch(Node node) {

        switch (node) {

            case ConstIntNode constIntNode -> {
                String dest = getFreshRegister() + "d";
                assemblyCode.append("MOVL ").append("$0x").append(constIntNode.value()).append(", ").append(dest).append("\n");
                return dest;
            }

            case AddNode add -> {
                Node successor1 = add.predecessors().get(0);
                Node successor2 = add.predecessors().get(1);
                String succ1 = maxMunch(successor1);
                String succ2 = maxMunch(successor2);
                String dest = getFreshRegister() + "d";
                //move succ1 into fresh dest
                //add succ2 to dest
                assemblyCode.append("MOVL ").append(succ1).append(", ").append(dest).append("\n");
                assemblyCode.append("ADDL ").append(succ2).append(", ").append(dest).append("\n");
                return dest;
            }
            case ReturnNode returnNode -> {
                String succ = "";
                for (Node predecessor : returnNode.predecessors()) {
                    succ = succ + maxMunch(predecessor);
                }
                //succ = succ.replace("d", "");
                //return value should be in %rax
                assemblyCode.append("MOVL ").append(succ).append(", ").append("%eax").append("\n");
                return "";
            }
            case Block _, ProjNode _, StartNode _ -> {
                // do nothing, skip line break
                return "";
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private String getFreshRegister() {
        String nextRegisterString = "%r" + nextRegister;
        nextRegister++;
        return nextRegisterString;
    }
}
