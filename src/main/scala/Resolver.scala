package com.craftinginterpreters.lox

import java.util.HashMap
import java.util.List
import java.util.Map
import java.util.Stack
import com.craftinginterpreters.lox.Stmt.Block
import scala.jdk.CollectionConverters._
import com.craftinginterpreters.lox.Stmt.Var
import com.craftinginterpreters.lox.Expr.Variable
import com.craftinginterpreters.lox.Expr.Assign
import com.craftinginterpreters.lox.Stmt.Expression
import com.craftinginterpreters.lox.Stmt.If
import com.craftinginterpreters.lox.Stmt.Print
import com.craftinginterpreters.lox.Stmt.While
import com.craftinginterpreters.lox.Expr.Binary
import com.craftinginterpreters.lox.Expr.Call
import com.craftinginterpreters.lox.Expr.Grouping
import com.craftinginterpreters.lox.Expr.Literal
import com.craftinginterpreters.lox.Expr.Logical
import com.craftinginterpreters.lox.Expr.Unary

class Resolver(val interpreter: Interpreter) extends Expr.Visitor[Unit], Stmt.Visitor[Unit] {
    private val scopes: Stack[Map[String, Boolean]] = new Stack()
    private var currentFunction: FunctionType = FunctionType.NONE

    private[lox] enum FunctionType {
        case NONE, FUNCTION
    }

    private def beginScope(): Unit = {
        scopes.push(new HashMap[String, Boolean])
    }

    private def endScope(): Unit = {
        scopes.pop()
    }

    private def declare(name: Token): Unit = {
        if (scopes.isEmpty()) return
        var scope: Map[String, Boolean] = scopes.peek()
        if (scope.containsKey(name.lexeme)) {
            Lox.error(name, "Already a variable with this name in this scope.")
        }
        scope.put(name.lexeme, false)
    }

    private def define(name: Token): Unit = {
        if (scopes.isEmpty()) return
        scopes.peek().put(name.lexeme, true)
    }

    private def resolveLocal(expr: Expr, name: Token): Unit = {
        var i = scopes.size() - 1
        while (i >= 0) {
            if (scopes.get(i).containsKey(name.lexeme)) {
                interpreter.resolve(expr, scopes.size - 1 - i)
                return
            }
            i -= 1
        }
    }

    def resolve(statements: List[Stmt]): Unit = {
        for (statement <- statements.asScala) {
            resolve(statement);
        }
    }

    def resolveFunction(function: Stmt.Function, funcType: FunctionType): Unit = {
        var enclosingFunction: FunctionType = currentFunction
        currentFunction = funcType
        beginScope()
        for (param <- function.params) {
            declare(param)
            define(param)
        }
        resolve(function.body.asJava)
        endScope()
        currentFunction = enclosingFunction
    }

    override def visitBlockStmt(stmt: Stmt.Block): Unit = {
        beginScope()
        resolve(stmt.statements.asJava)
        endScope()
        ()
    }

    override def visitExpressionStmt(stmt: Stmt.Expression): Unit = {
        resolve(stmt.expression)
        ()
    }

    override def visitFunctionStmt(stmt: Stmt.Function): Unit = {
        declare(stmt.name)
        define(stmt.name)

        resolveFunction(stmt, FunctionType.FUNCTION)
        ()
    }

    override def visitIfStmt(stmt: Stmt.If): Unit = {
        resolve(stmt.condition)
        resolve(stmt.thenBranch)
        if (stmt.elseBranch != null) resolve(stmt.elseBranch)
        ()
    }

    override def visitPrintStmt(stmt: Stmt.Print): Unit = {
        resolve(stmt.expression)
        ()
    }

    override def visitReturnStmt(stmt: Stmt.Return): Unit = {
        if (currentFunction == FunctionType.NONE) {
            Lox.error(stmt.keyword, "Can't return from top-level code.")
        }
        if (stmt.value != null) {
            resolve(stmt.value)
        }
        ()
    }

    override def visitVarStmt(stmt: Stmt.Var): Unit = {
        declare(stmt.name)
        if (stmt.initializer != null) {
            resolve(stmt.initializer)
        }
        define(stmt.name)
        ()
    }

    override def visitWhileStmt(stmt: Stmt.While): Unit = {
        resolve(stmt.condition)
        resolve(stmt.body)
        ()
    }

    override def visitAssignExpr(expr: Expr.Assign): Unit = {
        resolve(expr.value)
        resolveLocal(expr, expr.name)
        ()
    }

    override def visitBinaryExpr(expr: Expr.Binary): Unit = {
        resolve(expr.left)
        resolve(expr.right)
        ()
    }

    override def visitCallExpr(expr: Expr.Call): Unit = {
        resolve(expr.callee)
        for (argument <- expr.arguments) {
            resolve(argument)
        }
        ()
    }

    override def visitGroupingExpr(expr: Expr.Grouping): Unit = {
        resolve(expr.expression)
        ()
    }

    override def visitLiteralExpr(expr: Expr.Literal): Unit = {
        ()
    }

    override def visitLogicalExpr(expr: Expr.Logical): Unit = {
        resolve(expr.left)
        resolve(expr.right)
        ()
    }

    override def visitUnaryExpr(expr: Expr.Unary): Unit = {
        resolve(expr.right)
        ()
    }

    override def visitVariableExpr(expr: Expr.Variable): Unit = {
        if (!scopes.isEmpty() && scopes.peek().get(expr.name.lexeme) == java.lang.Boolean.FALSE) {
            Lox.error(expr.name, "Can't read local variable in its own initializer.")
        }
        resolveLocal(expr, expr.name)
        ()
    }

    private def resolve(stmt: Stmt): Unit = {
        stmt.accept(this)
    }

    private def resolve(expr: Expr): Unit = {
        expr.accept(this)
    }
}
