package me.shortint.cwlang.parser;

import me.shortint.cwlang.interpreter.types.Value;
import me.shortint.cwlang.lexer.Token;

import java.util.HashMap;

public abstract class Expression {
    public interface Visitor<R> {
        R visitLiteral(Literal literal);
        R visitVariable(Variable variable);
        R visitBinaryExpr(BinaryExpression binaryExpression);
        R visitFunctionCall(FunctionCall functionCall);
        R visitFunctionExpression(FunctionExpression functionExpression);
        R visitVariableAssign(VariableAssignment varAssign);
        R visitThisRef(ThisReference thisRef);
        R visitSuperRef(SuperReference superRef);
        R visitGetProperty(GetProperty getProperty);
        R visitSetProperty(SetProperty setProperty);
        R visitObjectConstructor(ObjectConstructor objectConstructor);
        R visitListConstructor(ListConstructor listConstructor);
        R visitGetIndex(GetIndex getIndex);
        R visitSetIndex(SetIndex setIndex);
        R visitNewInstance(NewInstance newInstance);
        R visitIsInstanceOf(IsInstanceOf isInstanceOf);
        R visitNotOperator(NotOperator notOperator);
        R visitCastExpression(CastExpression castExpression);
        R visitUnaryMinus(UnaryMinus unaryMinus);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    public static class Literal extends Expression {
        public final Value value;

        public Literal(Value value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    public static class Variable extends Expression {
        public final Token token;

        public Variable(Token token) {
            this.token = token;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariable(this);
        }
    }

    public static class BinaryExpression extends Expression {
        public final Expression left;
        public final Expression right;
        public final Token operator;

        public BinaryExpression(Expression left, Expression right, Token operator) {
            this.left = left;
            this.right = right;
            this.operator = operator;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }
    }

    public static class FunctionCall extends Expression {
        public final Expression callee;
        public final Expression[] args;
        public final int line;

        public FunctionCall(Expression callee, Expression[] args, int line) {
            this.callee = callee;
            this.args = args;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionCall(this);
        }
    }

    public static class FunctionExpression extends Expression {
        public final Statement body;
        public final String[] argument_names;
        public final String function_name;
        public final boolean varargs;


        public FunctionExpression(String function_name, Statement body, String[] argument_names, boolean varargs) {
            this.function_name = function_name;
            this.body = body;
            this.argument_names = argument_names;
            this.varargs = varargs;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionExpression(this);
        }
    }

    public static class VariableAssignment extends Expression {
        public final Token variable;
        public final Expression value;

        public VariableAssignment(Token variable, Expression value) {
            this.variable = variable;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableAssign(this);
        }
    }

    public static class ThisReference extends Expression {
        public int line;

        public ThisReference(int line) {
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisRef(this);
        }
    }

    public static class SuperReference extends Expression {
        public int line;

        public SuperReference(int line) {
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperRef(this);
        }
    }

    public static class ObjectConstructor extends Expression {
        public final HashMap<Token, Expression> properties;

        public ObjectConstructor(HashMap<Token, Expression> properties) {
            this.properties = properties;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitObjectConstructor(this);
        }
    }

    public static class GetProperty extends Expression {
        public final Expression propertyHolder;
        public final Token propertyName;

        public GetProperty(Expression propertyHolder, Token propertyName) {
            this.propertyHolder = propertyHolder;
            this.propertyName = propertyName;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetProperty(this);
        }
    }

    public static class SetProperty extends Expression {
        public final Expression propertyHolder;
        public final Token propertyName;
        public final Expression propertyValue;

        public SetProperty(Expression propertyHolder, Token propertyName, Expression propertyValue) {
            this.propertyHolder = propertyHolder;
            this.propertyName = propertyName;
            this.propertyValue = propertyValue;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetProperty(this);
        }
    }

    public static class ListConstructor extends Expression {
        public final Expression[] values;

        public ListConstructor(Expression[] values) {
            this.values = values;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitListConstructor(this);
        }
    }

    public static class GetIndex extends Expression {
        public final Expression indexValue;
        public final Expression index;
        public final int line;

        public GetIndex(Expression indexValue, Expression index, int line) {
            this.indexValue = indexValue;
            this.index = index;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetIndex(this);
        }
    }

    public static class SetIndex extends Expression {
        public final Expression indexValue;
        public final Expression index;
        public final Expression value;
        public final int line;

        public SetIndex(Expression indexValue, Expression index, Expression value, int line) {
            this.indexValue = indexValue;
            this.index = index;
            this.value = value;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetIndex(this);
        }
    }

    public static class NewInstance extends Expression {
        public final Expression classValue;
        public final Expression[] args;
        public final int line;

        public NewInstance(Expression classValue, Expression[] args, int line) {
            this.classValue = classValue;
            this.args = args;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNewInstance(this);
        }
    }

    public static class IsInstanceOf extends Expression {
        public final Expression value;
        public final Expression tester;
        public final int line;

        public IsInstanceOf(Expression value, Expression tester, int line) {
            this.value = value;
            this.tester = tester;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIsInstanceOf(this);
        }
    }

    public static class NotOperator extends Expression {
        public final Expression value;

        public NotOperator(Expression value) {
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNotOperator(this);
        }
    }

    public static class CastExpression extends Expression {
        public final Expression value;
        public final Expression castTo;
        public final int line;

        public CastExpression(final Expression value, final Expression castTo, final int line) {
            this.value = value;
            this.castTo = castTo;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCastExpression(this);
        }
    }

    public static class UnaryMinus extends Expression {
        public final Expression value;
        public final int line;

        public UnaryMinus(final int line, final Expression value) {
            this.value = value;
            this.line = line;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryMinus(this);
        }
    }
}
