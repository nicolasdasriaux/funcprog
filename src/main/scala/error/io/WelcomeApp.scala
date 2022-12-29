package error.io

import java.io.IOException

extension (self: Console.type) {
  def readInt: IO[IOException | String, Int] =
    for {
      s <- Console.readLine
      i <-
        IO.attempt(s.toInt)
          .refineToOrDie[NumberFormatException]
          .mapError(_ => s"Invalid integer string ($s)")
    } yield i
}

object WelcomeApp {
  case class User(name: String, age: Int)

  val program: IO[IOException | String, User] =
    for {
      name <- Console.printLine("What's your name?").flatMap(_ => Console.readLine)
      age <- Console.printLine("What's your age?").flatMap(_ => Console.readInt)
      _ <- Console.printLine(s"You don't look like you're $age, $name.")
    } yield User(name, age)

  def main(args: Array[String]): Unit = {
    val exit: Exit[IOException | String, User] = Runtime.unsafeRun(program)
    println(s"exit=$exit")
  }
}
