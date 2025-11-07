package com.craftinginterpreters.lox

import java.util.Arrays
import java.util.List
import java.util.ArrayList
import com.craftinginterpreters.lox.TokenType._

class Parser (private val tokens: List[Token]) {
    private var current: Int = 0
        
    def parse(): List[Stmt] = {
        var statements = List.empty[Stmt]
        while (!isAtEnd()) {
            statements = statements :+ declaration()
        }
        statements
    }

    private def expression(): Expr = assignment()

    private def declaration(): Stmt = {
        try {
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
        if (matchTypes(TokenType.WHILE)) return whileStatement()
        if (matchTypes(TokenType.PRINT)) return printStatement()
        if (matchTypes(TokenType.LEFT_BRACE)) return Stmt.Block(block())
        expressionStatement()
    }

    //theres a lot of stuff in this one there might be an error in my code idk
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
            body = Stmt.Block(List(
                body,
                Stmt.Expression(increment)
            ))
        }

        if (condition == null) condition = Expr.Literal(true)
        body = Stmt.While(condition, body)

        if (initializer != null) {
            body = Stmt.Block(List(initializer, body))
        }

        body
    }

    private def ifStatement(): Stmt = {
        consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.")
        val condition = expression()
        consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.")
        val thenBranch = statement()
        val elseBranch =
            if (matchTypes(TokenType.ELSE)) Some(statement())
            else None
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

    private def block(): List[Stmt] = {
        var statements = List.empty[Stmt]

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            statements = statements :+ declaration()
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
        primary()
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
        for (typ <- types) {
            if (check(typ)) {
                advance()
                return true
            }
        }
        false
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

    private class ParseError extends RuntimeException
}
