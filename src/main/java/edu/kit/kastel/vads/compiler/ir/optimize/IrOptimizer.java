package edu.kit.kastel.vads.compiler.ir.optimize;

import edu.kit.kastel.vads.compiler.ir.IrGraph;
import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.Block;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;

import java.util.ArrayList;
import java.util.List;

import static edu.kit.kastel.vads.compiler.Main.DO_OPTIMIZATION;

public class IrOptimizer {

    private final List<Node> constEvalQueue;

    public IrOptimizer(){
        constEvalQueue = new ArrayList<>();
    }

    public List<IrGraph> optimize(List<IrGraph> programs) {
        if (!DO_OPTIMIZATION) {
            return programs;
        }
        IrGraph program = programs.getFirst();
        //init queue
        for (Node node : program.getNodes()) {
            if (node instanceof ConstIntNode) {
                constEvalQueue.add(node);
            }
        }
        //work upwards
        while (constEvalQueue.size() > 1) {
            Node node = constEvalQueue.removeFirst();
            for (Node succ : program.successors(node)) {    //ToDo successors not set correctly on new nodes
                //go through all and search for only const children
                if (succ.predecessors().size() < 2) {
                    continue;
                }
                Node one = succ.predecessors().getFirst();
                Node two = succ.predecessors().get(1);
               if (one != null && one instanceof ConstIntNode constOne && two != null && two instanceof ConstIntNode constTwo) {
                   //calculate the result and replace succ with new Const Node
                   final boolean divEdgeCases = constTwo.value() == 0 || (constTwo.value() == -1 && constOne.value() == Integer.MIN_VALUE);
                   switch (succ) {
                       case AddNode addNode -> {
                           int result = constOne.value() + constTwo.value();
                           ConstIntNode newNode = new ConstIntNode(succ.block(), result);
                           program.replaceNode(succ, newNode);
                           constEvalQueue.add(newNode);
                       }
                       case SubNode subNode -> {
                           int result = constOne.value() - constTwo.value();
                           ConstIntNode newNode = new ConstIntNode(succ.block(), result);
                           program.replaceNode(succ, newNode);
                           constEvalQueue.add(newNode);
                       }
                       case MulNode mulNode -> {
                           int result = constOne.value() * constTwo.value();
                           ConstIntNode newNode = new ConstIntNode(succ.block(), result);
                           program.replaceNode(succ, newNode);
                           constEvalQueue.add(newNode);
                       }
                       case ModNode modNode -> {
                           if (divEdgeCases) {
                               continue;
                           }
                           int result = constOne.value() % constTwo.value();
                           ConstIntNode newNode = new ConstIntNode(succ.block(), result);
                           program.replaceNode(succ, newNode);
                           constEvalQueue.add(newNode);
                       }
                       case DivNode divNode -> {
                           if (divEdgeCases) {
                               continue;        //ToDo: only place /0 in end programm
                           }
                           int result = constOne.value() / constTwo.value();
                           ConstIntNode newNode = new ConstIntNode(succ.block(), result);
                           program.replaceNode(succ, newNode);
                           constEvalQueue.add(newNode);
                       }
                       default -> {
                           //do nothing
                       }
                   }
               }
            }
        }
        List<IrGraph> optimizedPrograms = new ArrayList<>();
        optimizedPrograms.add(program);
        return optimizedPrograms;
    }

}
