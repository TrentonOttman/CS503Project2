package com.craftinginterpreters.lox

import java.util.ArrayList
import java.util.HashMap
import java.util.Map
import scala.collection.immutable.HashMap
import scala.jdk.CollectionConverters._ 
//import scala.collection.mutable.ListBuffer
//import java.util.List //im not sure if we need this?? we didnt use it before but its in the textbook that it should already be here

class Interpreter extends Expr.Visitor[Any], Stmt.Visitor[Unit] {
    val globals = new Environment()
    private var environment: Environment = globals
    private var locals: Map[Expr, Integer] = new java.util.HashMap

    {
        globals.define("clock", new LoxCallable {
        override def arity(): Int = 0
        override def call(interpreter: Interpreter, arguments: java.util.List[Any]): Any = {
            System.currentTimeMillis().toDouble / 1000.0
        }
        override def toString: String = "<native fn>"
        })
    }

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

    override def visitLogicalExpr(expr: Expr.Logical): Any = {
        val left = evaluate(expr.left)
        if (expr.operator.tokenType == TokenType.OR) {
            if (isTruthy(left)) return left
        } 
        else {
            if (!isTruthy(left)) return left
        }
        evaluate(expr.right)
    }

    override def visitSetExpr(expr: Expr.Set): Any = {
        val obj = evaluate(expr.obj)
        if (!obj.isInstanceOf[LoxInstance]) {
            throw new RuntimeError(expr.name, "Only instances have fields.")
        }
        val value = evaluate(expr.value)
        obj.asInstanceOf[LoxInstance].set(expr.name, value)
        value
    }

    override def visitThisExpr(expr: Expr.This): Any = {
        lookUpVariable(expr.keyword, expr)
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
        lookUpVariable(expr.name, expr)
    }

    private def lookUpVariable(name: Token, expr: Expr): Any = {
        var distance: Integer = locals.get(expr)
        if (distance != null) {
            return environment.getAt(distance, name.lexeme)
        } else {
            return globals.get(name)
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

    private def execute(stmt: Stmt): Unit = {
        stmt.accept(this)
    }

    private[lox] def resolve(expr: Expr, depth: Int): Unit = {
        locals.put(expr, depth)
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

    override def visitClassStmt(stmt: Stmt.Class): Unit = {
        var superclass: Any = null
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass)
            if (!superclass.isInstanceOf[LoxClass]) {
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.")
            }
        }
        environment.define(stmt.name.lexeme, null)
        val methods = new java.util.HashMap[String, LoxFunction]()
        for (method <- stmt.methods.asScala) {
            val function = new LoxFunction(method, environment, false)
            methods.put(method.name.lexeme, function)
        }
        val klass = new LoxClass(stmt.name.lexeme,(LoxClass)superclass, methods)
        environment.assign(stmt.name, klass)
    }


    override def visitExpressionStmt(stmt: Stmt.Expression): Unit = {
        evaluate(stmt.expression)
        ()
    }

    override def visitFunctionStmt(stmt: Stmt.Function): Unit = {
        val function = new LoxFunction(stmt, environment, false)
        environment.define(stmt.name.lexeme, function)
        ()
    }

    override def visitIfStmt(stmt: Stmt.If): Unit = {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.thenBranch)
        }
        else if (stmt.elseBranch != null) {
            execute(stmt.elseBranch)
        }
        ()
    }

    override def visitPrintStmt(stmt: Stmt.Print): Unit = {
        val value = evaluate(stmt.expression)
        println(stringify(value))
        ()
    }

    override def visitReturnStmt(stmt: Stmt.Return): Unit = {
        var value: Any = null
        if (stmt.value != null) value = evaluate(stmt.value)
        throw new Return(value)
    }

    override def visitVarStmt(stmt: Stmt.Var): Unit = {
        var value: Any = null
        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer)
        }
        environment.define(stmt.name.lexeme, value)
        ()
    }

    override def visitWhileStmt(stmt: Stmt.While): Unit = {
        while (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.body)
        }
    }

    override def visitAssignExpr(expr: Expr.Assign): Any = {
        val value = evaluate(expr.value)
        var distance: Integer = locals.get(expr)
        if (distance != null) {
            environment.assignAt(distance, expr.name, value)
        } else {
            globals.assign(expr.name, value)
        }
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

    override def visitCallExpr(expr: Expr.Call): Any = {
        val callee = evaluate(expr.callee)
        val arguments = new java.util.ArrayList[Any]()
        for (argument <- expr.arguments) {
            arguments.add(evaluate(argument))
        }
        if (!callee.isInstanceOf[LoxCallable]) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.")
        }
        val function = callee.asInstanceOf[LoxCallable]
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren, s"Expected ${function.arity()} arguments but got ${arguments.size()}.")
        }
        function.call(this, arguments)
    }

    override def visitGetExpr(expr: Expr.Get): Any = {
        val obj = evaluate(expr.obj)
        obj match {
            case instance: LoxInstance => instance.get(expr.name)
            case _ => throw new RuntimeError(expr.name, "Only instances have properties.")
        }
    }

    private def checkNumberOperands(operator: Token, left: Any, right: Any): Unit = {
        (left, right) match {
            case (_: Double, _: Double) => ()
            case _ => throw new RuntimeError(operator, "Operands must be numbers.")
        }
    }
}