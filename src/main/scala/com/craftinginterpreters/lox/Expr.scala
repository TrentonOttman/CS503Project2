package com.craftinginterpreters.lox

trait Expr {
  def accept[R](visitor: Expr.Visitor[R]): R
}

object Expr {
  trait Visitor[R] {
    def visitAssignExpr(expr: Assign): R
    def visitBinaryExpr(expr: Binary): R
    def visitCallExpr(expr: Call): R
    def visitGroupingExpr(expr: Grouping): R
    def visitLiteralExpr(expr: Literal): R
    def visitLogicalExpr(expr: Logical): R
    def visitUnaryExpr(expr: Unary): R
    def visitVariableExpr(expr: Variable): R
  }

  case class Assign(
    name: Token,
    value: Expr
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitAssignExpr(this)
    }
  }

  case class Binary(
    left: Expr,
    operator: Token,
    right: Expr
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitBinaryExpr(this)
    }
  }

  case class Call(
    callee: Expr,
    paren: Token,
    arguments: List[Expr]
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitCallExpr(this)
    }
  }

  case class Grouping(
    expression: Expr
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitGroupingExpr(this)
    }
  }

  case class Literal(
    value: Any
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitLiteralExpr(this)
    }
  }

  case class Logical(
    left: Expr,
    operator: Token,
    right: Expr
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitLogicalExpr(this)
    }
  }

  case class Unary(
    operator: Token,
    right: Expr
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitUnaryExpr(this)
    }
  }

  case class Variable(
    name: Token
  ) extends Expr {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitVariableExpr(this)
    }
  }
}
