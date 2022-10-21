package fr.jellycat;

import java.util.List;

abstract class Expr {
    interface Visitor<R> {
        R visitTernaryExpr(Ternary expr);
        R visitBinaryExpr(Binary expr);
        R visitGroupingExpr(Grouping expr);
        R visitLiteralExpr(Literal expr);
        R visitPreUnaryExpr(PreUnary expr);
        R visitPostUnaryExpr(PostUnary expr);
    }

    static class Ternary extends Expr {
        Ternary(Expr test, Token firstOperator, Expr consequent, Token secondOperator, Expr alternate) {
            this.test = test;
            this.firstOperator = firstOperator;
            this.consequent = consequent;
            this.secondOperator = secondOperator;
            this.alternate = alternate;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitTernaryExpr(this);
        }

        final Expr test;
        final Token firstOperator;
        final Expr consequent;
        final Token secondOperator;
        final Expr alternate;
    }

    static class Binary extends Expr {
        Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpr(this);
        }

        final Expr left;
        final Token operator;
        final Expr right;
    }

    static class Grouping extends Expr {
        Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpr(this);
        }

        final Expr expression;
    }

    static class Literal extends Expr {
        Literal(Object value) {
            this.value = value;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpr(this);
        }

        final Object value;
    }

    static class PreUnary extends Expr {
        PreUnary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPreUnaryExpr(this);
        }

        final Token operator;
        final Expr right;
    }

    static class PostUnary extends Expr {
        PostUnary(Expr left, Token operator) {
            this.left = left;
            this.operator = operator;
        }

        @Override
        <R> R accept(Visitor<R> visitor) {
            return visitor.visitPostUnaryExpr(this);
        }

        final Expr left;
        final Token operator;
    }


    abstract <R> R accept(Visitor<R> visitor);
}
