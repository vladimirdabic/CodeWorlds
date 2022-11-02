package me.shortint.cwlang.interpreter;

import me.shortint.cwlang.interpreter.types.*;
import me.shortint.cwlang.interpreter.types.ClassValue;
import me.shortint.cwlang.lexer.Token;
import me.shortint.cwlang.lexer.TokenType;
import me.shortint.cwlang.parser.Expression;
import me.shortint.cwlang.parser.Statement;

import java.util.*;

public class Interpreter implements Statement.Visitor, Expression.Visitor<Value> {
    public Environment environment;
    public String context;
    public static final NullValue nullValue = new NullValue();

    // CodeWorlds stuff
    public interface CodeWorldsInterface {
        void registerEvent(Interpreter interpreter, Statement.EventDeclaration declaration);
        void registerCommand(Interpreter interpreter, ValueCallable executor, String name, String description, String usageMessage, String category, List<String> aliases, int line);
    }

    private CodeWorldsInterface codeWorldsInterface;

    public void setCodeWorldsInterface(CodeWorldsInterface codeWorldsInterface) {
        this.codeWorldsInterface = codeWorldsInterface;
    }

    public Interpreter(Environment environment, String context) {
        this.environment = environment;
        this.context = context;
    }

    public Value evaluate(Expression expression) {
        return expression.accept(this);
    }

    public void evaluate(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Value visitLiteral(Expression.Literal literal) {
        return literal.value;
    }

    @Override
    public Value visitVariable(Expression.Variable variable) {
        Value value = environment.get(variable.token.lexeme);

        if(value == null)
            throw error(variable.token.line, "Tried referencing an undeclared variable '" + variable.token.lexeme + "'");

        return value;
    }

    @Override
    public Value visitBinaryExpr(Expression.BinaryExpression binaryExpression) {
        Value left = evaluate(binaryExpression.left);
        Value right = evaluate(binaryExpression.right);

        // special cases
        if(binaryExpression.operator.type == TokenType.DOUBLE_EQUALS)
            return new BooleanValue(left.equals(right));

        if(binaryExpression.operator.type == TokenType.NOT_EQUALS)
            return new BooleanValue(!left.equals(right));


        if(!(left instanceof ValueBinaryOp))
            throw error(binaryExpression.operator.line, "Missing binary operation interface implementation for left value of type '" + left.getTypeName() + "'");

        if(!(right instanceof ValueBinaryOp))
            throw error(binaryExpression.operator.line, "Missing binary operation interface implementation for right value of type '" + left.getTypeName() + "'");

        ValueBinaryOp leftBinaryOp = (ValueBinaryOp)left;
        ValueBinaryOp rightBinaryOp = (ValueBinaryOp)right;

        Value result = null;

        switch (binaryExpression.operator.type) {
            case PLUS: result = leftBinaryOp.add(right); break;
            case MINUS: result = leftBinaryOp.sub(right); break;
            case STAR: result = leftBinaryOp.mul(right); break;
            case SLASH: result = leftBinaryOp.div(right); break;
            case LOWER: result = leftBinaryOp.lt(right); break;
            case LT_EQUALS: result = leftBinaryOp.lte(right); break;
            case GREATER: result = rightBinaryOp.lt(left); break;
            case GT_EQUALS: result = rightBinaryOp.lte(left); break;
        }

        if(result == null)
            throw error(binaryExpression.operator.line, "Unsupported operator '" + binaryExpression.operator.lexeme + "' for types '" + left.getTypeName() + "' and '" + right.getTypeName() + "'");

        return result;
    }

    @Override
    public Value visitFunctionCall(Expression.FunctionCall functionCall) {
        Value value = evaluate(functionCall.callee);

        if(!(value instanceof ValueCallable))
            throw error(functionCall.line, "Tried calling a non callable value of type '" + value.getTypeName() + "'");

        ArrayList<Value> args = new ArrayList<>();

        for(Expression arg : functionCall.args)
            args.add(evaluate(arg).copy());

        return ((ValueCallable)value).run(this, args.toArray(new Value[0]), functionCall.line);
    }

    @Override
    public Value visitFunctionExpression(Expression.FunctionExpression functionExpression) {
        return new FunctionValue(functionExpression.function_name, functionExpression.body, functionExpression.argument_names, environment, functionExpression.varargs);
    }

    @Override
    public Value visitVariableAssign(Expression.VariableAssignment varAssign) {
        Value value = evaluate(varAssign.value);
        Value assigned = environment.assign(varAssign.variable.lexeme, value);

        if(assigned == null)
            throw error(varAssign.variable.line, "Tried assigning to an undeclared variable '" + varAssign.variable.lexeme + "'");

        return assigned;
    }

    @Override
    public Value visitThisRef(Expression.ThisReference thisRef) {
        Value thisValue = environment.getThisValue();

        if(thisValue == null)
            throw error(thisRef.line, "Using 'this' keyword outside an object/class method");

        return thisValue;
    }

    @Override
    public Value visitSuperRef(Expression.SuperReference superRef) {
        Value thisValue = environment.getThisValue();

        if(!(thisValue instanceof ClassInstanceValue))
            throw error(superRef.line, "Using 'super' keyword outside an class instance");

        ClassInstanceValue value = (ClassInstanceValue)thisValue;
        ClassInstanceValue superInstance = value.getSuperInstance();

        if(superInstance == null)
            throw error(superRef.line, "Class doesn't have a super instance");

        return superInstance;
    }

    @Override
    public Value visitGetProperty(Expression.GetProperty getProperty) {
        Value value = evaluate(getProperty.propertyHolder);

        if(!(value instanceof ValueProperties))
            throw error(getProperty.propertyName.line, "Tried getting a property from a non object value");

        Value returned = ((ValueProperties)value).getProperty(getProperty.propertyName.lexeme);

        if(returned == null)
            throw error(getProperty.propertyName.line, "Unknown property '" + getProperty.propertyName.lexeme + "' for object of type '" + value.getTypeName() + "'");

        return returned;
    }

    @Override
    public Value visitSetProperty(Expression.SetProperty setProperty) {
        Value object = evaluate(setProperty.propertyHolder);

        if(!(object instanceof ValueProperties))
            throw error(setProperty.propertyName.line, "Tried setting a property in a non object value");

        Value value = evaluate(setProperty.propertyValue);
        Value returned = ((ValueProperties)object).setProperty(setProperty.propertyName.lexeme, value);

        if(returned == null)
            throw error(setProperty.propertyName.line, "Failed to set property in object of type '" + object.getTypeName() + "'");

        return returned;
    }

    @Override
    public Value visitObjectConstructor(Expression.ObjectConstructor objectConstructor) {
        ObjectValue object = new ObjectValue();

        for(Map.Entry<Token, Expression> entry : objectConstructor.properties.entrySet()) {
            String key = entry.getKey().lexeme;
            Value value = evaluate(entry.getValue());
            object.setProperty(key, value);
        }

        return object;
    }

    @Override
    public Value visitListConstructor(Expression.ListConstructor listConstructor) {
        ListValue list = new ListValue();

        for(Expression value : listConstructor.values)
            list.getValues().add(evaluate(value));

        return list;
    }

    @Override
    public Value visitGetIndex(Expression.GetIndex getIndex) {
        Value value = evaluate(getIndex.indexValue);

        if(!(value instanceof ValueIndexable))
            throw error(getIndex.line, "Tried indexing from a non indexable object of type '" + value.getTypeName() + "'");

        Value index = evaluate(getIndex.index);

        ValueIndexable valueIndexable = (ValueIndexable) value;
        Value returned = valueIndexable.getAtIndex(index);

        if(returned == null)
            throw error(getIndex.line, "Index out of bounds");

        return returned;
    }

    @Override
    public Value visitSetIndex(Expression.SetIndex setIndex) {
        Value value = evaluate(setIndex.indexValue);

        if(!(value instanceof ValueIndexable))
            throw error(setIndex.line, "Tried setting at an index in a non indexable object of type '" + value.getTypeName() + "'");

        Value index = evaluate(setIndex.index);

        Value setValue = evaluate(setIndex.value);
        ValueIndexable valueIndexable = (ValueIndexable) value;
        Value returned = valueIndexable.setAtIndex(index, setValue);

        if(returned == null)
            throw error(setIndex.line, "Unknown index '" + index.toStringValue() + "'");

        return returned;
    }

    @Override
    public Value visitNewInstance(Expression.NewInstance newInstance) {
        Value value = evaluate(newInstance.classValue);

        if(!(value instanceof ValueNewInstance))
            throw error(newInstance.line, "Tried creating an instance of a non class value of type '" + value.getTypeName() + "'");

        ValueNewInstance valueNew = (ValueNewInstance)value;

        ArrayList<Value> args = new ArrayList<>();
        for(Expression arg : newInstance.args)
            args.add(evaluate(arg));

        return valueNew.newInstance(this, args.toArray(new Value[0]), newInstance.line);
    }

    @Override
    public Value visitIsInstanceOf(Expression.IsInstanceOf isInstanceOf) {
        Value tester = evaluate(isInstanceOf.tester);

        if(!(tester instanceof ValueClass))
            throw error(isInstanceOf.line, "Tried using a non class value with the 'is' operator");

        ValueClass classValue = (ValueClass)tester;
        Value value = evaluate(isInstanceOf.value);

        return new BooleanValue(classValue.valueIsInstance(value));
    }

    @Override
    public Value visitNotOperator(Expression.NotOperator notOperator) {
        Value value = evaluate(notOperator.value);
        return new BooleanValue(!booleanRepr(value));
    }

    @Override
    public Value visitCastExpression(Expression.CastExpression castExpression) {
        Value castTo = evaluate(castExpression.castTo);

        if(!(castTo instanceof ValueClass))
            throw error(castExpression.line, "Cannot cast to a non class value");

        ValueClass valueClass = (ValueClass)castTo;
        Value value = evaluate(castExpression.value);
        Value casted = valueClass.castValueToSelf(this, castExpression.line, value);

        if(casted == null)
            throw error(castExpression.line, "Cannot cast value of type '" + value.getTypeName() + "' to class of type '" + castTo.getTypeName() + "'");

        return casted;
    }

    @Override
    public Value visitUnaryMinus(Expression.UnaryMinus unaryMinus) {
        Value value = evaluate(unaryMinus.value);

        if(!(value instanceof NumberValue))
            throw error(unaryMinus.line, "Cannot use unary minus expression on value of type '" + value.getTypeName() + "'");

        NumberValue numberValue = (NumberValue)value;
        return new NumberValue(-numberValue.getValue());
    }

    @Override
    public void visitExprStmt(Statement.ExpressionStatement expressionStatement) {
        evaluate(expressionStatement.expression);
    }

    @Override
    public void visitCompoundStmt(Statement.CompoundStatement compoundStatement) {

        if(compoundStatement.scoped)
            environment = new Environment(environment);

        for(Statement statement : compoundStatement.statements)
            evaluate(statement);

        if(compoundStatement.scoped)
            environment = environment.parent;
    }

    @Override
    public void visitDeclareVarStmt(Statement.DeclareVariable declareVariable) {
        Value value = declareVariable.value == null ? Interpreter.nullValue : evaluate(declareVariable.value);
        environment.declare(declareVariable.variable.lexeme, value);
    }

    @Override
    public void visitReturnStmt(Statement.Return returnStmt) {
        throw new ReturnError(returnStmt.value == null ? Interpreter.nullValue : evaluate(returnStmt.value));
    }

    @Override
    public void visitClassDef(Statement.ClassDefinition classDef) {
        FunctionValue constructor = classDef.constructor == null ? null : (FunctionValue)evaluate(classDef.constructor); // guaranteed to return a function value

        HashMap<String, Value> static_props = new HashMap<>();
        Expression value;
        for(Map.Entry<String, Expression> prop : classDef.static_properties.entrySet()) {
            value = prop.getValue();
            static_props.put(prop.getKey(), value == null ? Interpreter.nullValue : evaluate(value));
        }

        Value parentClass = classDef.parentClassName == null ? null : environment.get(classDef.parentClassName.lexeme);
        ClassValue parent = parentClass == null ? null : (ClassValue) parentClass;

        ClassValue classValue = new ClassValue(classDef.className.lexeme, context, classDef.className.line, constructor, classDef.properties, environment, static_props, parent);

        // static constructor
        if(classDef.static_constructor != null) {
            Environment originalEnv = environment;
            environment = new Environment(classValue.static_environment);
            evaluate(classDef.static_constructor);
            environment = originalEnv;
        }

        environment.declare(classDef.className.lexeme, classValue);
    }

    @Override
    public void visitIfStmt(Statement.If ifStmt) {
        Value value = evaluate(ifStmt.check);
        if(booleanRepr(value))
            evaluate(ifStmt.body);
        else if(ifStmt.elseClause != null)
            evaluate(ifStmt.elseClause);
    }

    @Override
    public void visitWhileStmt(Statement.While whileStmt) {
        while(booleanRepr(evaluate(whileStmt.check)))
            evaluate(whileStmt.body);
    }

    @Override
    public void visitEventDeclare(Statement.EventDeclaration eventDeclaration) {
        codeWorldsInterface.registerEvent(this, eventDeclaration);
    }

    @Override
    public void visitCommandDeclare(Statement.CommandDeclaration commandDeclaration) {
        Value dataValue = evaluate(commandDeclaration.data);

        if(!(dataValue instanceof ObjectValue || dataValue instanceof ClassInstanceValue || dataValue instanceof ClassValue))
            throw error(commandDeclaration.commandName.line, "Can only use an object or class instance to define command data and behaviour");

        ValueProperties data = (ValueProperties) dataValue;
        Value executor = data.getProperty("executor");

        if(executor == null)
            throw error(commandDeclaration.commandName.line, "Undefined executor for command '" + commandDeclaration.commandName.lexeme + "'");

        if(!(executor instanceof ValueCallable))
            throw error(commandDeclaration.commandName.line, "Executor for command '" + commandDeclaration.commandName.lexeme + "' must be a callable value");

        Value temp;
        temp = data.getProperty("description");
        String description = temp instanceof NullValue ? "No description provided" : temp.toStringValue();

        temp = data.getProperty("usage");
        String usage = temp instanceof NullValue ? "/" + commandDeclaration.commandName.lexeme : temp.toStringValue();

        temp = data.getProperty("category");
        String category = temp instanceof NullValue ? context : temp.toStringValue();

        temp = data.getProperty("aliases");
        ArrayList<String> aliasesList = new ArrayList<>();

        if(!(temp instanceof NullValue)) {
            if (!(temp instanceof ListValue))
                throw error(commandDeclaration.commandName.line, "Aliases for command '" + commandDeclaration.commandName.lexeme + "' must be a list of strings");

            ListValue aliasesValue = (ListValue) temp;

            for (Value alias : aliasesValue.getValues()) {
                if (!(alias instanceof StringValue))
                    throw error(commandDeclaration.commandName.line, "Aliases for command '" + commandDeclaration.commandName.lexeme + "' must be a list of strings");
                StringValue aliasString = (StringValue) alias;
                aliasesList.add(aliasString.getValue().toLowerCase());
            }
        }

        codeWorldsInterface.registerCommand(this, (ValueCallable)executor, commandDeclaration.commandName.lexeme.toLowerCase(), description, usage, category, aliasesList, commandDeclaration.commandName.line);
    }

    @Override
    public void visitForLoop(Statement.ForLoop forLoop) {
        Environment interpreterEnvironment = environment;
        environment = new Environment(interpreterEnvironment);
        evaluate(forLoop.init);

        while(booleanRepr(evaluate(forLoop.test))) {
            evaluate(forLoop.body);
            evaluate(forLoop.update);
        }

        environment = interpreterEnvironment;
    }

    @Override
    public void visitForEachLoop(Statement.ForEachLoop forEachLoop) {
        Value value = evaluate(forEachLoop.iterableValue);

        if(!(value instanceof ValueIterable))
            throw error(forEachLoop.variableName.line, "Type '" + value.getTypeName() + "' is not iterable");

        ValueIterable iterable = (ValueIterable)value;

        Environment interpreterEnvironment = environment;
        environment = new Environment(interpreterEnvironment);
        iterable.iterate(this, forEachLoop.body, forEachLoop.variableName.lexeme);
        environment = interpreterEnvironment;
    }

    public InterpreterError error(String msg) {
        return new InterpreterError("[" + context + "] " + msg);
    }

    public InterpreterError error(int line, String msg) {
        return new InterpreterError("[Line " + line + " in " + context + "] " + msg);
    }

    public boolean booleanRepr(Value value) {
        if(value instanceof BooleanValue)
            return ((BooleanValue)value).getValue();

        return !(value instanceof NullValue);
    }

    public void validateArguments(String func_name, boolean match_length, int line, Value[] args, String... types) {
        if(match_length && args.length < types.length)
           throw error(line, "Too few arguments passed to '" + func_name + "', expected " + types.length + " got " + args.length);

        int min = Math.min(args.length, types.length);

        for(int i = 0; i < min; ++i) {
            if((!types[i].equals("any")) && (!args[i].getTypeName().equals(types[i])))
                throw error(line, "Invalid type of argument #" + (i+1) + " for '" + func_name + "', expected '" + types[i] + "' got '" + args[i].getTypeName() + "'");
        }
    }
}
