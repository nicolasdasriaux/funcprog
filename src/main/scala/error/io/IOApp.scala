package error.io

import error.either.Either

import java.io.IOException
import java.sql.SQLException

object IOApp {
  def main(args: Array[String]): Unit = {
    test("IO.succeed") {
      IO.succeed(1000 / 0)
    }

    test("IO.fail") {
      IO.fail(1000 / 0)
    }

    test("IO.attempt") {
      IO.attempt(1000 / 0)
    }

    test("IO.map") {
      IO.succeed(0).map(i => 1000 / i)
    }

    test("IO.flatMap") {
      IO.succeed(0).flatMap({ i => val j = 1000 / i; IO.succeed(j)})
    }

    test("IO.refineToOrDie[IOException]") {
      IO.attempt(throw new IOException("Boom")).refineToOrDie[IOException]
    }

    test("IO.refineToOrDie[IOException]") {
      IO.attempt(throw new RuntimeException("Boom")).refineToOrDie[SQLException]
    }

    def test[E, A](name: String)(io: IO[E, A]): Unit = {
      val exit = Runtime.unsafeRun(io)
      println(s"$name --> $exit")
    }

    object IntField {
      def parse(s: String): Either[NumberFormatException, Int] =
        Either.attempt(s.toInt)
          .refineToOrDie[NumberFormatException]
    }

    object Terminal {
      def printLine(s: Any): IO[Nothing, Unit] = Console.printLine(s).orDie
      def readLine: IO[Nothing, String] = Console.readLine.orDie

      def readInt: IO[String, Int] = {
        for {
          s <- readLine
          n <- IO.fromEither(IntField.parse(s)).mapError(_ => s"Invalid integer ($s)")
        } yield n
      }
    }

    import scala.io.AnsiColor.*
    val program: IO[String, Unit] = for {
      _ <- Terminal.printLine(s"What's your ${BOLD}name${RESET}?")
      name <- Terminal.readLine
      _ <- Terminal.printLine(s"Hello $name!")
      _ <- Terminal.printLine("How old are you?")
      age <- Terminal.readInt
      _ <- Terminal.printLine(s"You do not look like you're $age years old, $name!")
    } yield ()

    val exit = Runtime.unsafeRun(program)
    println(exit)
   }
}
