package io.pure

import scala.io.StdIn

object Console {
  def printLine(o: Any): IO[Unit] = IO.attempt(println(o))
  val readLine: IO[String] = IO.attempt(StdIn.readLine())
}
