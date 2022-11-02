package me.shortint.cwlang.parser;

import me.shortint.cwlang.interpreter.Interpreter;
import me.shortint.cwlang.interpreter.types.BooleanValue;
import me.shortint.cwlang.interpreter.types.NumberValue;
import me.shortint.cwlang.interpreter.types.StringValue;
import me.shortint.cwlang.lexer.Token;
import me.shortint.cwlang.lexer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
    private int current;
    private Token[] tokens;
    private String context;
    private static final HashMap<TokenType, OpPrec> operatorPrecedence;

    static {
        operatorPrecedence = new HashMap<>();
        operatorPrecedence.put(TokenType.DOUBLE_EQUALS, new OpPrec(0, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.NOT_EQUALS, new OpPrec(0, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.GREATER, new OpPrec(10, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.LOWER, new OpPrec(10, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.GT_EQUALS, new OpPrec(10, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.LT_EQUALS, new OpPrec(10, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.PLUS, new OpPrec(20, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.MINUS, new OpPrec(20, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.STAR, new OpPrec(30, OpAssoc.LEFT));
        operatorPrecedence.put(TokenType.SLASH, new OpPrec(30, OpAssoc.LEFT));
    }

    public Statement parse(Token[] tokens, String context) {
        current = 0;
        this.tokens = tokens;
        this.context = context;

        ArrayList<Statement> statements = new ArrayList<>();

        while(available())
            statements.add(parseDeclaration());

        return new Statement.CompoundStatement(statements.toArray(new Statement[0]), false);
    }

    /**
     * Parses top level declarations such as events, functions, or global script variable definitions.
     * @return Statement node
     */
    private Statement parseDeclaration() {
        if(match(TokenType.VAR)) return parseVarDeclaration();

        if(match(TokenType.FUNC)) {
            Token function_name = consume(TokenType.IDENTIFIER, "Expected function name after func keyword");
            Expression.FunctionExpression function = parseFunctionBody(function_name.lexeme);
            return new Statement.DeclareVariable(function_name, function);
        }

        if(match(TokenType.EVENT)) return parseEventDeclaration();
        if(match(TokenType.COMMAND)) return parseCommandDeclaration();
        if(match(TokenType.CLASS)) return parseClassStmt();

        throw error(peek(),"Expected declaration at top level");
    }

    private Statement.CommandDeclaration parseCommandDeclaration() {
        Token commandName = consume(TokenType.IDENTIFIER, "Expected command name");
        //consume(TokenType.OPEN_BRACE, "Expected command data");
        // Expression.ObjectConstructor data = parseObject();    old way
        Expression data = parseCall();

        if(!(data instanceof Expression.ObjectConstructor))
            consume(TokenType.SEMICOLON, "Expected ';' after command data");

        return new Statement.CommandDeclaration(commandName, data);
    }

    private Statement.EventDeclaration parseEventDeclaration() {
        Token eventName = consume(TokenType.IDENTIFIER, "Expected event name");
        consume(TokenType.OPEN_PAREN, "Expected event arguments");
        ArrayList<String> arguments = new ArrayList<>();

        if(!check(TokenType.CLOSE_PAREN)) {
            do {
                arguments.add(consume(TokenType.IDENTIFIER, "Expected event argument").lexeme);
            } while(match(TokenType.COMMA));
        }

        consume(TokenType.CLOSE_PAREN, "Expected ')' after event arguments");
        consume(TokenType.OPEN_BRACE, "Expected event body");
        Statement.CompoundStatement body = parseCompoundStatement(false);

        return new Statement.EventDeclaration(eventName, arguments.toArray(new String[0]), body);
    }

    private Statement parseStatement() {
        if(match(TokenType.VAR)) return parseVarDeclaration();
        if(match(TokenType.RETURN)) return parseReturnStmt();
        if(match(TokenType.CLASS)) return parseClassStmt();
        if(match(TokenType.IF)) return parseIfStmt();
        if(match(TokenType.WHILE)) return parseWhileStmt();
        if(match(TokenType.FOR)) return parseForStmt();
        if(match(TokenType.OPEN_BRACE)) return parseCompoundStatement(true);
        return parseExprStmt();
    }

    private Statement parseForStmt() {
        consume(TokenType.OPEN_PAREN, "Expected '(' after for");
        Statement initial;

        if(match(TokenType.VAR)) {
            Token variable = consume(TokenType.IDENTIFIER, "Expected variable name after var keyword");

            // for each loop
            if(match(TokenType.COLON)) {
                Expression iterable = parseExpression();
                consume(TokenType.CLOSE_PAREN, "Expected ')' after for loop");
                Statement body = parseStatement();
                return new Statement.ForEachLoop(variable, iterable, body);
            }

            //consume(TokenType.EQUALS, "Expected initial value for variable in for loop");
            Expression initialValue = match(TokenType.EQUALS) ? parseExpression() : null;
            initial = new Statement.DeclareVariable(variable, initialValue);
            consume(TokenType.SEMICOLON, "Expected ';' after init for statement");
        } else {
            initial = new Statement.ExpressionStatement(parseExpression());
            consume(TokenType.SEMICOLON, "Expected ';' after init for statement");
        }

        Expression test = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after test case in for statement");
        Expression update = parseExpression();
        consume(TokenType.CLOSE_PAREN, "Expected ')' after for loop");
        Statement body = parseStatement();
        return new Statement.ForLoop(initial, test, update, body);
    }

    private Statement.Return parseReturnStmt() {
        Expression value = check(TokenType.SEMICOLON) ? null : parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after return value");
        return new Statement.Return(value);
    }

    private Statement.If parseIfStmt() {
        consume(TokenType.OPEN_PAREN, "Expected '(' after if statement");
        Expression value = parseExpression();
        consume(TokenType.CLOSE_PAREN, "Expected ')' after if statement");
        Statement body = parseStatement();
        Statement elseClause = match(TokenType.ELSE) ? parseStatement() : null;
        return new Statement.If(value, body, elseClause);
    }

    private Statement.While parseWhileStmt() {
        consume(TokenType.OPEN_PAREN, "Expected '(' after while statement");
        Expression value = parseExpression();
        consume(TokenType.CLOSE_PAREN, "Expected ')' after while statement");
        Statement body = parseStatement();
        return new Statement.While(value, body);
    }

    private Statement.CompoundStatement parseCompoundStatement(boolean scoped) {
        ArrayList<Statement> statements = new ArrayList<>();

        while(!check(TokenType.CLOSE_BRACE))
            statements.add(parseStatement());

        consume(TokenType.CLOSE_BRACE, "Expected '}' after compound statement");

        return new Statement.CompoundStatement(statements.toArray(new Statement[0]), scoped);
    }

    private Statement.DeclareVariable parseVarDeclaration() {
        Token variable = consume(TokenType.IDENTIFIER, "Expected variable name after var keyword");
        Expression initial_value = match(TokenType.EQUALS) ? parseExpression() : null;
        consume(TokenType.SEMICOLON, "Expected ';' after variable declaration");
        return new Statement.DeclareVariable(variable, initial_value);
    }

    private Statement.ClassDefinition parseClassStmt() {
        Token className = consume(TokenType.IDENTIFIER, "Expected class name");
        Token parentClassName = match(TokenType.COLON) ? consume(TokenType.IDENTIFIER, "Expected parent class name") : null;
        consume(TokenType.OPEN_BRACE, "Expected class body");
        HashMap<String, Expression> properties = new HashMap<>();
        HashMap<String, Expression> static_properties = new HashMap<>();
        Expression.FunctionExpression constructor = null;
        Statement.CompoundStatement static_constructor = null;

        HashMap<String, Expression> propMap;
        boolean is_static;
        while(!check(TokenType.CLOSE_BRACE)) {
            is_static = match(TokenType.STATIC);
            propMap = is_static ? static_properties : properties;

            if(match(TokenType.VAR)) {
                Token propName = consume(TokenType.IDENTIFIER, "Expected property name");
                Expression value = match(TokenType.EQUALS) ? parseExpression() : null;
                consume(TokenType.SEMICOLON, "Expected ';' after property value");
                propMap.put(propName.lexeme, value);
            } else if(match(TokenType.FUNC)) {
                String function_name = consume(TokenType.IDENTIFIER, "Expected method name").lexeme;
                Expression.FunctionExpression function = parseFunctionBody(function_name);
                propMap.put(function_name, function);
            } else if(match(TokenType.IDENTIFIER)) {
                if (!previous().lexeme.equals(className.lexeme))
                    throw error(peek(), "Expected class method, property or constructor definition");

                constructor = parseFunctionBody(className.lexeme + "_constructor");
            } else if(match(TokenType.OPEN_BRACE)) {
                if(!is_static)
                    throw error(peek(), "Expected class method, property or constructor definition");

                static_constructor = parseCompoundStatement(false);
            } else {
                throw error(peek(), "Expected class method, property or constructor definition");
            }
        }

        consume(TokenType.CLOSE_BRACE, "Expected '}' after class body");
        return new Statement.ClassDefinition(className, parentClassName, constructor, properties, static_constructor, static_properties);
    }

    private Statement parseExprStmt() {
        Expression expression = parseExpression();
        consume(TokenType.SEMICOLON, "Expected ';' after expression statement");
        return new Statement.ExpressionStatement(expression);
    }

    private Expression parseExpression() {
        return parseAssignment();
    }

    private Expression parseAssignment() {
        Expression left = parseIsOperator();

        while(match(TokenType.EQUALS)) {
            if(left instanceof Expression.Variable) {
                Expression value = parseExpression();
                left = new Expression.VariableAssignment(((Expression.Variable)left).token, value);
            } else if(left instanceof Expression.GetProperty) {
                Expression.GetProperty getProperty = (Expression.GetProperty)left;
                left = new Expression.SetProperty(getProperty.propertyHolder, getProperty.propertyName, parseExpression());
            } else if(left instanceof Expression.GetIndex) {
                Expression.GetIndex getIndex = (Expression.GetIndex)left;
                left = new Expression.SetIndex(getIndex.indexValue, getIndex.index, parseExpression(), getIndex.line);
            } else {
                throw error(peek(), "Invalid assignment target");
            }
        }

        return left;
    }

    private Expression parseIsOperator() {
        Expression left = parseBinaryOperation(0);

        if(match(TokenType.IS)) {
            int line = previous().line;
            Expression right = parseBinaryOperation(0);
            left = new Expression.IsInstanceOf(left, right, line);
        }

        return left;
    }

    @SuppressWarnings({"MethodRecursesInfinitely", "InfiniteRecursion"})
    private Expression parseBinaryOperation(int min_precedence) {
        Expression left = parseCall();

        while(true) {
            if(!available()) break;
            Token operator = peek();
            if(!operatorPrecedence.containsKey(operator.type)) break;
            OpPrec data = operatorPrecedence.get(operator.type);
            if(data.prec < min_precedence) break;

            advance(); // consume operator
            int next_precedence = data.assoc == OpAssoc.LEFT ? min_precedence + 1 : min_precedence;

            Expression right = parseBinaryOperation(next_precedence);
            left = new Expression.BinaryExpression(left, right, operator);
        }

        return left;
    }

    private Expression parseCall() {
        Expression left = parsePrimary();

        while(true) {
            if(match(TokenType.OPEN_PAREN)) {
                left = finishFunctionCall(left, previous().line);
            } else if(match(TokenType.DOT)) {
                Token propertyName = consume(TokenType.IDENTIFIER, "Expected property name after '.'");
                left = new Expression.GetProperty(left, propertyName);
            } else if(match(TokenType.OPEN_SQUARE)) {
                int line = previous().line;
                Expression index = parseExpression();
                consume(TokenType.CLOSE_SQUARE, "Expected ']' after indexing expression");
                left = new Expression.GetIndex(left, index, line);
            } else {
                break;
            }
        }

        return left;
    }

    private Expression.FunctionCall finishFunctionCall(Expression callee, int line) {
        ArrayList<Expression> args = new ArrayList<>();

        if(!check(TokenType.CLOSE_PAREN)) {
            do {
                args.add(parseExpression());
            } while(match(TokenType.COMMA));
        }

        consume(TokenType.CLOSE_PAREN, "Expected ')' after function call arguments");

        return new Expression.FunctionCall(callee, args.toArray(new Expression[0]), line);
    }

    private Expression parsePrimary() {
        if(match(TokenType.NUMBER_LITERAL))
            return new Expression.Literal(new NumberValue((double)previous().literal));

        if(match(TokenType.STRING_LITERAL))
            return new Expression.Literal(new StringValue((String)previous().literal));

        if(match(TokenType.TRUE))
            return new Expression.Literal(new BooleanValue(true));

        if(match(TokenType.FALSE))
            return new Expression.Literal(new BooleanValue(false));

        if(match(TokenType.IDENTIFIER))
            return new Expression.Variable(previous());

        if(match(TokenType.NULL))
            return new Expression.Literal(Interpreter.nullValue);

        if(match(TokenType.THIS))
            return new Expression.ThisReference(previous().line);

        if(match(TokenType.SUPER))
            return new Expression.SuperReference(previous().line);

        if(match(TokenType.FUNC)) {
            String function_name = match(TokenType.IDENTIFIER) ? previous().lexeme : "anonymous";
            return parseFunctionBody(function_name);
        }

        if(match(TokenType.OPEN_BRACE))
            return parseObject();

        if(match(TokenType.OPEN_SQUARE))
            return parseList();

        if(match(TokenType.NEW)) {
            int line = previous().line;
            Expression classValue = parsePrimary();
            ArrayList<Expression> args = new ArrayList<>();

            if(match(TokenType.OPEN_PAREN)) {
                if(!check(TokenType.CLOSE_PAREN)) {
                    do {
                        args.add(parseExpression());
                    } while(match(TokenType.COMMA));
                }
                consume(TokenType.CLOSE_PAREN, "Expected ')' after new class instance arguments");
            }

            return new Expression.NewInstance(classValue, args.toArray(new Expression[0]), line);
        }

        if(match(TokenType.BANG)) {
            Expression value = parseExpression();
            return new Expression.NotOperator(value);
        }

        if(match(TokenType.OPEN_PAREN)) {
            int line = previous().line;
            Expression expr = parseExpression();

            if(expr instanceof Expression.Variable) {
                consume(TokenType.CLOSE_PAREN, "Expected ')' after casting type");
                Expression value = parseExpression();
                return new Expression.CastExpression(value, expr, line);
            }

            consume(TokenType.CLOSE_PAREN, "Expected ')' after group expression");
            return expr;
        }

        if(match(TokenType.MINUS))
            return new Expression.UnaryMinus(previous().line, parsePrimary());

        throw error(peek(), "Expected expression");
    }

    private Expression.FunctionExpression parseFunctionBody(String function_name) {
        function_name = function_name + "@" + context + ":" + previous().line;
        consume(TokenType.OPEN_PAREN, "Expected '('");

        ArrayList<String> argument_names = new ArrayList<>();
        if(!check(TokenType.CLOSE_PAREN)) {
            do {
                Token id = consume(TokenType.IDENTIFIER, "Expected function argument identifier (name)");
                argument_names.add(id.lexeme);
            } while(match(TokenType.COMMA));
        }

        boolean varargs = (argument_names.size() > 0 && match(TokenType.VARARGS));

        consume(TokenType.CLOSE_PAREN, "Expected ')' after function arguments");
        consume(TokenType.OPEN_BRACE, "Expected function body");
        Statement.CompoundStatement body = parseCompoundStatement(false);

        return new Expression.FunctionExpression(function_name, body, argument_names.toArray(new String[0]), varargs);
    }

    private Expression.ObjectConstructor parseObject() {
        HashMap<Token, Expression> properties = new HashMap<>();

        if(!check(TokenType.CLOSE_BRACE)) {
            do {
                Token key = consume(TokenType.IDENTIFIER, "Expected property name");
                consume(TokenType.COLON, "Expected ':' after property name");
                Expression value = parseExpression();
                properties.put(key, value);
            } while(match(TokenType.COMMA));
        }

        consume(TokenType.CLOSE_BRACE, "Expected '}' to close object");
        return new Expression.ObjectConstructor(properties);
    }

    private Expression.ListConstructor parseList() {
        ArrayList<Expression> values = new ArrayList<>();

        if(!check(TokenType.CLOSE_SQUARE)) {
            do {
                values.add(parseExpression());
            } while(match(TokenType.COMMA));
        }

        consume(TokenType.CLOSE_SQUARE, "Expected ']' after list constructor");
        return new Expression.ListConstructor(values.toArray(new Expression[0]));
    }

    private boolean match(TokenType... types) {
        for(TokenType type : types) {
            if(check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token previous() {
        return tokens[current - 1];
    }

    private boolean check(TokenType type) {
        return peek().type == type;
    }

    private Token consume(TokenType type, String error_msg) {
        if(check(type)) return advance();
        throw error(peek(), error_msg);
    }

    private boolean available() {
        return peek().type != TokenType.EOF;
    }

    private Token peek() {
        return tokens[current];
    }

    private Token advance() {
        return tokens[current++];
    }

    private ParserError error(Token token, String msg) {
        return new ParserError("[Line " + token.line + " in " + context + "] " + msg);
    }
}
