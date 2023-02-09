package zio.app

import zio.direct._
import zio.{ZIO, ZIOAppDefault, Console}

object ZioDirectApp extends ZIOAppDefault {
  override def run = defer {
    Console.printLine("What's you name?").run
    val name = Console.readLine.run
    Console.printLine(s"Hello $name!").run

    val answer = Console.readLine.run

    val choice = unsafe(answer.toInt)
    Console.printLine(choice).run

    for i <- 1 to 10 do {
      val answer = Console.readLine.run

      if Set("Y", "y", "O", "o").contains(answer) then {
        Console.printLine("Yes!").run
      }
    }
  }
}
