
package com.craftinginterpreters.lox

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths
import java.util.List
import scala.util.control.Breaks._
import java.util.{Scanner => JavaScanner}
import scala.compiletime.ops.boolean._
import com.craftinginterpreters.lox.Scanner

object Lox {
  var hadError: Boolean = false;
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
      var bytes: Array[Byte] = Files.readAllBytes(Paths.get(path))
      run(new String(bytes, Charset.defaultCharset()))

      // Indicate an error in the exit code.
      if (hadError) System.exit(65);
    } catch {
      case e: IOException => println("IOException occurred.")
    }
  }

  private def runPrompt(): Unit = {
    try {
      var input: InputStreamReader = new InputStreamReader(System.in)
      var reader: BufferedReader = new BufferedReader(input)

      while (true) {
        print("> ")
        var line: String = reader.readLine()
        if (line == null) break
        run(line)
        hadError = false
      }
    } catch {
      case e: IOException => println("IOException occurred.")
    }
  }

  private def run(source: String): Unit = {
    var scanner: Scanner = new Scanner(source)
    var tokens: List[Token] = scanner.scanTokens()
    tokens.forEach(println)
  }

  def error(line: Int, message: String): Unit = {
    report(line, "", message)
  }

  private def report(line: Int, where: String, message: String): Unit = {
    println("[line " + line + "] Error" + where + ": " + message)
    hadError = true
  }
}
