package com.craftinginterpreters.lox

import java.util.List
import com.craftinginterpreters.lox.TokenType._
import scala.util.boundary, boundary.break
import java.util.ArrayList
import scala.jdk.CollectionConverters._
import java.util.Arrays

class Parser (private val tokens: List[Token]) {
    private var current: Int = 0
        
    def parse(): List[Stmt] = {
        var statements: List[Stmt] = new ArrayList
        while (!isAtEnd()) {
            statements.add(declaration())
        }
        statements
    }

    private def expression(): Expr = assignment()

    private def declaration(): Stmt = {
        try {
            if (matchTypes(TokenType.FUN)) return function("function")
            if (matchTypes(TokenType.VAR)) return varDeclaration()
            statement()
        } catch {
            case _: ParseError =>
            synchronize()
            null
        }
    }

    private def statement(): Stmt = {
        if (matchTypes(TokenType.FOR)) return forStatement()
        if (matchTypes(TokenType.IF)) return ifStatement()
        if (matchTypes(TokenType.PRINT)) return printStatement()
        if (matchTypes(TokenType.WHILE)) return whileStatement()
        if (matchTypes(TokenType.LEFT_BRACE)) return Stmt.Block(block().asScala.toList)
        expressionStatement()
    }

    private def forStatement(): Stmt = {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.")
        var initializer: Stmt = null
        if (matchTypes(TokenType.SEMICOLON)) {
            initializer = null
        }
        else if (matchTypes(TokenType.VAR)) {
            initializer = varDeclaration()
        }
        else {
            initializer = expressionStatement()
        }

        var condition: Expr = null
        if (!check(TokenType.SEMICOLON)) {
            condition = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after loop condition.")

        var increment: Expr = null
        if (!check(TokenType.RIGHT_PAREN)) {
            increment = expression()
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.")
        
        var body = statement()

        if (increment != null) {
            body = new Stmt.Block(scala.List(
                body,
                Stmt.Expression(increment)
            ))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(scala.List(initializer, body))
        }

        body
    }


    private def ifStatement(): Stmt = {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        var elseBranch: Stmt = null
        if (matchTypes(ELSE)) {
            elseBranch = statement()
        }
        Stmt.If(condition, thenBranch, elseBranch)
    }

    private def printStatement(): Stmt = {
        val value = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after value.")
        Stmt.Print(value)
    }

    private def varDeclaration(): Stmt = {
        val name = consume(TokenType.IDENTIFIER, "Expect variable name.")
        var initializer: Expr = null
        if (matchTypes(TokenType.EQUAL)) {
            initializer = expression()
        }
        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.")
        Stmt.Var(name, initializer)
    }

    private def whileStatement(): Stmt = {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.")
        val body = statement()

        Stmt.While(condition, body)
    }

    private def expressionStatement(): Stmt = {
        val expr = expression()
        consume(TokenType.SEMICOLON, "Expect ';' after expression.")
        Stmt.Expression(expr)
    }

    private def function(kind: String): Stmt.Function = {
        val name = consume(TokenType.IDENTIFIER, s"Expect $kind name.")
        consume(TokenType.LEFT_PAREN, s"Expect '(' after $kind name.")
        val parameters = new java.util.ArrayList[Token]()
        if (!check(TokenType.RIGHT_PAREN)) {
            var first = true
            var continue = true
            while (continue) {
                if (!first) advance()
                first = false
                if (parameters.size() >= 255) {
                error(peek(), "Can't have more than 255 parameters.")
                }
                parameters.add(
                consume(TokenType.IDENTIFIER, "Expect parameter name.")
                )
                continue = matchTypes(TokenType.COMMA)
            }
        }
        consume(TokenType.RIGHT_PAREN, "Expect ')' after parameters.")
        consume(TokenType.LEFT_BRACE, s"Expect '{' before $kind body.")
        val body = block()
        new Stmt.Function(name, parameters, body)
    }

    private def block(): List[Stmt] = {
        var statements: List[Stmt] = new ArrayList

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration())
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.")
        statements
    }

    private def assignment(): Expr = {
        var expr = or()
        if (matchTypes(TokenType.EQUAL)) {
            val equals = previous()
            val value = assignment()
            expr match {
            case variable: Expr.Variable =>
                val name = variable.name
                return Expr.Assign(name, value)
            case _ => error(equals, "Invalid assignment target.")
            }
        }
        expr
    }

    private def or(): Expr = {
        var expr = and()
        while (matchTypes(TokenType.OR)) {
            val operator = previous()
            val right = and()
            expr = Expr.Logical(expr, operator, right)
        }
        expr
    }

    private def and(): Expr = {
        var expr = equality()
        while (matchTypes(TokenType.AND)) {
            val operator = previous()
            val right = equality()
            expr = Expr.Logical(expr, operator, right)
        }
        expr
    }

    private def equality(): Expr = {
        var expr = comparison()
        while(matchTypes(BANG_EQUAL, EQUAL_EQUAL)) {
            val operator = previous()
            val right = comparison()
            expr = Expr.Binary(expr, operator, right)
        }
        expr
    }

    private def comparison(): Expr = {
        var expr = term()
        while (matchTypes(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            val operator = previous()
            val right = term()
            expr = Expr.Binary(expr, operator, right)
        }
        expr
    }

    private def term(): Expr = {
        var expr = factor()
        while (matchTypes(TokenType.MINUS, TokenType.PLUS)) {
            val operator = previous()
            val right = factor()
            expr = Expr.Binary(expr, operator, right)
        }
        expr
    }

    private def factor(): Expr = {
        var expr = unary()
        while (matchTypes(TokenType.SLASH, TokenType.STAR)) {
            val operator = previous()
            val right = unary()
            expr = Expr.Binary(expr, operator, right)
        }
        expr
    }

    private def unary(): Expr = {
        if (matchTypes(TokenType.BANG, TokenType.MINUS)) {
            val operator = previous()
            val right = unary()
            return Expr.Unary(operator, right)
        }
        call()
    }

    private def call(): Expr = {
        var expr = primary()
        while (true) {
            if (matchTypes(TokenType.LEFT_PAREN)) {
            expr = finishCall(expr)
            }
            else {
                expr
            }
        }
        expr
    }

    private def finishCall(callee: Expr): Expr = {
        import scala.collection.mutable.ListBuffer

        val arguments = ListBuffer[Expr]()
        if (!check(TokenType.RIGHT_PAREN)) {
            arguments += expression()
            while (matchTypes(TokenType.COMMA)) {
            if (arguments.size >= 255) {
                error(peek(), "Can't have more than 255 arguments.")
            }
            arguments += expression()
            }
        }

        val paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.")
        Expr.Call(callee, paren, arguments.toList)
    }

    private def primary(): Expr = {
        if (matchTypes(TokenType.FALSE)) return Expr.Literal(false)
        if (matchTypes(TokenType.TRUE))  return Expr.Literal(true)
        if (matchTypes(TokenType.NIL))   return Expr.Literal(null)
        if (matchTypes(TokenType.NUMBER, TokenType.STRING)) return Expr.Literal(previous().literal)
        if (matchTypes(TokenType.IDENTIFIER)) {
            return Expr.Variable(previous())
        }
        if (matchTypes(TokenType.LEFT_PAREN)) {
            val expr = expression()
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.")
            return Expr.Grouping(expr)
        }
        throw error(peek(), "Expect expression.")
    }

    private def matchTypes(types: TokenType*): Boolean = {
        var matched = false
        for (typ <- types if !matched) {
            if (check(typ)) {
                advance()
                matched = true
            }
        }
        matched
    }

    private def check(tpe: TokenType): Boolean = {
        if (isAtEnd()) false else peek().tokenType == tpe
    }

    private def consume(tpe: TokenType, message: String): Token = {
        if (check(tpe)) return advance()
        throw error(peek(), message)
    }

    private def advance(): Token = {
        if(!isAtEnd()) current += 1
        previous()
    }

    private def isAtEnd(): Boolean = {
        peek().tokenType == TokenType.EOF
    }

    private def peek(): Token = {
        tokens.get(current)
    }

    private def previous(): Token = {
        tokens.get(current - 1)
    }

    private def error(token: Token, message: String): ParseError = {
        Lox.error(token, message)
        new ParseError
    }

    private def synchronize(): Unit = {
        advance()
        while (!isAtEnd()) {
            if (previous().tokenType == TokenType.SEMICOLON) return
            peek().tokenType match {
            case TokenType.CLASS |
                TokenType.FUN |
                TokenType.VAR |
                TokenType.FOR |
                TokenType.IF |
                TokenType.WHILE |
                TokenType.PRINT |
                TokenType.RETURN =>
                return
            case _ =>
            }
            advance()
        }
    }

    private class ParseError extends RuntimeException //this part might be wrong im not sure by where it wants it nested inside
}