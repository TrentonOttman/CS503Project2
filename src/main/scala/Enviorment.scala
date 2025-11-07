package com.craftinginterpreters.lox

import java.util.HashMap
import java.util.Map
import scala.collection.mutable

class Environment(val enclosing: Environment = null) {

    private val values = mutable.Map[String, Any]()

    def define(name: String, value: Any): Unit = {
        values(name) = value
    }

    def get(name: Token): Any = {
        values.get(name.lexeme) match {
            case Some(value) => value
            case None =>
                if (enclosing != null)
                    enclosing.get(name)
                else
                    throw new RuntimeError(name, s"Undefined variable '${name.lexeme}'.")
        }
    }

    def assign(name: Token, value: Any): Unit = {
        if (values.contains(name.lexeme)) {
            values(name.lexeme) = value
            return
        }
        if (enclosing != null) {
            enclosing.assign(name, value)
            return
        }
        throw new RuntimeError(name, s"Undefined variable '${name.lexeme}'.")
    }
}
