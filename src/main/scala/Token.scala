package com.craftinginterpreters.lox

class Token(val tokenType: TokenType, val lexeme: String, val literal: Any, val line: Int) {
    override def toString(): String = {
        s"$tokenType $lexeme $literal"
    }
}
