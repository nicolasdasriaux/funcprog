autoscale: true
footer: Practical Pure I/O in Scala
slidenumbers: true

# Practical

# [fit] **Pure I/O**

## in Scala

---

# Previously on Practical Immutability...

* Immutable **Classes**
* Immutable **Collections** and **Options**
* Immutable **Variables**
* Expressions
* `enum` on steroids aka Algebraic Data Types (**ADT**)
* Pattern Matching

---

# What Functional Programming Is About

* Functional Programming (FP) is programming with **functions**.
  - **Deterministic**: same arguments implies same result
  - **Total**: result always available for arguments, no exception
  - **Pure**: no side-effects, only effect is computing result

* A benefit of FP is **referential transparency**.

---

# What Referential Transparency Brings

* **Typical refactorings cannot break a working program** :thumbsup:.
* Applies to the following refactorings:
  - :wrench: **Extract Variable**
  - :wrench: **Inline Variable**
  - :wrench: **Extract Method**
  - :wrench: **Inline Method**

---

# [fit] Refactorings Break Impure :imp: Programs

---

# Console Operations

```scala
object Console {
  def printLine(o: Any): Unit = println(o)
  def readLine(): String = StdIn.readLine()
}
```

---

# A Working Program

```scala
object ConsoleApp {
  def main(args: Array[String]): Unit = {
    Console.printLine("What's player 1 name?")
    val player1 = Console.readLine()
    Console.printLine("What's player 2 name?")
    val player2 = Console.readLine();
    Console.printLine(s"Players are $player1 and $player2.")
  }
}
```

```
What's player 1 name?
> Paul
What's player 2 name?
> Mary
Players are Paul and Mary.
```

---

# Broken Extract Variable Refactoring

```scala
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
```

```
> Paul
What's player 1 name?
What's player 2 name?
Players are Paul and Paul.
```

---

# Broken Inline Variable Refactoring

```scala
object ConsoleApp {
  def main(args: Array[String]): Unit = {
    Console.printLine("What's player 1 name?")
    Console.printLine("What's player 2 name?")
    val player2 = Console.readLine();
    Console.printLine(s"Players are ${Console.readLine()} and $player2.")
  }
}
```

```
What's player 1 name?
What's player 2 name?
> Paul
> Mary
Players are Mary and Paul.
```

---

# Building a Pure Program<br>from the Ground Up

---

# Describing a Program

```scala
class IO[+A]
```

* Describes a **program** performing I/Os
* When run, will eventually yield a **result** of type `A`
* Simplified, don't handle errors or exceptions

---

# Program as Immutable Object

```scala
case class IO[+A](unsafeIO: () => A) {
  // ...
}

object IO { // ...
  def attempt[A](a: => A): IO[A] = IO(() => a)
}
```

* Simple immutable class
* Holds a parameterless **side-effecting** function

---

# Elementary `Console` Programs

```scala
object Console {
  def printLine(o: Any): IO[Unit] =
    IO.attempt(/* () => */ println(o))
    
  val readLine: IO[String] =
    IO.attempt(/* () => */ StdIn.readLine())
}
```

---

# Program Yielding a Value

```scala
object IO {
  def succeed[A](a: A): IO[A] = IO(() => a) // ...
}
```

---

# Chaining Programs

```scala
case class IO[+A](unsafeIO: () => A) { ioA =>
  def flatMap[B](cont: A => IO[B]): IO[B] = {
    val ioB: IO[B] = IO { () =>
      val a: A = ioA.unsafeIO()
      val ioB: IO[B] = cont(a)
      val b: B = ioB.unsafeIO()
      b
    }

    ioB
  } // ...
}
```

---

# Transforming Result of Program

```scala
case class IO[+A](unsafeIO: () => A) { ioA => // ...
  def map[B](trans: A => B): IO[B] = {
    val ioB: IO[B] = IO { () =>
      val a: A = ioA.unsafeIO()
      val b: B = trans(a)
      b
    }

    ioB
  }
}
```

---

# A Value Containing Void (`Unit`)

* `Unit` is a class with only 1 instance written as `()`
* A value that bears no information
* Somehow an empty tuple

---

# Instantiating a Program

```scala
object ConsoleApp {
  val helloApp: IO[Unit] =
    Console.printLine("What's your name?").flatMap { _ =>
      Console.readLine.flatMap { name =>
        Console.printLine(s"Hello $name!")
      }
    }

  def main(args: Array[String]): Unit = {
    val program = helloApp
  }
}
```

---

# But Program Does Not Run :astonished:

```scala
object ConsoleApp {
  // ...
  def main(args: Array[String]): Unit = {
    val program = helloApp
    println(program)
  }
}
```

* Will print something
  like `IO(io.pure.IO$$Lambda$19/0x0000000800098c40@42eca56e)`
* This is just an **immutable object**, it does no side-effect, it's **pure** :innocent:.
* Need an **interpreter** to run!

---

# Interpreting a Program

```scala
object Runtime {
  def unsafeRun[A](io: IO[A]): A = io.unsafeIO()
}
```

---

# Running a Program

```scala
object ConsoleApp {
  // PURE ...
  
  def main(args: Array[String]): Unit = {
    val program = helloApp // PURE
    Runtime.unsafeRun(helloApp) // IMPURE!!! But that's OK!
  }
}
```

* Sure, `unsafeRun` call point (**_edge of the world_**) is **impure** :imp:...
* But the **rest of the code** is fully **pure** :innocent:!

---

# `for` Comprehension

---

# Elementary `Random` Programs

```scala
object Random {
  def nextIntBetween(min: Int, max: Int): IO[Int] =
    IO.attempt(scala.util.Random.nextInt(max - min + 1) + min)
}
```

---

# Pyramid of `map`s and `flatMap`s :smiling_imp:

```scala
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
```

---

# Flatten Them All :innocent:

```scala
val welcomeNewPlayer: IO[Unit] =
  for {
    _ <- Console.printLine("What's your name?")
    name <- Console.readLine
    x <- Random.nextIntBetween(0, 20)
    y <- Random.nextIntBetween(0, 20)
    z <- Random.nextIntBetween(0, 20)
    _ <- Console.printLine(s"Welcome $name, you start at coordinates ($x, $y, $z).")
  } yield ()
```

---

# Intermediary Variable

```scala
val printRandomPoint: IO[Unit] =
  for {
    x <- Random.nextIntBetween(0, 20)
    y <- Random.nextIntBetween(0, 20)
    point = Point(x, y) // Not running an IO, '=' instead of '<-'
    _ <- Console.printLine(s"point=$point")
  } yield ()
```

---

# Anatomy of `for` Comprehension

---

# [fit] **`for` comprehension is not a `for` loop**.
## It can be a `for` loop...
# [fit] But it can handle **many other things**
## like `IO` and ... `Seq`, `Option`, `Future`...

---

# `for` Comprehension **Types**

```scala
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
```

---

# `for` Comprehension **Type Rules**

|             | `val` type | operator | expression type |
|-------------|------------|----------|-----------------|
| generation  | `A`        | `<-`     | `IO[E, A]`      |
| assignment  | `B`        | `=`      | `B`             |

|            | `for` comprehension type | `yield` expression type |
|------------|--------------------------|-------------------------|
| production | `IO[E, R]`               | `R`                     |

* Combines **only** `IO[E, T]`, **no mix** with `Seq[T]`, `Option[T]`, `Future[T]`...
* But it could be **only** `Seq[T]`, **only** `Option[T]`, **only** `Future[T]`...

---

# `for` Comprehension **Scopes**

```scala
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
```

---

# `for` Comprehension **Implicit Nesting**

```scala
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
```

---

# Writing a Small Application

---

# Saying Hello

```scala
val helloApp: IO[Unit] =
  for {
    _ <- Console.printLine("What's your name?")
    name <- Console.readLine
    _ <- Console.printLine(s"Hello $name!")
  } yield ()
```

---

# Counting Down

```scala
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
```

---

# Displaying Menu and Getting Choice

```scala
val displayMenu: IO[Unit] =
  for {
    _ <- Console.printLine("1) Hello")
    _ <- Console.printLine("2) Countdown")
    _ <- Console.printLine("3) Exit")
  } yield ()

val readChoice: IO[Int] = readIntBetween(1, 3)
```

---

# Launching Menu Item

```scala
def launchMenuItem(choice: Int): IO[Boolean] =
  choice match {
    case 1 => helloApp.map(_ => false)
    case 2 => countDownApp.map(_ => false)
    case 3 => IO.succeed(true)
  }
```

---

# Looping over Menu

```scala
def mainApp: IO[Unit] =
  for {
    _ <- displayMenu
    choice <- readChoice
    exit <- launchMenuItem(choice)
    _ <- if exit then IO.succeed(()) else /* RECURSE */ mainApp
  } yield ()
```

---

# Reading Integer from Console

```scala
def readIntBetween(min: Int, max: Int): IO[Int] =
  for {
    _ <- Console.printLine(s"Enter a number between $min and $max")
    i <- readInt
    n <- if min <= i && i <= max then IO.succeed(i) else /* RECURSE */ readIntBetween(min, max)
  } yield n

def readInt: IO[Int] = Console.readLine.map(_.toInt)
```

---

# Just a Fancy Toy

* What's **good**
  - Easy to reason about with type safety :thumbsup:
  - Unlimited safe refactorings :thumbsup:
* What's **not so good**
  - Stack unsafe :bomb:
  - Do not handle exceptions, need a better error model :thumbsdown:
  - Not testable :thumbsdown:
  - Difficult to debug :thumbsdown:

---

# Toward a Stack-Freer Implementation

---

# Describing Operations with an ADT

```scala
trait IO[+A] {
  // ...
}

object IO { // ...
  // An ADT (Algebraic Data Type) with a type parameter
  // known as GADT (Generalized Algebraic Data Type)
  enum Op[+A] extends IO[A] {
    case Succeed(result: A) extends Op[A]
    case Attempt(result: () => A) extends Op[A]
    case FlatMap[A0, A](io: IO[A0], cont: A0 => IO[A]) extends Op[A]
  }
}
```

---

# Implementing Same Methods as Before

```scala
trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] =
    Op.FlatMap(this, cont)
    
  def map[B](trans: A => B): IO[B] =
    this.flatMap(a => IO.succeed(trans(a)))
}

object IO {
  def succeed[A](a: A): IO[A] = Op.Succeed(a)
  def attempt[A](a: => A): IO[A] = Op.Attempt(() => a)
}
```

---

# Interpreting with Better Stack Safety

```scala
object Runtime {
  def unsafeRun[A](io: IO[A]): A = {
    io match {
      case Op.Succeed(result: A) => result
      case Op.Attempt(result: (() => A)) => result()

      case Op.FlatMap(ioA0: IO[Any], cont: (Any => IO[A])) =>
        val a0 = /* RECURSE */ unsafeRun(ioA0)
        val ioA = cont(a0)
        val a = /* RECURSE (tail call) */ unsafeRun(ioA)
        a
    }
  }
}
```

---

> Harder, Better, Faster, Stronger
-- Daft Punk

---

# What About Real Life Applications?

* What we could possibly dream of for **real life applications**
  - Support for **asynchronicity**, **concurrency** and  **interruptibility**
  - Consistent **error model** (expected vs. unexpected)
  - **Resiliency** and **resource safety**
  - Full **testability** with dependency injection
  - Easy **debugging**
  - **Performance** and **stack safety**
  - And still fully functional with **100 % safe refactorings**

* _ZIO_, an easy to use Scala library, gives it to us!