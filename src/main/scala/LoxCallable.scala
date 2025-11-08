package com.craftinginterpreters.lox

import java.util.List

trait LoxCallable {
    def call(interpreter: Interpreter, arguments: java.util.List[Any]): Any
    def arity(): Int
}
