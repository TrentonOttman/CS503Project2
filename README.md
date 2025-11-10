# CS503 Project 2 | Lox Interpreter Written in Scala
- Trenton Ottman and Daniel Lecorchick
- CS503 Section 001
- This file contains README, Test Plan, Test Results, and Scala Feedback for our Lox interpreter
- https://github.com/TrentonOttman/CS503Project2

## README 
This project is an implementation of a Lox interpreter written in Scala based on chapters 4-13 of the book Crafting Interpreters by Robert Nystrom.
https://craftinginterpreters.com/

Lox is a programming languange created by Robert Nystrom that has basic functionality you would expect to find in any language as well as classes and inheritance. To see the complete syntax and original Java implementation check out his website linked above. Our implementation is a direct translation of the code featured in his book from Java to Scala.

### Required Software
- Java (specifically 17.0.12 was used, but any modern version should work)
- Scala sbt (1.11.7) https://www.scala-sbt.org/download/
- Metals VSCode Extension

### Organization of Code
- src/main/scala contains all of the Scala source code, a tool directory, and a com/craftinginterpreters/lox directory.
- The tool directory contains GenerateAst.scala which was used to generate Scala code.
- The com/craftinginterpreters/lox directory is where generated code is stored.
- Test code can be found in /LoxTestFiles

### Compilation Instructions
- To run the REPL interpreter, enter the command `sbt run` and when prompted press `1`
- To run a prewritten Lox test file, enter the command `sbt "run LoxTestFiles/fileName.lox"` and when prompted press `1`
- Generated files were built with `sbt "runMain com.craftinginterpreters.tool.GenerateAst src/main/scala/com/craftinginterpreters/lox"`

## Test Plan

## Test Results

## Scala Feedback

