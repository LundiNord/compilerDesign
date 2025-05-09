package edu.kit.kastel.vads.compiler.backend.custom;

import edu.kit.kastel.vads.compiler.backend.instructions.*;
import edu.kit.kastel.vads.compiler.backend.regalloc.Register;
import edu.kit.kastel.vads.compiler.backend.regalloc.RegisterAlloc;
import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.*;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AssemblyGenerator {

    private List<AsInstruction> assemblyCode;
    private final Set<Node> visited;
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
                pushq %rbp
                movq %rsp, %rbp

            """;
    private static final String endTemplate = """
            movq %rbp, %rsp
            popq %rbp
            ret
            """;
    private int nextRegister = 1;

    public AssemblyGenerator() {
        assemblyCode = new ArrayList<AsInstruction>();
         visited = new HashSet<Node>();
    }

    public String generateCode(List<IrGraph> programs) {
        IrGraph program = programs.getFirst();

//        System.out.println("-----------------");
//        System.out.print(print(program));
//        System.out.println();
//        System.out.println("-----------------");

        Node endNode = program.endBlock();
        Node returnNode = endNode.predecessors().getFirst();
        maxMunch(returnNode);

        RegisterAlloc regAlloc = new RegisterAlloc(assemblyCode);
        assemblyCode = regAlloc.doRegAlloc();
        assemblyCode = regAlloc.removeMemToMem();

        String result = assemblyCode.stream()
                .map(AsInstruction::toString)
                .reduce(template, (acc, instruction) -> acc + instruction + "\n");
        return result + endTemplate;
    }


    @Nullable
    private Register maxMunch(Node node) {
        boolean alreadyVisited = !visited.add(node);
        if (alreadyVisited) {
            return node.getDestination();  //Could be changes to only store dest register
        }
        switch (node) {
            case ModNode mod -> {
                Node successor1 = mod.predecessors().get(0);    //oberer -> dividend
                Node successor2 = mod.predecessors().get(1);    //unterer -> divisor
                Register succ1 = maxMunch(successor1);      //ToDo: change maxMunch to give in desired dest register
                Register succ2 = maxMunch(successor2);
                Register dest = new StandardRegister("%edx", false);
                Register dividend = new StandardRegister("%eax", false); //dividend
                assemblyCode.add(new Movel(succ1, dividend));
                assert succ2 != null;

                // Add runtime check for INT_MIN / -1
                Label skipSpecialCase = new Label("skip_special_case_" + nextRegister++);
                // Check if divisor is -1
                assemblyCode.add(new CmplConst(-1, succ2));
                assemblyCode.add(new Jne(skipSpecialCase));
                // Check if dividend is INT_MIN
                assemblyCode.add(new CmplConst(Integer.MIN_VALUE, dividend));
                assemblyCode.add(new Jne(skipSpecialCase));
                // Throw exception by dividing by zero
                assemblyCode.add(new MovlConst(0, succ2));

                assemblyCode.add(skipSpecialCase);
                assemblyCode.add(new MovlConst(0, dest));   //replace with xor for performance?
                Mod modl = new Mod(succ2);
                assemblyCode.add(modl);

                Register destination = getFreshRegister();
                assemblyCode.add(new Movel(dest, destination));
                node.setDestination(destination);
                return destination;
            }
            case DivNode div -> {
                Node successor1 = div.predecessors().get(0);    //oberer -> dividend
                Node successor2 = div.predecessors().get(1);    //unterer -> divisor
                Node sideEffect = div.predecessors().get(2); //side effect
                maxMunch(sideEffect);
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = new StandardRegister("%eax", false);    //dividend
                assemblyCode.add(new Movel(succ1, dest));       //ToDo: change maxMunch to give in desired dest register
                assert succ2 != null;

                // Add runtime check for INT_MIN / -1
                Label skipSpecialCase = new Label("skip_special_case_" + nextRegister++);
                // Check if divisor is -1
                assemblyCode.add(new CmplConst(-1, succ2));
                assemblyCode.add(new Jne(skipSpecialCase));
                // Check if dividend is INT_MIN
                assemblyCode.add(new CmplConst(Integer.MIN_VALUE, dest));
                assemblyCode.add(new Jne(skipSpecialCase));
                // Throw exception by dividing by zero
                assemblyCode.add(new MovlConst(0, succ2));

                assemblyCode.add(skipSpecialCase);
                assemblyCode.add(new MovlConst(0, new StandardRegister("%edx", false)));   //replace with xor for performance?
                Div divl = new Div(succ2);
                assemblyCode.add(divl);

                Register destination = getFreshRegister();
                assemblyCode.add(new Movel(dest, destination));
                node.setDestination(destination);
                return destination;
            }
            case ConstIntNode constIntNode -> {
                Register dest = getFreshRegister();
                MovlConst movlConst = new MovlConst(constIntNode.value(), dest);
                assemblyCode.add(movlConst);
                node.setDestination(dest);
                return dest;
            }
            case SubNode sub -> {
                Node successor1 = sub.predecessors().get(0);
                Node successor2 = sub.predecessors().get(1);
                Register succ1 = maxMunch(successor1);
                Register succ2 = maxMunch(successor2);
                Register dest = getFreshRegister();
                //move succ1 into fresh dest
                //add succ2 to dest
                assemblyCode.add(new Movel(succ1, dest));
                assert succ1 != null;
                assert succ2 != null;
                Subl subl = new Subl(succ2, dest);
                assemblyCode.add(subl);
                node.setDestination(dest);
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
                assert succ1 != null;
                assert succ2 != null;
                Addl addl = new Addl(succ2, dest);
                assemblyCode.add(addl);
                node.setDestination(dest);
                return dest;
            }
            case MulNode mul -> {
                Node successor1 = mul.predecessors().get(0);
                Node successor2 = mul.predecessors().get(1);
                Register succ1 = maxMunch(successor1);  //ToDo: change maxMunch to give in desired dest register
                Register succ2 = maxMunch(successor2);
                Register dest = new StandardRegister("%eax", false);    //dividend
                assemblyCode.add(new Movel(succ1, dest));
                assert succ2 != null;
                Mul mull = new Mul(succ2);
                assemblyCode.add(mull);

                Register destination = getFreshRegister();
                assemblyCode.add(new Movel(dest, destination));
                node.setDestination(destination);
                return destination;
            }
            case ReturnNode returnNode -> {
                Register succ = null;
                for (Node predecessor : returnNode.predecessors()) {
                    if (predecessor.getClass() == ProjNode.class && ((ProjNode) predecessor).info().equals("SIDE_EFFECT")) {
                        maxMunch(node); //calculate value and don't store it
                    }
                    succ = maxMunch(predecessor);
                }
                assert succ != null;
                //succ = succ.replace("d", "");
                //return value should be in %rax
                assemblyCode.add(new Movel(succ, new StandardRegister("%eax", false)));
                return null;
            }
            case ProjNode projNode -> {
                if (projNode.info().equals("RESULT")) {
                    Register reg = maxMunch(projNode.predecessors().getFirst());
                    assert reg != null;
                    node.setDestination(reg);
                    return reg;
                } else if (projNode.info().equals("SIDE_EFFECT")) {
                    // do nothing
                    Node successor1 = projNode.predecessors().getFirst();
                    Register reg = maxMunch(successor1);    //bei START hier null
                    node.setDestination(reg);
                    return null;
                } else {
                   throw new IllegalStateException("Unexpected value: " + projNode.info());
                }
            }
            case Block _, StartNode _ -> {
                // do nothing, skip line break
                return null;
            }
            default -> throw new IllegalStateException("Unexpected value: " + node);
        }
    }

    private Register getFreshRegister() {
        return new InfiniteRegister(nextRegister++);
    }

}
