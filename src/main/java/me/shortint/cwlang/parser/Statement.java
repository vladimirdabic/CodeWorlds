package me.shortint.cwlang.parser;

import me.shortint.cwlang.lexer.Token;

import java.util.HashMap;

public abstract class Statement {

    public interface Visitor {
        void visitExprStmt(ExpressionStatement expressionStatement);
        void visitCompoundStmt(CompoundStatement compoundStatement);
        void visitDeclareVarStmt(DeclareVariable declareVariable);
        void visitReturnStmt(Return returnStmt);
        void visitClassDef(ClassDefinition classDef);
        void visitIfStmt(If ifStmt);
        void visitWhileStmt(While whileStmt);
        void visitEventDeclare(EventDeclaration eventDeclaration);
        void visitCommandDeclare(CommandDeclaration commandDeclaration);
        void visitForLoop(ForLoop forLoop);
        void visitForEachLoop(ForEachLoop forEachLoop);
    }

    public abstract void accept(Visitor visitor);


    public static class ExpressionStatement extends Statement {
        public final Expression expression;

        public ExpressionStatement(Expression expression) {
            this.expression = expression;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitExprStmt(this);
        }
    }

    public static class CompoundStatement extends Statement {
        public final Statement[] statements;
        public final boolean scoped;

        public CompoundStatement(Statement[] statements, boolean scoped) {
            this.statements = statements;
            this.scoped = scoped;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCompoundStmt(this);
        }
    }

    public static class DeclareVariable extends Statement {
        public final Token variable;
        public final Expression value;

        public DeclareVariable(Token variable, Expression value) {
            this.variable = variable;
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitDeclareVarStmt(this);
        }
    }

    public static class Return extends Statement {
        public final Expression value;

        public Return(Expression value) {
            this.value = value;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturnStmt(this);
        }
    }

    public static class ClassDefinition extends Statement {
        public final Token className;
        public final Token parentClassName;
        public final HashMap<String, Expression> properties;
        public final HashMap<String, Expression> static_properties;
        public final Expression.FunctionExpression constructor;
        public final Statement.CompoundStatement static_constructor;

        public ClassDefinition(Token className, Token parentClassName, Expression.FunctionExpression constructor, HashMap<String, Expression> properties, Statement.CompoundStatement static_constructor, HashMap<String, Expression> static_properties) {
            this.className = className;
            this.parentClassName = parentClassName;
            this.constructor = constructor;
            this.properties = properties;
            this.static_constructor = static_constructor;
            this.static_properties = static_properties;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitClassDef(this);
        }
    }

    public static class If extends Statement {
        public final Expression check;
        public final Statement body;
        public final Statement elseClause;

        public If(Expression check, Statement body, Statement elseClause) {
            this.check = check;
            this.body = body;
            this.elseClause = elseClause;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIfStmt(this);
        }
    }

    public static class While extends Statement {
        public final Expression check;
        public final Statement body;

        public While(Expression check, Statement body) {
            this.check = check;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitWhileStmt(this);
        }
    }

    public static class EventDeclaration extends Statement {
        public final Token eventName;
        public final String[] args;
        public final Statement body;

        public EventDeclaration(Token eventName, String[] args, Statement body) {
            this.eventName = eventName;
            this.args = args;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitEventDeclare(this);
        }
    }

    public static class CommandDeclaration extends Statement {
        public final Token commandName;
        public final Expression data;

        public CommandDeclaration(Token commandName, Expression data) {
            this.commandName = commandName;
            this.data = data;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitCommandDeclare(this);
        }
    }

    public static class ForLoop extends Statement {
        public final Statement init;
        public final Expression test;
        public final Expression update;
        public final Statement body;

        public ForLoop(final Statement init, final Expression test, final Expression update, Statement body) {
            this.init = init;
            this.test = test;
            this.update = update;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitForLoop(this);
        }
    }

    public static class ForEachLoop extends Statement {
        public final Token variableName;
        public final Expression iterableValue;
        public final Statement body;

        public ForEachLoop(final Token variableName, final Expression iterableValue, Statement body) {
            this.variableName = variableName;
            this.iterableValue = iterableValue;
            this.body = body;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitForEachLoop(this);
        }
    }
}
