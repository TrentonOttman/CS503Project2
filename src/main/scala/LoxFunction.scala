package com.craftinginterpreters.lox

import java.util
import scala.jdk.CollectionConverters._

class LoxFunction(
    val declaration: Stmt.Function,
    val closure: Environment,
    val isInitializer: Boolean
) extends LoxCallable {

    override def call(interpreter: Interpreter, arguments: util.List[Any]): Any = {
        val environment = new Environment(closure)
        for (i <- 0 until declaration.params.size) {
            environment.define(declaration.params(i).lexeme, arguments.get(i))
        }
        try {
            interpreter.executeBlock(declaration.body, environment)
        } catch {
            case returnValue: Return =>
                if (isInitializer) return closure.getAt(0, "this")
                return returnValue.value
        }
        if (isInitializer) return closure.getAt(0, "this")
        null
    }

    override def arity(): Int = declaration.params.size

    def bind(instance: LoxInstance): LoxFunction = {
        val environment = new Environment(closure)
        environment.define("this", instance)
        new LoxFunction(declaration, environment, isInitializer)
    }

    override def toString: String = s"<fn ${declaration.name.lexeme}>"
}
