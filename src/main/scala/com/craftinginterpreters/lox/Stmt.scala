package com.craftinginterpreters.lox

trait Stmt {
  def accept[R](visitor: Stmt.Visitor[R]): R
}

object Stmt {
  trait Visitor[R] {
    def visitBlockStmt(stmt: Block): R
    def visitClassStmt(stmt: Class): R
    def visitClassStmt(stmt: Class): R
    def visitExpressionStmt(stmt: Expression): R
    def visitFunctionStmt(stmt: Function): R
    def visitIfStmt(stmt: If): R
    def visitPrintStmt(stmt: Print): R
    def visitReturnStmt(stmt: Return): R
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

  case class Class(
    name: Token,
    superclass: Expr.Variable,
    methods: List[Stmt.Function]
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitClassStmt(this)
    }
  }

  case class Class(
    name: Token,
    methods: java.util.List[Stmt.Function]
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitClassStmt(this)
    }
  }

  case class Expression(
    expression: Expr
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitExpressionStmt(this)
    }
  }

  case class Function(
    name: Token,
    params: List[Token],
    body: List[Stmt]
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitFunctionStmt(this)
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

  case class Return(
    keyword: Token,
    value: Expr
  ) extends Stmt {
    def accept[R](visitor: Visitor[R]): R = {
      visitor.visitReturnStmt(this)
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
