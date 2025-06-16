package edu.kit.kastel.vads.compiler.ir;

import edu.kit.kastel.vads.compiler.ir.node.AddNode;
import edu.kit.kastel.vads.compiler.ir.node.BitwiseCompNode;
import edu.kit.kastel.vads.compiler.ir.node.CompNode;
import edu.kit.kastel.vads.compiler.ir.node.ConstIntNode;
import edu.kit.kastel.vads.compiler.ir.node.DivNode;
import edu.kit.kastel.vads.compiler.ir.node.EndNode;
import edu.kit.kastel.vads.compiler.ir.node.IfNode;
import edu.kit.kastel.vads.compiler.ir.node.ModNode;
import edu.kit.kastel.vads.compiler.ir.node.MulNode;
import edu.kit.kastel.vads.compiler.ir.node.Node;
import edu.kit.kastel.vads.compiler.ir.node.PhiNode;
import edu.kit.kastel.vads.compiler.ir.node.ReturnNode;
import edu.kit.kastel.vads.compiler.ir.node.Scope;
import edu.kit.kastel.vads.compiler.ir.node.ShiftNode;
import edu.kit.kastel.vads.compiler.ir.node.StartNode;
import edu.kit.kastel.vads.compiler.ir.node.SubNode;
import edu.kit.kastel.vads.compiler.ir.node.WhileNode;
import edu.kit.kastel.vads.compiler.ir.optimize.Optimizer;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfo;
import edu.kit.kastel.vads.compiler.ir.util.DebugInfoHelper;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.ConditionalJumpTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LiteralTree;
import edu.kit.kastel.vads.compiler.parser.ast.LoopCtrlTree;
import edu.kit.kastel.vads.compiler.parser.ast.NameTree;
import edu.kit.kastel.vads.compiler.parser.ast.NegateTree;
import edu.kit.kastel.vads.compiler.parser.ast.ProgramTree;
import edu.kit.kastel.vads.compiler.parser.ast.ReturnTree;
import edu.kit.kastel.vads.compiler.parser.ast.StatementTree;
import edu.kit.kastel.vads.compiler.parser.ast.Tree;
import edu.kit.kastel.vads.compiler.parser.ast.TypeTree;
import edu.kit.kastel.vads.compiler.parser.ast.WhileTree;
import edu.kit.kastel.vads.compiler.parser.symbol.Name;
import edu.kit.kastel.vads.compiler.parser.visitor.Visitor;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BinaryOperator;

/// SSA translation as described in
/// [`Simple and Efficient Construction of Static Single Assignment Form`](https://compilers.cs.uni-saarland.de/papers/bbhlmz13cc.pdf).
///
/// This implementation also tracks side effect edges that can be used to avoid reordering of operations that cannot be
/// reordered.
///
/// We recommend reading the paper to better understand the mechanics implemented here.
public class SsaTranslation {
    private final FunctionTree function;
    @Nullable
    protected Node startNode;
    protected EndNode endNode;
    private final ArrayDeque<Scope> scopes;
    @Nullable
    protected Node currentCtrlNode;

    public SsaTranslation(FunctionTree function, Optimizer optimizer) {
        this.function = function;
        this.scopes = new ArrayDeque<>();
        this.scopes.add(new Scope());
        this.endNode = new EndNode();
    }

    public IrGraph translate() {
        var visitor = new SsaTranslationVisitor();
        this.function.accept(visitor, this);
        assert startNode != null;
        return new IrGraph(function.name().toString(), startNode, endNode);
    }

    private void writeVariable(Name variable, Node value) {
        scopes.getLast().addVariable(variable, value);
    }

    @Nullable
    private Node readVariable(Name variable) {
        for (Iterator<Scope> it = scopes.descendingIterator(); it.hasNext(); ) {
            Scope scope = it.next();
            Node value = scope.getVariable(variable);
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private static class SsaTranslationVisitor implements Visitor<SsaTranslation, Optional<Node>> {

        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        private static final Optional<Node> NOT_AN_EXPRESSION = Optional.empty();

        private final Deque<DebugInfo> debugStack = new ArrayDeque<>();

        private void pushSpan(Tree tree) {
            this.debugStack.push(DebugInfoHelper.getDebugInfo());
            DebugInfoHelper.setDebugInfo(new DebugInfo.SourceInfo(tree.span()));
        }

        private void popSpan() {
            DebugInfoHelper.setDebugInfo(this.debugStack.pop());
        }

        @Override
        public Optional<Node> visit(AssignmentTree assignmentTree, SsaTranslation data) {
            pushSpan(assignmentTree);
            BinaryOperator<Node> desugar = switch (assignmentTree.operator().type()) {
                case ASSIGN_MINUS -> SubNode::new;
                case ASSIGN_PLUS -> AddNode::new;
                case ASSIGN_MUL -> MulNode::new;
                case ASSIGN_DIV -> (lhs, rhs) -> projResultDivMod(data, new DivNode(lhs, rhs));
                case ASSIGN_MOD -> (lhs, rhs) -> projResultDivMod(data, new ModNode(lhs, rhs));
                case LEFT_SHIFT_ASSIGN -> (lhs, rhs) -> new ShiftNode(lhs, rhs, ShiftNode.Shift.LEFT);
                case RIGHT_SHIFT_ASSIGN -> (lhs, rhs) -> new ShiftNode(lhs, rhs, ShiftNode.Shift.RIGHT);
                case ASSIGN -> null;
                default ->
                    throw new IllegalArgumentException("not an assignment operator " + assignmentTree.operator());
            };

            switch (assignmentTree.lValue()) {
                case LValueIdentTree(var name) -> {
                    Node rhs = assignmentTree.expression().accept(this, data).orElseThrow();
                    if (desugar != null) {
                        rhs = desugar.apply(Objects.requireNonNull(data.readVariable(name.name())), rhs);
                    }
                    data.writeVariable(name.name(), rhs);
                }
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(BinaryOperationTree binaryOperationTree, SsaTranslation data) {
            pushSpan(binaryOperationTree);
            Node lhs = binaryOperationTree.lhs().accept(this, data).orElseThrow();
            Node rhs = binaryOperationTree.rhs().accept(this, data).orElseThrow();
            Node res = switch (binaryOperationTree.operatorType()) {
                case MINUS -> new SubNode(lhs, rhs);
                case PLUS -> new AddNode(lhs, rhs);
                case MUL -> new MulNode(lhs, rhs);
                case DIV -> projResultDivMod(data, new DivNode(lhs, rhs));
                case MOD -> projResultDivMod(data, new ModNode(lhs, rhs));
                case RIGHT_SHIFT -> new ShiftNode(lhs, rhs, ShiftNode.Shift.RIGHT);
                case LEFT_SHIFT  -> new ShiftNode(lhs, rhs, ShiftNode.Shift.LEFT);
                case BITWISE_EXCLUSIVE_OR -> new BitwiseCompNode(lhs, rhs, BitwiseCompNode.BitwiseCompType.XOR);
                case BITWISE_AND -> new BitwiseCompNode(lhs, rhs, BitwiseCompNode.BitwiseCompType.AND);
                case BITWISE_OR -> new BitwiseCompNode(lhs, rhs, BitwiseCompNode.BitwiseCompType.OR);
                case LESS_THAN -> new CompNode(lhs, rhs, CompNode.CompType.LESS_THAN);
                case LESS_THAN_OR_EQUAL -> new CompNode(lhs, rhs, CompNode.CompType.LESS_THAN_OR_EQUALS);
                case GREATER_THAN -> new CompNode(lhs, rhs, CompNode.CompType.GREATER_THAN);
                case GREATER_THAN_OR_EQUAL -> new CompNode(lhs, rhs, CompNode.CompType.GREATER_THAN_OR_EQUALS);
                case EQUALS -> new CompNode(lhs, rhs, CompNode.CompType.EQUALS);
                case NOT_EQUALS -> new CompNode(lhs, rhs, CompNode.CompType.NOT_EQUALS);
                default ->
                    throw new IllegalArgumentException("not a binary expression operator " + binaryOperationTree.operatorType());
            };
            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(BlockTree blockTree, SsaTranslation data) {
            pushSpan(blockTree);
            for (StatementTree statement : blockTree.statements()) {
                if (statement instanceof LoopCtrlTree loopCtrlTree && loopCtrlTree.bbreak()) {
                    break;  //ToDo
                }
                statement.accept(this, data);
                // skip everything after a return in a block
                if (statement instanceof ReturnTree) {
                    break;
                }
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(ConditionalJumpTree conditionalJumpTree, SsaTranslation data) {
            pushSpan(conditionalJumpTree);
            //ToDo
            Node condition = conditionalJumpTree.condition().accept(this, data).orElseThrow();
            Node ifNode = new IfNode(condition);

            data.scopes.add(new Scope());
            conditionalJumpTree.block().accept(this, data);
            //ad phi nodes
            Scope blockScope = data.scopes.getLast();
            data.scopes.removeLast();
            for (Name name : blockScope.getNames()) {
                Node value = blockScope.getVariable(name);
                Node oldValue = data.readVariable(name);
                assert value != null;
                assert oldValue != null;
                Node phiNode = new PhiNode(oldValue, value, ifNode);
                data.scopes.getLast().replaceVariable(name, phiNode);  //ToDo: in nested probably wrong
            }
            //add merge node
            //Node mergeNode = new RegionNode(ifNode, block);

            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(WhileTree whileTree, SsaTranslation data) {
            pushSpan(whileTree);
            //ToDo
            if (whileTree.condition() == null) {    //for loop
                //ToDo
                return NOT_AN_EXPRESSION;
            }

            Node condition = whileTree.condition().accept(this, data).orElseThrow();
            Node whileNode = new WhileNode(condition);
            Node savedCtrlNode = data.currentCtrlNode;
            data.currentCtrlNode = whileNode;
            data.scopes.add(new Scope());
            whileTree.block().accept(this, data);

            Scope blockScope = data.scopes.getLast();
            data.scopes.removeLast();
            for (Name name : blockScope.getNames()) {
                Node value = blockScope.getVariable(name);
                Node oldValue = data.readVariable(name);
                assert value != null;
                assert oldValue != null;
                Node phiNode = new PhiNode(oldValue, value, whileNode);
                data.scopes.getLast().replaceVariable(name, phiNode);  //ToDo: in nested probably wrong
            }

            data.currentCtrlNode = savedCtrlNode;
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(LoopCtrlTree loopCtrlTree, SsaTranslation data) {
            pushSpan(loopCtrlTree);
            //ToDo


            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(DeclarationTree declarationTree, SsaTranslation data) {
            pushSpan(declarationTree);
            if (declarationTree.initializer() != null) {
                Node rhs = declarationTree.initializer().accept(this, data).orElseThrow();
                data.writeVariable(declarationTree.name().name(), rhs);
            }
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(FunctionTree functionTree, SsaTranslation data) {
            pushSpan(functionTree);
            data.startNode = new StartNode();
            //data.constructor.writeCurrentSideEffect(data.constructor.newSideEffectProj(start));
            functionTree.body().accept(this, data);
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(IdentExpressionTree identExpressionTree, SsaTranslation data) {
            pushSpan(identExpressionTree);
            Node value = data.readVariable(identExpressionTree.name().name());
            popSpan();
            return Optional.of(value);
        }

        @Override
        public Optional<Node> visit(LiteralTree literalTree, SsaTranslation data) {
            pushSpan(literalTree);
            Node node = new ConstIntNode((int) literalTree.parseValue().orElseThrow());
            popSpan();
            return Optional.of(node);
        }

        @Override
        public Optional<Node> visit(LValueIdentTree lValueIdentTree, SsaTranslation data) {
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(NameTree nameTree, SsaTranslation data) {
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(NegateTree negateTree, SsaTranslation data) {
            pushSpan(negateTree);
            Node node = negateTree.expression().accept(this, data).orElseThrow();
            Node res = new SubNode(new ConstIntNode(0), node);
            popSpan();
            return Optional.of(res);
        }

        @Override
        public Optional<Node> visit(ProgramTree programTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Node> visit(ReturnTree returnTree, SsaTranslation data) {
            pushSpan(returnTree);
            Node node = returnTree.expression().accept(this, data).orElseThrow();
            ReturnNode ret = new ReturnNode(node);
            ret.addPredecessor(data.currentCtrlNode);
            data.endNode.addPredecessor(ret);
            popSpan();
            return NOT_AN_EXPRESSION;
        }

        @Override
        public Optional<Node> visit(TypeTree typeTree, SsaTranslation data) {
            throw new UnsupportedOperationException();
        }

        private Node projResultDivMod(SsaTranslation data, Node divMod) {
            // make sure we actually have a div or a mod, as optimizations could
            // have changed it to something else already
            if (!(divMod instanceof DivNode || divMod instanceof ModNode)) {
                return divMod;
            }
//            Node projSideEffect = data.constructor.newSideEffectProj(divMod);
//            data.constructor.writeCurrentSideEffect(projSideEffect);
//            return data.constructor.newResultProj(divMod);
            return divMod;
        }
    }

}
