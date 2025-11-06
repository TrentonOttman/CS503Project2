package com.craftinginterpreters.tool

import java.io.IOException
import java.io.PrintWriter
import java.io.File
import java.util.Arrays

object GenerateAst {
    def main(args: Array[String]): Unit = {
        try {
            if (args.length != 1) {
                println("Usage: generate_ast <output directory>")
                System.exit(64)
            }
            val outputDir = args(0)
            defineAst(outputDir, "Expr", List(
                "Binary   : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal  : Any value",
                "Unary    : Token operator, Expr right"
            ))
        } catch {
            case e: IOException => println("IOException occurred.")
        }
    }

    private def defineAst(outputDir: String, baseName: String, types: List[String]): Unit = {
        val dir = new File(outputDir)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = new File(dir, s"$baseName.scala")
        val writer = new PrintWriter(file, "UTF-8")

        writer.println("package com.craftinginterpreters.lox")
        writer.println()
        writer.println("trait " + baseName + " {")
        writer.println("  def accept[R](visitor: " + baseName + ".Visitor[R]): R")
        writer.println("}")
        writer.println()
        writer.println("object " + baseName + " {")

        defineVisitor(writer, baseName, types)

        for (t <- types) {
            val className = t.split(":").head.trim()
            val fields = t.split(":").last.trim()
            writer.println()
            defineType(writer, baseName, className, fields)
        }
        writer.println("}")
        writer.close()
    }

    private def defineVisitor(writer: PrintWriter, baseName: String, types: List[String]): Unit = {
        writer.println("  trait Visitor[R] {")

        for (t <- types) {
            var typeName: String = t.split(":").head.trim()
            writer.println("    def visit" + typeName + baseName + "(" + baseName.toLowerCase + ": " + typeName + "): R")
        }

        writer.println("  }")
    }

    private def defineType(writer: PrintWriter, baseName: String, className: String, fieldList: String): Unit = {
        writer.println("  case class " + className + "(")
        val fields = fieldList.split(", ").map(_.trim)
        fields.zipWithIndex.foreach { case (field, i) =>
            val parts = field.split(" ")
            val tpe = parts(0)
            val name = parts(1)
            val comma = if (i < fields.length - 1) "," else ""
            writer.println("    " + name + ": " + tpe + comma)
        }
        
        writer.println("  ) extends " + baseName + " {")
        writer.println("    def accept[R](visitor: Visitor[R]): R = {")
        writer.println("      visitor.visit" + className + baseName + "(this)")
        writer.println("    }")
        writer.println("  }")
    }
}

