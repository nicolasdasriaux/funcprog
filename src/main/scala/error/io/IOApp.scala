package error.io

import error.either.Either

import java.io.IOException
import java.sql.SQLException

object IOApp {
  object IntField {
    def parse(s: String): Either[NumberFormatException, Int] =
      Either.attempt(s.toInt)
        .refineToOrDie[NumberFormatException]
  }

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

    val program: IO[IOException, Unit] = for {
      _ <- Console.printLine("What's your name?")
      name <- Console.readLine
      _ <- Console.printLine(s"Hello $name!")
      _ <- Console.printLine("How old are you?")
      age <- Console.readLine.map(_.toInt)
      _ <- Console.printLine(s"You do not look like you're $age years old, $name!")
    } yield ()

    val exit = Runtime.unsafeRun(program)
    println(exit)
   }
}
