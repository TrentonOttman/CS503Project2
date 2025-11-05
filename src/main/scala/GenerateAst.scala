package com.craftinginterpreters.tool

import java.io.IOException
import java.io.PrintWriter
import java.util.Arrays
import java.util.List

def GenerateAst(args: String*): Unit =
    if args.length != 1 then
        System.err.println("Usage: generateSst <output dir>")
        sys.exit(64)

    val outputDir = args.head
    defineAst(
        outputDir, "Expr", List(
            "Binary   : Expr left, Token operator, Expr right",
            "Grouping : Expr expression",
            "Literal  : Any | Null value",
            "Unary    : Token operator, Expr right"
        )
    )

private def defineAst(outputDir: String, baseName: String, types: List[String]): Unit =
    val path = s"$outputDir/$baseName.scala"
    val writer = new PrintWriter(path, "UTF-8")

    writer.println("package com.craftinginterpreters.lox")
    writer.println()
    writer.println(s"abstract class $baseName {")
    writer.println("}")

    for t <- types do
        val parts = t.split(":").map(_.trim)
        val className = parts(0)
        val fields = parts(1)
        defineType(writer, baseName, className, fields)

    writer.close()

    private def defineType(
        writer: PrintWriter,
        baseName: String,
        className: String,
        fieldList: String
    ): Unit =
        writer.println(s"class $className($fieldList) extends $baseName {")
        writer.println()
        writer.println(s"    // Fields")
        val fields = fieldList.split(", ").map(_.trim)
        for f <- fields do
            writer.println(s"    val ${f.split(' ')(1)}: ${f.split(' ')(0)}")
        writer.println("  }")