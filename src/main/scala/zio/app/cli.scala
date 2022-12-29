
package zio.app

import zio.cli.Args

import zio.cli.Command

import java.time.LocalDate


import zio.*
import zio.cli.{CliApp, Command, HelpDoc, Options, Args}

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum GreetSubCommand {
  case HelloSubCommand(name: String, maybeBirthdate: Option[LocalDate])
  case GoodbyeSubCommand(name: String)
}

import GreetSubCommand.*

val nameOption: Options[String] = Options.text("name") ?? "Name of the person to greet"
val birthdateOption: Options[Option[LocalDate]] = Options.localDate("birthdate").optional("")
val luckyNumbersArgs: Args[List[BigInt]] = Args.integer.repeat

val helloCommand: Command[HelloSubCommand] = Command(
  name = "hello",
  options = nameOption ++ birthdateOption,
  args = luckyNumbersArgs
).map {
  case ((name, maybeBirthdate), luckyNumbers) => HelloSubCommand(name, maybeBirthdate)
}

val goodbyeCommand: Command[GoodbyeSubCommand] = Command(
  name = "goodbye",
  options = nameOption
).map(name => GoodbyeSubCommand(name = name))

val greetCommand: Command[GreetSubCommand] = Command(name = "greet")
  .subcommands(helloCommand, goodbyeCommand)

val helloCliApp = CliApp.make(
  name = "greet-app",
  version = "0.1",
  summary = HelpDoc.Span.text("Greet App"),
  command = greetCommand
) {
  case HelloSubCommand(name, maybeBirthdate) =>
    for {
      _ <- Console.printLine(s"Hello $name!")
      _ <- ZIO.foreach(maybeBirthdate) { birthdate =>
        for {
          now <- Clock.localDateTime.map(_.toLocalDate)
          age = ChronoUnit.YEARS.between(birthdate, now)
          _ <- Console.printLine(s"You were born on $birthdate, so you're $age years old.")
        } yield ()
      }
    } yield ()

  case GoodbyeSubCommand(name) => Console.printLine(s"Goodbye $name!")
}
