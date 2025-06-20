package edu.kit.kastel.vads.compiler.parser;

import edu.kit.kastel.vads.compiler.lexer.BooleanLiteral;
import edu.kit.kastel.vads.compiler.lexer.Identifier;
import edu.kit.kastel.vads.compiler.lexer.Keyword;
import edu.kit.kastel.vads.compiler.lexer.KeywordType;
import edu.kit.kastel.vads.compiler.lexer.NumberLiteral;
import edu.kit.kastel.vads.compiler.lexer.Operator;
import edu.kit.kastel.vads.compiler.lexer.Operator.OperatorType;
import edu.kit.kastel.vads.compiler.lexer.Separator;
import edu.kit.kastel.vads.compiler.lexer.Separator.SeparatorType;
import edu.kit.kastel.vads.compiler.Span;
import edu.kit.kastel.vads.compiler.lexer.Token;
import edu.kit.kastel.vads.compiler.parser.ast.AssignmentTree;
import edu.kit.kastel.vads.compiler.parser.ast.BinaryOperationTree;
import edu.kit.kastel.vads.compiler.parser.ast.BlockTree;
import edu.kit.kastel.vads.compiler.parser.ast.ConditionalJumpTree;
import edu.kit.kastel.vads.compiler.parser.ast.DeclarationTree;
import edu.kit.kastel.vads.compiler.parser.ast.ExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.FunctionTree;
import edu.kit.kastel.vads.compiler.parser.ast.IdentExpressionTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueIdentTree;
import edu.kit.kastel.vads.compiler.parser.ast.LValueTree;
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
import edu.kit.kastel.vads.compiler.parser.type.BasicType;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final TokenSource tokenSource;

    public Parser(TokenSource tokenSource) {
        this.tokenSource = tokenSource;
    }

    public ProgramTree parseProgram() {
        ProgramTree programTree = new ProgramTree(List.of(parseFunction()));
        if (this.tokenSource.hasMore()) {
            throw new ParseException("expected end of input but got " + this.tokenSource.peek());
        }
        return programTree;
    }

    private FunctionTree parseFunction() {
        Keyword returnType = this.tokenSource.expectKeyword(KeywordType.INT);
        Identifier identifier = this.tokenSource.expectIdentifier();
        if (!identifier.value().equals("main")) {
            throw new ParseException("expected main function but got " + identifier);
        }
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree body = parseBlock();
        return new FunctionTree(
            new TypeTree(BasicType.INT, returnType.span()),
            name(identifier),
            body
        );
    }

    private BlockTree parseBlock() {
        Separator bodyOpen = this.tokenSource.expectSeparator(SeparatorType.BRACE_OPEN);
        List<StatementTree> statements = new ArrayList<>();
        while (!(this.tokenSource.peek() instanceof Separator sep && sep.type() == SeparatorType.BRACE_CLOSE)) {
            statements.add(parseStatement());
        }
        Separator bodyClose = this.tokenSource.expectSeparator(SeparatorType.BRACE_CLOSE);
        return new BlockTree(statements, bodyOpen.span().merge(bodyClose.span()));
    }

    private StatementTree parseStatement() {
        StatementTree statement;
        if (this.tokenSource.peek().isKeyword(KeywordType.INT) || this.tokenSource.peek().isKeyword(KeywordType.BOOL)) {
            statement = parseDeclaration();
        } else if (this.tokenSource.peek().isKeyword(KeywordType.RETURN)) {
            statement = parseReturn();
        } else if (this.tokenSource.peek().isKeyword(KeywordType.IF)) {
            statement = parseIf();
            return statement;
        }else if (this.tokenSource.peek().isKeyword(KeywordType.ELSE)) {
            this.tokenSource.consume();
            if (this.tokenSource.peek().isKeyword(KeywordType.IF)) {    //else if
                statement = parseIf();
                return statement;
            } else {        //just else
                return parseBlock();        //FixMe
            }
        } else if (this.tokenSource.peek().isKeyword(KeywordType.WHILE)) {
            statement = parseWhile();
            return statement;
        } else if (this.tokenSource.peek().isKeyword(KeywordType.BREAK)) {
            Span span = this.tokenSource.consume().span();
            this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
            return new LoopCtrlTree(true, span);
        } else if (this.tokenSource.peek().isKeyword(KeywordType.CONTINUE)) {
            Span span = this.tokenSource.consume().span();
            this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
            return new LoopCtrlTree(false, span);
        } else if (this.tokenSource.peek().isKeyword(KeywordType.FOR)) {
            statement = parseForLoop();
            return statement;
        } else if (this.tokenSource.peek().isSeparator(SeparatorType.BRACE_OPEN)) {
            BlockTree body = parseBlock();

            return body;    //ToDo wrap in block node
        } else {
            statement = parseSimple();
        }
        this.tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        return statement;
    }

    private StatementTree parseDeclaration() {
        Keyword type = this.tokenSource.expectKeyword(KeywordType.INT, KeywordType.BOOL);
        Identifier ident = this.tokenSource.expectIdentifier();
        ExpressionTree expr = null;
        if (this.tokenSource.peek().isOperator(OperatorType.ASSIGN)) {
            this.tokenSource.expectOperator(OperatorType.ASSIGN);
            expr = parseExpression();
        }
        if (type.type() == KeywordType.INT) {
            return new DeclarationTree(new TypeTree(BasicType.INT, type.span()), name(ident), expr);
        } else if (type.type() == KeywordType.BOOL) {
            return new DeclarationTree(new TypeTree(BasicType.BOOLEAN, type.span()), name(ident), expr);
        } else {
            throw new ParseException("expected int or bool but got " + type);
        }
    }

    private StatementTree parseSimple() {
        LValueTree lValue = parseLValue();
        Operator assignmentOperator = parseAssignmentOperator();
        ExpressionTree expression = parseExpression();
        return new AssignmentTree(lValue, assignmentOperator, expression);
    }

    private Operator parseAssignmentOperator() {
        if (this.tokenSource.peek() instanceof Operator op) {
            return switch (op.type()) {
                case ASSIGN, ASSIGN_DIV, ASSIGN_MINUS, ASSIGN_MOD, ASSIGN_MUL, ASSIGN_PLUS, LEFT_SHIFT_ASSIGN, RIGHT_SHIFT_ASSIGN -> {
                    this.tokenSource.consume();
                    yield op;
                }
                default -> throw new ParseException("expected assignment but got " + op.type());
            };
        }
        throw new ParseException("expected assignment but got " + this.tokenSource.peek());
    }

    private LValueTree parseLValue() {
        if (this.tokenSource.peek().isSeparator(SeparatorType.PAREN_OPEN)) {
            this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
            LValueTree inner = parseLValue();
            this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
            return inner;
        }
        Identifier identifier = this.tokenSource.expectIdentifier();
        return new LValueIdentTree(name(identifier));
    }

    private StatementTree parseReturn() {
        Keyword ret = this.tokenSource.expectKeyword(KeywordType.RETURN);
        ExpressionTree expression = parseExpression();
        return new ReturnTree(expression, ret.span().start());
    }

    private ExpressionTree parseExpression() {
        ExpressionTree lhs = parseTerm();
        while (true) {
            if (this.tokenSource.peek() instanceof Operator(var type, _)
                && (type == OperatorType.PLUS || type == OperatorType.MINUS
                || type == OperatorType.LEFT_SHIFT || type == OperatorType.RIGHT_SHIFT
                || type == OperatorType.BITWISE_EXCLUSIVE_OR
                || type == OperatorType.GREATER_THAN || type == OperatorType.LESS_THAN
                || type == OperatorType.GREATER_THAN_OR_EQUAL || type == OperatorType.LESS_THAN_OR_EQUAL
                || type == OperatorType.EQUALS || type == OperatorType.NOT_EQUALS
                || type == OperatorType.BITWISE_AND || type == OperatorType.BITWISE_OR
                || type == OperatorType.LOGICAL_AND || type == OperatorType.LOGICAL_OR
            )) {
                this.tokenSource.consume();
                lhs = new BinaryOperationTree(lhs, parseTerm(), type);
            } else {
                return lhs;
            }
        }
    }

    private ExpressionTree parseTerm() {
        ExpressionTree lhs = parseFactor();
        while (true) {
            if (this.tokenSource.peek() instanceof Operator(var type, _)
                && (type == OperatorType.MUL || type == OperatorType.DIV || type == OperatorType.MOD)) {
                this.tokenSource.consume();
                lhs = new BinaryOperationTree(lhs, parseFactor(), type);
            } else {
                return lhs;
            }
        }
    }

    private ExpressionTree parseFactor() {
        return switch (this.tokenSource.peek()) {
            case Separator(var type, _) when type == SeparatorType.PAREN_OPEN -> {
                this.tokenSource.consume();
                ExpressionTree expression = parseExpression();
                this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
                yield expression;
            }
            case Operator(var type, _) when type == OperatorType.MINUS -> {
                Span span = this.tokenSource.consume().span();
                yield new NegateTree(parseFactor(), span);
            }
            case Identifier ident -> {
                this.tokenSource.consume();
                yield new IdentExpressionTree(name(ident));
            }
            case NumberLiteral(String value, int base, Span span) -> {
                this.tokenSource.consume();
                yield new LiteralTree(value, base, span);
            }
            case BooleanLiteral(boolean value, Span span) -> {
                this.tokenSource.consume();
                yield new LiteralTree(String.valueOf(value), 0, span);
            }
            case Token t -> throw new ParseException("invalid factor " + t);
        };
    }

    private static NameTree name(Identifier ident) {
        return new NameTree(Name.forIdentifier(ident), ident.span());
    }

    private StatementTree parseIf() {
        this.tokenSource.consume();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        ExpressionTree expression = parseExpression();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree block = parseBlock();
        return new ConditionalJumpTree(expression, block, expression.span().merge(block.span()));
    }
    private StatementTree parseWhile() {
        this.tokenSource.consume();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);
        ExpressionTree expression = parseExpression();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree block = parseBlock();
        return new WhileTree(expression, block, expression.span().merge(block.span()), null);
    }
    private StatementTree parseForLoop() {
        Span start = this.tokenSource.consume().span();
        this.tokenSource.expectSeparator(SeparatorType.PAREN_OPEN);

        List<Tree> statements = new ArrayList<>(3);
        statements.add(parseStatement());
        statements.add(parseExpression());
        tokenSource.expectSeparator(SeparatorType.SEMICOLON);
        statements.add(parseSimple());

        this.tokenSource.expectSeparator(SeparatorType.PAREN_CLOSE);
        BlockTree block = parseBlock();
        return new WhileTree(null, block, start.merge(block.span()), statements);
    }

}
