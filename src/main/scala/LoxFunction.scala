package com.craftinginterpreters.lox

import java.util.List
import collection.JavaConverters._

class LoxFunction(private val declaration: Stmt.Function, private val closure: Environment) extends LoxCallable {
    
    override def call(interpreter: Interpreter, arguments: java.util.List[Any]): Any = {
        val environment = new Environment(closure)
        for (i <- 0 until declaration.params.size) {
            environment.define(
            declaration.params(i).lexeme,
            arguments.get(i)
            )
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch {
            case returnValue: Return => return returnValue.value
        }
        null
    }

    override def arity(): Int = {
        declaration.params.size
    }

    override def toString: String = {
        s"<fn ${declaration.name.lexeme}>"
    }


}
