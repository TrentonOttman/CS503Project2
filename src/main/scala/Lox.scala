
package com.craftinginterpreters.lox

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

object Lox {
  def main(args: Array[String]): Unit = {
    try {
      if (args.length > 1) {
        println("Usage: jlox [script]")
        System.exit(64)
      } else if (args.length == 1) {
        runFile(args(0))
      } else {
        runPrompt()
      }
    } catch {
      case e: IOException => println("IOException occurred.")
    }
  }

  private def runFile(path: String): Unit = {
    try {

    } catch {
      case e: IOException => println("IOException occurred.")
    }
  }

  private def runPrompt(): Unit = {
    try {

    } catch {
      case e: IOException => println("IOException occurred.")
    }
  }
}
