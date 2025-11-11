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
- Test cases can be found in /LoxTestFiles and test results can be found in /LoxTestResults

### Compilation Instructions
- To run the REPL interpreter, enter the command `sbt run` and when prompted press `1`
- To run a prewritten Lox test file, enter the command `sbt "run LoxTestFiles/fileName.lox"` and when prompted press `2`
- Generated files were built with `sbt "runMain com.craftinginterpreters.tool.GenerateAst src/main/scala/com/craftinginterpreters/lox"`

## Test Plan
Our methodology for testing the Lox interpreter was to think of the core functionality of the language implemented by each chapter, and to create as many test cases as possible to confirm the validity of our implementation. 

## Test Results
Specific test cases can be found in /LoxTestFiles and specific test results can be found in /LoxTestResults. These results can be verified by running the command mentioned in Compilation Instructions.

### Chapter 4 Scanning
### Chapter 5 Representing Code
### Chapter 6 Parsing Expressions
### Chapter 7 Evalauation Expressions
### Chapter 8 Statements and State
### Chapter 9 Control Flow
### Chapter 10 Functions
### Chapter 11 Resolving and Binding
### Chapter 12 Classes
### Chapter 13 Inheritance

## Scala Feedback

