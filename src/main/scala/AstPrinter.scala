package com.craftinginterpreters.lox

import com.craftinginterpreters.lox.Expr.Binary
import com.craftinginterpreters.lox.Expr.Grouping
import com.craftinginterpreters.lox.Expr.Literal
import com.craftinginterpreters.lox.Expr.Unary
import com.craftinginterpreters.lox.Expr.Assign
import com.craftinginterpreters.lox.Expr.Logical
import com.craftinginterpreters.lox.Expr.Variable

class AstPrinter extends Expr.Visitor[String] {
    def print(expr: Expr): String = {
        expr.accept(this)
    }

    def visitBinaryExpr(expr: Binary): String = {
        parenthesize(expr.operator.lexeme, expr.left, expr.right)
    }

    def visitGroupingExpr(expr: Grouping): String = {
        parenthesize("group", expr.expression)
    }

    def visitLiteralExpr(expr: Literal): String = {
        if (expr.value == null) return "nil"
        expr.value.toString()
    }

    def visitUnaryExpr(expr: Unary): String = {
        parenthesize(expr.operator.lexeme, expr.right)
    }

    def visitAssignExpr(expr: Assign): String = {
        return ""
    }

    def visitLogicalExpr(expr: Logical): String = {
        return ""
    }

    def visitVariableExpr(expr: Variable): String = {
        return ""
    }

    private def parenthesize(name: String, exprs: Expr*): String = {
        var builder = new StringBuilder
        builder.append("(").append(name)
        for (expr <- exprs) {
            builder.append(" ")
            builder.append(expr.accept(this))
        }
        builder.append(")")

        builder.toString();
    }
}