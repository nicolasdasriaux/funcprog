package error.io

import java.io.IOException
import scala.io.StdIn

object Console {
  def readLine: IO[IOException, String] =
    IO.attempt(StdIn.readLine())
      .refineToOrDie[IOException]

  def printLine(o: Any): IO[IOException, Unit] =
    IO.attempt(println(o))
      .refineToOrDie[IOException]
}
