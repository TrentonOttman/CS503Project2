package com.craftinginterpreters.lox

import java.util.List

class LoxFunction(private val declaration: Stmt.Function) extends LoxCallable {
    
    override def call(interpreter: Interpreter, arguments: java.util.List[Any]): Any = {
        val environment = new Environment(interpreter.globals)
        for (i <- 0 until declaration.params.size) {
            environment.define(
            declaration.params.get(i).lexeme,
            arguments.get(i)
            )
        }
        interpreter.executeBlock(declaration.body, environment)
        null
    }

    override def arity(): Int = {
        declaration.params.size
    }

    override def toString: String = {
        s"<fn ${declaration.name.lexeme}>"
    }


}
