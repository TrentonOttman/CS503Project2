package com.craftinginterpreters.lox

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

import com.craftinginterpreters.lox.TokenType._

class Scanner(val source: String) {
    private val tokens: List[Token] = new ArrayList[Token]()
    private var start: Int = 0
    private var current: Int = 0
    private var line: Int = 0

    def scanTokens(): List[Token] = {
        while (!isAtEnd()) {
            // Beginning of next lexeme
            start = current
            scanToken()
        }
        tokens.add(new Token(EOF, "", null, line))
        tokens
    }

    private def scanToken(): Unit = {
        var c: Character = advance()
        c match {
            case '(' => addToken(LEFT_PAREN)
            case ')' => addToken(RIGHT_PAREN)
            case '{' => addToken(LEFT_BRACE)
            case '}' => addToken(RIGHT_BRACE)
            case ',' => addToken(COMMA)
            case '.' => addToken(DOT)
            case '-' => addToken(MINUS)
            case '+' => addToken(PLUS)
            case ';' => addToken(SEMICOLON)
            case '*' => addToken(STAR)
            case '!' => if (matchChar('=')) {
                            addToken(BANG_EQUAL)
                        } else {
                            addToken(BANG)
                        }
            case '=' => if (matchChar('=')) {
                            addToken(EQUAL_EQUAL)
                        } else {
                            addToken(EQUAL)
                        }
            case '<' => if (matchChar('=')) {
                            addToken(LESS_EQUAL)
                        } else {
                            addToken(LESS)
                        }
            case '>' => if (matchChar('=')) {
                            addToken(GREATER_EQUAL)
                        } else {
                            addToken(GREATER)
                        }
            case '/' => if (matchChar('/')) {
                            while (peek() != '\n' && !isAtEnd()) advance()
                        } else {
                            addToken(SLASH)
                        }
            case ' ' =>
            case '\r' =>
            case '\t' =>
            case '\n' => line += 1
            case '"' => string()
            case _ => if (isDigit(c)) {
                        number()
                    } else {
                        Lox.error(line, "Unexpected character.")
                    }
        }
    }

    private def number(): Unit = {
        while (isDigit(peek())) advance()

        if (peek() == '.' && isDigit(peekNext())) {
            advance()

            while (isDigit(peek())) advance()
        }

        addToken(NUMBER, source.substring(start, current).toDouble)
    }

    private def string(): Unit = {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line += 1
            advance()
        }

        if (isAtEnd()) {
            Lox.error(line, "Undetermined string.")
            return
        }

        advance()

        var value: String = source.substring(start+1, current-1)
        addToken(STRING, value)
    }

    private def matchChar(expected: Character): Boolean = {
        if (isAtEnd()) return false
        if (source.charAt(current) != expected) return false

        current += 1
        true
    }

    private def peek(): Character = {
        if (isAtEnd()) return '\u0000'
        source.charAt(current)
    }

    private def peekNext(): Character = {
        if (current+1 >= source.length()) return '\u0000'
        return source.charAt(current+1)
    }

    private def isDigit(c: Character): Boolean = {
        c >= '0' && c <= '9'
    }

    private def isAtEnd(): Boolean = {
        current >= source.length()
    }

    private def advance(): Character = {
        var char: Character = source.charAt(current)
        current += 1
        char
    }

    private def addToken(tokenType: TokenType): Unit = {
        addToken(tokenType, null)
    }

    private def addToken(tokenType: TokenType, literal: Any): Unit = {
        var text: String = source.substring(start, current)
        tokens.add(new Token(tokenType, text, literal, line))
    }
}