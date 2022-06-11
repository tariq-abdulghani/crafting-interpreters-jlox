package abdulghani.tariq.lox;

import java.beans.Expression;
import java.util.List;
import java.util.function.Function;

abstract class Stmt {
    interface Visitor<R> {
        R visitBlockStmt(Block stmt);
//        R visitClassStmt(Class stmt);
        R visitExpressionStmt(Expression stmt);
//        R visitFunctionStmt(Function stmt);
//        R visitIfStmt(If stmt);
        R visitPrintStmt(Print stmt);
//        R visitReturnStmt(Return stmt);
        R visitVarStmt(Var stmt);
//        R visitWhileStmt(While stmt);
    }

    // Nested Stmt classes here...

    abstract <R> R accept(Visitor<R> visitor);


    static class Expression extends Stmt {
        Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStmt(this);
        }

        final Expr expression;
    }

    static class Print extends Stmt {
        Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }

        final Expr expression;
    }

    static class Var extends Stmt {
        Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitVarStmt(this);
        }

        final Token name;
        final Expr initializer;
    }

    static class Block extends Stmt {
        Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }

        final List<Stmt> statements;
    }
}