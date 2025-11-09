package com.craftinginterpreters.lox

import java.util.HashMap
import java.util.Map

class LoxInstance(val klass: LoxClass) {

    private val fields = new java.util.HashMap[String, Any]()

    def get(name: Token): Any = {
        if (fields.containsKey(name.lexeme)) {
            return fields.get(name.lexeme)
        }
        val method = klass.findMethod(name.lexeme)
        if (method != null) return method.bind(this)
        throw new RuntimeError(name, s"Undefined property '${name.lexeme}'.")
    }

    def set(name: Token, value: Any): Unit = {
        fields.put(name.lexeme, value)
    }

    override def toString: String = s"${klass.name} instance"
}
