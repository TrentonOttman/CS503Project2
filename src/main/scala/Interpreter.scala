package com.craftinginterpreters.lox

import java.util.List

class Interpreter extends Expr.Visitor[Any] with Stmt.Visitor[Unit] {

    private val environment = new Environment()

    def interpret(statements: List[Stmt]): Unit = {
        try {
            for (statement <- statements) {
            execute(statement)
            }
        } catch {
            case error: RuntimeError =>
            Lox.runtimeError(error)
        }
    }

    override def visitLiteralExpr(expr: Expr.Literal): Any = {
        expr.value
    }

    override def visitUnaryExpr(expr: Expr.Unary): Any = {
        val right = evaluate(expr.right)
        expr.operator.tokenType match {
            case TokenType.MINUS => -right.asInstanceOf[Double]
            case TokenType.BANG => !isTruthy(right)
            case _ => null
        }
    }

    override def visitVariableExpr(expr: Expr.Variable): Any = {
        environment.get(expr.name)
    }

    private def isTruthy(value: Any): Boolean = {
        value match {
            case null => false
            case b: Boolean => b
            case _ => true
        }
    }

    private def isEqual(a: Any, b: Any): Boolean = {
        if (a == null && b == null) true
        else if (a == null) false
        else a.equals(b)
    }

    private def stringify(value: Any): String = {
        if (value == null) return "nil"
        value match {
            case d: Double =>
                val text = d.toString
                if (text.endsWith(".0")) text.substring(0, text.length - 2)
                else text
            case other =>
                other.toString
        }
    }

    override def visitGroupingExpr(expr: Expr.Grouping): Any = {
        evaluate(expr.expression)
    }

    private def evaluate(expr: Expr): Any = {
        expr.accept(this)
    }

    private def execute(stmt: Stmt): Unit = {
        stmt.accept(this)
    }

    def executeBlock(statements: List[Stmt], environment: Environment): Unit = {
        val previous = this.environment
        try {
            this.environment = environment
            for (statement <- statements) {
            execute(statement)
            }
        } finally {
            this.environment = previous
        }
    }


    override def visitBlockStmt(stmt: Stmt.Block): Unit = {
        executeBlock(stmt.statements, new Environment(environment))
    }

    override def visitExpressionStmt(stmt: Stmt.Expression): Unit = {
        evaluate(stmt.expression)
        ()
    }

    override def visitPrintStmt(stmt: Stmt.Print): Unit = {
        val value = evaluate(stmt.expression)
        println(stringify(value))
        ()
    }

    override def visitVarStmt(stmt: Stmt.Var): Unit = {
        var value: Any = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
    }

    override def visitAssignExpr(expr: Expr.Assign): Any = {
        val value = evaluate(expr.value)
        environment.assign(expr.name, value)
        value
    }

    override def visitBinaryExpr(expr: Expr.Binary): Any = {
        val left = evaluate(expr.left)
        val right = evaluate(expr.right)
        expr.operator.tokenType match {
            case TokenType.GREATER =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] > right.asInstanceOf[Double]
            case TokenType.GREATER_EQUAL =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] >= right.asInstanceOf[Double]
            case TokenType.LESS =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] < right.asInstanceOf[Double]
            case TokenType.LESS_EQUAL =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] <= right.asInstanceOf[Double]
            case TokenType.MINUS =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] - right.asInstanceOf[Double]
            case TokenType.SLASH =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] / right.asInstanceOf[Double]
            case TokenType.STAR =>
                checkNumberOperands(expr.operator, left, right)
                left.asInstanceOf[Double] * right.asInstanceOf[Double]
            case TokenType.PLUS =>
                (left, right) match {
                    case (l: Double, r: Double) => l + r
                    case (l: String, r: String) => l + r
                    case _ =>
                    throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.")
                }
            case TokenType.BANG_EQUAL =>
                !isEqual(left, right)
            case TokenType.EQUAL_EQUAL =>
                isEqual(left, right)
            case _ => null
        }
    }

    private def checkNumberOperands(operator: Token, left: Any, right: Any): Unit = {
        (left, right) match {
            case (_: Double, _: Double) => ()
            case _ => throw new RuntimeError(operator, "Operands must be numbers.")
        }
    }
}