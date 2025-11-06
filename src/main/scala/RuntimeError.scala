package com.craftinginterpreters.lox

class RuntimeError(val token: Token, message: String) extends RuntimeException(message)