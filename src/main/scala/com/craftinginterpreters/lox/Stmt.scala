package com.craftinginterpreters.lox

trait Stmt {
  def accept[R](visitor: Stmt.Visitor[R]): R
}

object Stmt {
  trait Visitor[R] {
    def visitBlockStmt(stmt: Block): R
    def visitExpressionStmt(stmt: Expression): R
    def visitIfStmt(stmt: If): R
    def visitPrintStmt(stmt: Print): R
    def visitVarStmt(stmt: Var): R
    def visitWhileStmt(stmt: While): R
  }

  case class Block(
    statements: List[Stmt]
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitBlockStmt(this)
    }
  }

  case class Expression(
    expression: Expr
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitExpressionStmt(this)
    }
  }

  case class If(
    condition: Expr,
    thenBranch: Stmt,
    elseBranch: Stmt
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitIfStmt(this)
    }
  }

  case class Print(
    expression: Expr
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitPrintStmt(this)
    }
  }

  case class Var(
    name: Token,
    initializer: Expr
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitVarStmt(this)
    }
  }

  case class While(
    condition: Expr,
    body: Stmt
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitWhileStmt(this)
    }
  }
}
