package com.craftinginterpreters.lox

import java.util.List
import java.util.Map


class LoxClass(val name: String, val superclass: LoxClass, val methods: java.util.Map[String, LoxFunction]) extends LoxCallable {

    override def arity(): Int = {
        val initializer = findMethod("init")
        if (initializer == null) 0 else initializer.arity()
    }

    override def call(interpreter: Interpreter, arguments: java.util.List[Any]): Any = {
        val instance = new LoxInstance(this)
        val initializer = findMethod("init")
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments)
        }
        instance
    }

    def findMethod(name: String): LoxFunction = {
        if (methods.containsKey(name)) {
            return methods.get(name)
        }
        if (superclass != null) {
            return superclass.findMethod(name)
        }
        null
    }

    override def toString: String = name
}


