package fr.jellycat;

import static fr.jellycat.TokenType.*;

import java.util.ArrayList;
import java.util.List;

class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;
    private boolean allowExpression;
    private boolean foundExpression = false;

    Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    Object parseREPL() {
        allowExpression = true;

        List<Stmt> statements = new ArrayList<>();
        while (!isAtEnd()) {
            statements.add(declaration());

            if (foundExpression) {
                Stmt last = statements.get(statements.size() - 1);
                return ((Stmt.Expression) last).expression;
            }

            allowExpression = false;
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(LET))
                return varDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Stmt statement() {
        if (match(IF))
            return ifStatement();
        if (match(WHILE)) {
            return whileStatement();
        }
        if (match(PRINT))
            return printStatement();
        if (match(DO))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt expressionStatement() {
        Expr expr = expression();

        if (allowExpression && isAtEnd()) {
            foundExpression = true;
        } else {
            consume(SEMICOLON, "Expect ';' after expression.");
        }

        return new Stmt.Expression(expr);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(END) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(END, "Expect 'end' after block.");
        return statements;
    }

    private Stmt ifStatement() {
        Expr condition = expression();
        consume(THEN, "Expect 'then' after condition.");

        List<Stmt> consequent = new ArrayList<>();
        List<Stmt> alternate = new ArrayList<>();

        while ((!check(ELSE) && !check(END)) && !isAtEnd()) {
            consequent.add(declaration());
        }

        if (match(ELSE)) {
            while (!check(END) && !isAtEnd()) {
                alternate.add(declaration());
            }
        }

        consume(END, "Expect 'end' after if statement.");

        return new Stmt.If(condition, consequent, alternate);
    }

    private Stmt whileStatement() {
        Expr condition = expression();
        Stmt body = statement();

        return new Stmt.While(condition, body);
    }

    private Stmt printStatement() {
        Expr value = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Stmt.Print(value);
    }

    private Stmt varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Stmt.Var(name, initializer);
    }

    private Expr expression() {
        return assignment();
    }

    private Expr assignment() {
        Expr expr = conditional();

        if (match(EQUAL)) {
            Token equals = previous();
            Expr value = assignment();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            }

            throw error(equals, "Invalid assignment target.");
        }

        return expr;
    }

    private Expr conditional() {
        Expr expr = or();

        if (match(QUESTION_MARK)) {
            Expr consequent = or();

            consume(COLUMN, "Expect ':' after consequent of conditional expression.");

            Expr alternate = conditional();

            expr = new Expr.Conditional(expr, consequent, alternate);
        }

        return expr;
    }

    private Expr or() {
        Expr expr = and();

        while (match(OR)) {
            Token operator = previous();
            Expr right = and();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr and() {
        Expr expr = equality();

        while (match(AND)) {
            Token operator = previous();
            Expr right = equality();
            expr = new Expr.Logical(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparison();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (match(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (match(STAR, SLASH, PERCENT)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(MINUS, PLUS, BANG, PLUS_PLUS, MINUS_MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return postfix();
    }

    private Expr postfix() {
        Expr expr = primary();

        if (match(MINUS_MINUS, PLUS_PLUS)) {
            Token operator = previous();
            return new Expr.PostFix(expr, operator);
        }

        return expr;
    }

    private Expr primary() {
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();

        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        Jeevee.error(token, current, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON)
                return;
        }

        switch (peek().type) {
            case CLASS:
            case DEF:
            case IF:
            case FOR:
            case LAMBDA:
            case MATCH:
            case PRINT:
            case RETURN:
            case LET:
            case WHILE:
                return;
            default:
                break;
        }

        advance();
    }
}
