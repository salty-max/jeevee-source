// package fr.jellycat;

// class AstPrinter implements Expr.Visitor<String> {
// String print(Expr expr) {
// return expr.accept(this);
// }

// @Override
// public String visitConditionalExpr(Expr.Conditional expr) {
// return parenthesizeConditional(
// expr.firstOperator.lexeme, expr.secondOperator.lexeme, expr.test,
// expr.consequent,
// expr.alternate);
// }

// @Override
// public String visitBinaryExpr(Expr.Binary expr) {
// return parenthesize(expr.operator.lexeme, expr.left, expr.right);
// }

// @Override
// public String visitGroupingExpr(Expr.Grouping expr) {
// return parenthesize("group", expr.expression);
// }

// @Override
// public String visitLiteralExpr(Expr.Literal expr) {
// if (expr.value == null)
// return "nil";
// return expr.value.toString();
// }

// @Override
// public String visitPostFixExpr(Expr.PostFix expr) {
// return parenthesize(expr.operator.lexeme, expr.left);
// }

// @Override
// public String visitUnaryExpr(Expr.Unary expr) {
// return parenthesize(expr.operator.lexeme, expr.right);
// }

// private String parenthesize(String name, Expr... exprs) {
// StringBuilder builder = new StringBuilder();

// builder.append("(").append(name);

// for (Expr expr : exprs) {
// builder.append(" ");
// builder.append(expr.accept(this));
// }

// builder.append(")");

// return builder.toString();
// }

// private String parenthesizeConditional(String firstOperator, String
// secondOperator, Expr... exprs) {
// StringBuilder builder = new StringBuilder();

// builder.append("(").append(firstOperator).append(secondOperator);

// for (Expr expr : exprs) {
// builder.append(" ");
// builder.append(expr.accept(this));
// }

// builder.append(")");

// return builder.toString();
// }
// }
