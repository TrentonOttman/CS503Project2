# CS503 Project 2 | Lox Interpreter Written in Scala
- Trenton Ottman and Daniel Lecorchick
- CS503 Section 001
- This file contains README, Test Plan, Test Results, and Scala Feedback for our Lox interpreter
- Based on the book Crafting Interpreters by Robert Nystrom
- https://github.com/TrentonOttman/CS503Project2

## README 
This project is an implementation of a Lox interpreter written in Scala based on chapters 4-13 of the book Crafting Interpreters by Robert Nystrom.
https://craftinginterpreters.com/

Lox is a programming languange created by Robert Nystrom that has basic functionality you would expect to find in any language as well as classes and inheritance. To see the complete syntax and original Java implementation check out his website linked above. Our implementation is a direct translation of the code featured in his book from Java to Scala.

### Required Software
- Java (specifically 17.0.12 was used, but any modern version should work)
- Scala sbt (1.11.7) https://www.scala-sbt.org/download/
- Scala (Metals) VSCode Extension

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

NOTE: Tests for Chapter 11, 12, and 13 did not work in the TestRunner.Lox file and were manually verfiried by hand.

### Chapter 4 Scanning
- LoxTestResults/chapter4.txt - 27/27

### Chapter 5 Representing Code
- LoxTestResults/chapter5.txt - 29/29

### Chapter 6 Parsing Expressions
- LoxTestResults/chapter6.txt - 19/19

### Chapter 7 Evalauation Expressions
- LoxTestResults/chapter7.txt - 21/21

### Chapter 8 Statements and State
- LoxTestResults/chapter8.txt - 20/20

### Chapter 9 Control Flow
- LoxTestResults/chapter9.txt - 10/10

### Chapter 10 Functions
- LoxTestResults/chapter10.txt - 7/7

### Chapter 11 Resolving and Binding
- LoxTestResults/chapter11.txt - 9/9

### Chapter 12 Classes
- LoxTestResults/chapter12.txt - 8/8

### Chapter 13 Inheritance
- LoxTestResults/chapter13.txt - 9/9

### Closures, Inheritance, and Recursion Test Files
- No results file, 3/3

## Scala Feedback

