package com.craftinginterpreters.lox

sealed trait Expr
object Expr:
    final case class Binary(val left: Expr, val op: Token, val right: Expr) extends Expr
    final case class Grouping(expression: Expr) extends Expr
    final case class Literal(value: Any | Null) extends Expr
    final case class Unary(operator: Token, right: Expr) extends Expr