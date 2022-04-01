package io {
  package immutability {
    extension[K, V] (self: Map[K, V]) {
      def updatedWith[V2 >: V](key: K, f: V => V2): Map[K, V2] =
        self.updatedWith(key) {
          case Some(v) => Some(f(v))
          case None => None
        }
    }

    enum Operation {
      case Credit(account: Int, amount: Double)
      case Debit(account: Int, amount: Double)
      case Transfer(sourceAccount: Int, destinationAccount: Int, amount: Double)
    }

    import Operation.*

    case class Bank(accounts: Map[Int, Double], totalAmount: Double) {
      def process(operation: Operation): Bank = {
        operation match {
          case Credit(account, amount) =>
            Bank(
              accounts = this.accounts.updatedWith(account, _ + amount),
              totalAmount = this.totalAmount + amount
            )

          case Debit(account, amount) =>
            Bank(
              accounts = accounts.updatedWith(account, _ - amount),
              totalAmount = this.totalAmount - amount
            )

          case Transfer(sourceAccount, destinationAccount, amount) =>
            this.copy(
              accounts = this.accounts
                .updatedWith(sourceAccount, _ - amount)
                .updatedWith(destinationAccount, _ + amount)
            )
        }
      }
    }

    object Bank {
      def apply(accounts: Map[Int, Double]): Bank =
        Bank(
          accounts = accounts,
          totalAmount =  accounts.values.sum
        )
    }

    object BankApp {
      def main(args: Array[String]): Unit = {
        val bank = Bank(
          Map(
            1 -> 100.0,
            2 -> 200.0
          )
        )

        val finalBank = bank
          .process(Credit(account = 1, amount = 30.0))
          .process(Debit(account = 2, amount = 10.0))
          .process(Transfer(sourceAccount = 1, destinationAccount = 2, amount = 10.0))

        println(s"finalBank = $finalBank")
      }
    }
  }

  package impure {
    import io.impure.Console

    package working {
      object ConsoleApp {
        def main(args: Array[String]): Unit = {
          Console.printLine("What's player 1 name?")
          val player1 = Console.readLine()
          Console.printLine("What's player 2 name?")
          val player2 = Console.readLine();
          Console.printLine(s"Players are $player1 and $player2.")
        }
      }
    }

    package broken_extract_variable {
      object ConsoleApp {
        def main(args: Array[String]): Unit = {
          val s = Console.readLine()
          Console.printLine("What's player 1 name?")
          val player1 = s
          Console.printLine("What's player 2 name?")
          val player2 = s;
          Console.printLine(s"Players are $player1 and $player2.")
        }
      }
    }

    package broken_inline_variable {
      object ConsoleApp {
        def main(args: Array[String]): Unit = {
          Console.printLine("What's player 1 name?")
          Console.printLine("What's player 2 name?")
          val player2 = Console.readLine();
          Console.printLine(s"Players are ${Console.readLine()} and $player2.")
        }
      }
    }
  }

  package program {
    import io.pure.{Console, IO, Runtime}

    package instantiate {
      object ConsoleApp {
        val helloApp: IO[Unit] =
          Console.printLine("What's your name?").flatMap { _ =>
            Console.readLine.flatMap { name =>
              Console.printLine(s"Hello $name!")
            }
          }

        def main(args: Array[String]): Unit = {
          val program = helloApp
          println(program)
        }
      }
    }

    package interpret {
      object ConsoleApp {
        val helloApp: IO[Unit] =
          Console.printLine("What's your name?").flatMap { _ =>
            Console.readLine.flatMap { name =>
              Console.printLine(s"Hello $name!")
            }
          }

        def main(args: Array[String]): Unit = {
          val program = helloApp // PURE
          Runtime.unsafeRun(helloApp) // IMPURE!!! But that's OK!
        }
      }
    }
  }

  case class Point(x: Int, y: Int)

  package for_comprehension {
    import io.purefree.{IO, Runtime, Console, Random}

    package too_many_maps_and_flatmaps {
      object Main {

        import java.io.IOException

        val welcomeNewPlayer: IO[Unit] =
          Console.printLine("What's your name?").flatMap { _ =>
            Console.readLine.flatMap { name =>
              Random.nextIntBetween(0, 20).flatMap { x =>
                Random.nextIntBetween(0, 20).flatMap { y =>
                  Random.nextIntBetween(0, 20).flatMap { z =>
                    Console.printLine(s"Welcome $name, you start at coordinates($x, $y, $z).")
                  }
                }
              }
            }
          }

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(welcomeNewPlayer)
        }
      }
    }

    package for_comprehension {
      object Main {
        val welcomeNewPlayer: IO[Unit] =
          for {
            _ <- Console.printLine("What's your name?")
            name <- Console.readLine
            x <- Random.nextIntBetween(0, 20)
            y <- Random.nextIntBetween(0, 20)
            z <- Random.nextIntBetween(0, 20)
            _ <- Console.printLine(s"Welcome $name, you start at coordinates ($x, $y, $z).")
          } yield ()

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(welcomeNewPlayer)
        }
      }
    }

    package intermediary_variable {
      object Main {
        val printRandomPoint: IO[Unit] =
          for {
            x <- Random.nextIntBetween(0, 20)
            y <- Random.nextIntBetween(0, 20)
            point = Point(x, y) // Not running an IO, '=' instead of '<-'
            _ <- Console.printLine(s"point=$point")
          } yield ()

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(printRandomPoint)
        }
      }
    }

    package types {
      object Main {
        val printRandomPoint: IO[Point] = {
          for {
            x     /* Int   */ <- Random.nextIntBetween(0, 10)             /* IO[Int]  */
            _     /* Unit  */ <- Console.printLine(s"x=$x")               /* IO[Unit] */
            y     /* Int   */ <- Random.nextIntBetween(0, 10)             /* IO[Int]  */
            _     /* Unit  */ <- Console.printLine(s"y=$y")               /* IO[Unit] */
            point /* Point */ = Point(x, y)                               /* Point    */
            _     /* Unit  */ <- Console.printLine(s"point.x=${point.x}") /* IO[Unit] */
            _     /* Unit  */ <- Console.printLine(s"point.y=${point.y}") /* IO[Unit] */
          } yield point /* Point */
        } /* IO[Point] */

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(printRandomPoint)
        }
      }
    }

    package scopes {
      object Main {
        val printRandomPoint: IO[Point] = {
          for {
            x <- Random.nextIntBetween(0, 10)             /*  x                */
            _ <- Console.printLine(s"x=$x")               /*  O                */
            y <- Random.nextIntBetween(0, 10)             /*  |    y           */
            _ <- Console.printLine(s"y=$y")               /*  |    O           */
            point = Point(x, y)                           /*  O    O    point  */
            _ <- Console.printLine(s"point.x=${point.x}") /*  |    |    O      */
            _ <- Console.printLine(s"point.y=${point.y}") /*  |    |    O      */
          } yield point                                   /*  |    |    O      */
        }

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(printRandomPoint)
        }
      }
    }

    package implicit_nesting {
      object Main {
        val printRandomPoint: IO[Point] = {
          for {
              x <- Random.nextIntBetween(0, 10)
            /* | */ _ <- Console.printLine(s"x=$x")
            /* |    | */ y <- Random.nextIntBetween(0, 10)
            /* |    |    | */ _ <- Console.printLine(s"y=$y")
            /* |    |    |    | */ point = Point(x, y)
            /* |    |    |    |    | */ _ <- Console.printLine(s"point.x=${point.x}")
            /* |    |    |    |    |    | */ _ <- Console.printLine(s"point.y=${point.y}")
          } /* |    |    |    |    |    |    | */ yield point
        }

        def main(args: Array[String]): Unit = {
          Runtime.unsafeRun(printRandomPoint)
        }
      }
    }
  }

  package full_app {
    import io.purefree.{Console, IO, Random, Runtime}

    object ConsoleApp {
      def mainApp: IO[Unit] =
        for {
          _ <- displayMenu
          choice <- readChoice
          exit <- launchMenuItem(choice)
          _ <- if exit then IO.succeed(()) else /* RECURSE */ mainApp
        } yield ()

      val displayMenu: IO[Unit] =
        for {
          _ <- Console.printLine("1) Hello")
          _ <- Console.printLine("2) Countdown")
          _ <- Console.printLine("3) Exit")
        } yield ()

      val readChoice: IO[Int] = readIntBetween(1, 3)

      def launchMenuItem(choice: Int): IO[Boolean] =
        choice match {
          case 1 => helloApp.map(_ => false)
          case 2 => countDownApp.map(_ => false)
          case 3 => IO.succeed(true)
        }

      val helloApp: IO[Unit] =
        for {
          _ <- Console.printLine("What's your name?")
          name <- Console.readLine
          _ <- Console.printLine(s"Hello $name!")
        } yield ()

      val countDownApp: IO[Unit] =
        for {
          n <- readIntBetween(10, 100000)
          _ <- countdown(n)
        } yield ()

      def countdown(n: Int): IO[Unit] =
        if n == 0 then
          Console.printLine("BOOM!!!")
        else
          for {
            _ <- Console.printLine(n)
            _ <- /* RECURSE */ countdown(n - 1)
          } yield ()

      def readIntBetween(min: Int, max: Int): IO[Int] =
        for {
          _ <- Console.printLine(s"Enter a number between $min and $max")
          i <- readInt
          n <- if min <= i && i <= max then IO.succeed(i) else /* RECURSE */ readIntBetween(min, max)
        } yield n

      def readInt: IO[Int] = Console.readLine.map(_.toInt)

      def main(args: Array[String]): Unit = {
        Runtime.unsafeRun(mainApp)
      }
    }
  }
}
