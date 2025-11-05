//Part of chapter 6
package com.craftinginterpreters.lox


class Parser(tokens: List[Token]):
    var current = 0

    def parse(): Expr = expression()

    def expression(): Expr = equality()

    def equality(): Expr = 
        var expr = comparison()
        while 
