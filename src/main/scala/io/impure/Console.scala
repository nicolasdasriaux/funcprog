package io.impure

import io.pure.IO

import scala.io.StdIn

object Console {
  def printLine(o: Any): Unit = println(o)
  def readLine(): String = StdIn.readLine()
}
