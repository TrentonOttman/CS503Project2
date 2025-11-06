package com.craftinginterpreters.lox


class Interpreter extends Expr.Visitor[Any] {

    def interpret(expression: Expr): Unit = {
        try {
            val value = evaluate(expression)
            println(stringify(value))
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