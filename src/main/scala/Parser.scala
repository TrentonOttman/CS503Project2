//parser completed up to chapter 6.3 rn

package com.craftinginterpreters.lox

import java.util.List
import static com.craftinginterpreters.lox.TokenType.*

class Parser (private val tokens: List[Tokens]) {
    private var current: Int = 0
    
    private class ParseError extends RuntimeException //this part might be wrong im not sure by where it wants it nested inside
}


def parse(): Expr = {
    try {
        expression()
    } catch {
        case _: ParseError => null
    }
}


private def expression(): Expr = equality()


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


private def consume(tpe: TokenType, message: String): Token = {
    if (check(tpe)) return advance()
    throw error(peek(), message)
}


private def advance(): Token = {
    if(!isAtEnd()) current += 1
    previous()
}


private def isAtEnd(): boolean = {
    peek().tokenType == tokenType.EOF
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
    while (!isAtEnd) {
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
