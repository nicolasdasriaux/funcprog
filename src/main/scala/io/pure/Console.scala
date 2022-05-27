package io.pure

import scala.io.StdIn

object Console {
  def printLine(o: Any): IO[Unit] = IO.succeed(println(o))
  val readLine: IO[String] = IO.succeed(StdIn.readLine())
}
