package com.craftinginterpreters.lox

import scala.io.Source
import scala.util.Try
import scala.jdk.CollectionConverters._

case class TestCase(name: String, code: String, expected: String)
case class TestResults(var total: Int = 0, var passed: Int = 0, var failed: Int = 0, failedNames: collection.mutable.Buffer[String] = collection.mutable.Buffer.empty)

object TestRunner {
  def main(args: Array[String]): Unit = {
    val filename = if (args.nonEmpty) args(0) else "test_basic.lox"
    println("==================================================")
    println("LOX INTERPRETER TEST")
    println("==================================================")
    println(s"File: $filename\n")

    val tests = loadTests(filename)
    val results = TestResults()

    for (test <- tests) {
      runTest(test, results)
    }

    println("\n==================================================")
    println("SUMMARY")
    println("==================================================")
    println(s"Total:  ${results.total}")
    println(s"Passed: ${results.passed}")
    println(s"Failed: ${results.failed}")
    if (results.total > 0)
      println(f"Rate:   ${results.passed * 100.0 / results.total}%.1f%%")

    if (results.failed > 0) {
      println("\nFailed Tests:")
      results.failedNames.foreach(n => println(s"  - $n"))
    }
    println("==================================================")
  }

  def loadTests(file: String): Seq[TestCase] = {
    val lines = Source.fromFile(file).getLines().toList
    lines.flatMap { line =>
      val parts = line.split("\\|").map(_.trim)
      if (parts.length == 3 && !parts(0).startsWith(";")) Some(TestCase(parts(0), parts(1), parts(2)))
      else None
    }
  }

  def runTest(test: TestCase, results: TestResults): Unit = {
    results.total += 1
    val output = captureOutput {
      Try {
        val interpreter = new Interpreter()
        val scanner = new Scanner(test.code)
        val tokens = scanner.scanTokens()
        val parser = new Parser(tokens)
        val statements = parser.parse()
        interpreter.interpret(statements.asScala.toList)
      }.recover { case e => println(s"ERROR: ${e.getMessage}") }
    }

    val actual = output.trim
    val expected = test.expected.trim

    val isErrorExpected = expected.equalsIgnoreCase("ERROR") || expected.startsWith("ERROR")
    val isErrorOutput = actual.toLowerCase.contains("error")

    if ((isErrorExpected && isErrorOutput) || actual == expected) {
      println(s"PASS ${test.name} | Expr: ${test.code} | Expected: $expected | Got: $actual")
      results.passed += 1
    } else {
      println(s"FAIL ${test.name} | Expr: ${test.code} | Expected: $expected | Got: $actual")
      results.failed += 1
      results.failedNames += test.name
    }
  }

  private def captureOutput(block: => Unit): String = {
    val stream = new java.io.ByteArrayOutputStream()
    Console.withOut(stream)(block)
    stream.toString()
  }
}
