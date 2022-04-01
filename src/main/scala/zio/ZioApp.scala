package zio

import zio._


object ZioApp {
  def main(args: Array[String]): Unit = {
    def parseInt(s: String): Either[NumberFormatException, Int] = ???

    val program = for {
      _ <- Console.printLine("What's your name?")
      name <- Console.readLine
      _ <- Console.printLine(s"Hello $name!")
    } yield ()


  }
}
