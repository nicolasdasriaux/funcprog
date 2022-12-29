autoscale: true
footer: Practical Functional Programming - Values
slidenumbers: true

# Practical Functional Programming
# [fit] **It's about Values**
## in Scala

---

# What Is Functional Programming?

---

# **Functions**

* Functional Programming is programming with **functions**
* A _function_ must be
  - **Deterministic**: same arguments implies same result
  - **Total**: result always available for arguments, no exception :wink:
  - **Pure**: no side-effects, only effect is computing result
* Functional programming **in the small**

---

# **Values**

* A **function** manipulates values
  - Consumes values as **arguments**
  - Produces a value as a **result**
* **Values** are **immutable** and **pure** instances of
  - Primitive types
  - Immutable classes
  - **Function** types
* Values are **compared by value** :wink:

---

# **Expressions**

* **Expressions** Combine multiple _values_ to compute another value
* _Functions_ can participate in expressions (as they are values)

---

# It’s All about **Values** and **Expressions**

* Functional Programming is programming with **values** and **expressions**
* _Functions_ are just a special kind of value
* _Applying a function_ is just a special case of expression
* Functional programming **in the large**

---

# Values in a **Pure** World

---

# Immutable Class

```scala
case class Customer(id: Int, firstName: String, lastName: String)

// Create an new instance
val customer = Customer(id = 1, firstName = "John", lastName = "Doe")
val name = customer.firstName

// Create a modified copy of an instance
val modifiedCustomer = customer.copy(lastName = "Martin")
// `customer` remains unmodified

// Compare instances by value
val sameCustomer = Customer(id = 1, firstName = "John", lastName = "Doe")
assert(customer == sameCustomer)
```

---

# Immutable Collection

```scala
// Create a new instance
val greetings: Set[String] = Set("hello", "goodbye")

// Creating an instance by applying a method on an instance  
val availableGreetings =
  greetings ++ Set("hi", "bye", "hello")
// `greetings` remains unmodified
```

---

# Expressions

```scala
val status = if enabled then "On" else "Off" // `if` expression

val mark = color match { // `match` expression
  case Red => 2
  case Orange => 4
  case Green => 6
}

val altitude = { // { ... } expression
  val y = slope * t

  if y < -threshold then -threshold
  else if y > threshold then threshold
  else y
}
```

---

# Simple Immutable `enum`

```scala
enum Direction {
  case North, South, West, East
}

case class Position(x: Int, y: Int) {
  def move(direction: Direction): Position =
    direction match {
      case North => this.copy(y = this.y - 1)
      case South => this.copy(y = this.y + 1)
      case West => this.copy(x = this.x - 1)
      case East => this.copy(x = this.x + 1)
    }
}
```

---

# Immutable `enum` on Steroids

```scala
enum Action { // ADT (Algebraic Data Type)
  case Sleep
  case Walk(direction: Direction)
  case Jump(position: Position)
}

case class Player(position: Position) {
  def act(action: Action): Player =
    action match { // Pattern Matching
      case Sleep => this
      case Walk(direction) => Player(position.move(direction))
      case Jump(position) => Player(position)
    }
}
```

---

# Modeling with Value Types

* Model data as **value types** with **ADT**s (Algebraic Data Types)
  - Combination of fields with `case class`
  - Alternatives with `enum`
* Add **combinator** methods
* Use **pattern matching** with `match` expression to handle `case`s
* Mostly do not use _inheritance_ and _polymorphism_

---

# Modeling Bank `Operation`

```scala
enum Operation {
  case Credit(account: Int, amount: Double)
  case Debit(account: Int, amount: Double)
  case Transfer(sourceAccount: Int, destinationAccount: Int, amount: Double)
}
```

---

# Modeling `Bank`

```scala
case class Bank(name: String, accounts: Map[Int, Double]) {
  // ...
}
```

---

# Combine `Bank` and `Operation`

```scala
case class Bank(name: String, accounts: Map[Int, Double]) {
  def process(operation: Operation): Bank =
    operation match {
      case Credit(account, amount) =>
        val updatedAccounts = this.accounts.updatedWith(account, _ + amount)
        // `_ + amount` is equivalent to `a => a + amount`
        this.copy(accounts = updatedAccounts)

      case Debit(account, amount) => ???
      case Transfer(sourceAccount, destinationAccount, amount) => ???
    }
}

```

---

# Fallible Result Reified

```scala
enum Either[+E, +A] { // ...
  case Left[E](error: E) extends Either[E, Nothing]
  case Right[A](result: A) extends Either[Nothing, A]
  // ...
}

object Either {
  def succeed[A](result: A): Either[Nothing, A] = Right(result)
  def fail[E](error: E): Either[E, Nothing] = Left(error)
  // ...
}
```

---

# Fallible Result Reified (continued)

```scala
enum Either[+E, +A] { va => // `va` becomes an alias for `this`
  def flatMap[E2 >: E, B](cont: A => Either[E2, B]): Either[E2, B] =
    va match {
      case Right(a) => cont(a)
      case Left(e) => Left(e)
    }

  def map[B](trans: A => B): Either[E, B] =
    va match {
      case Right(a) => Right(trans(a))
      case Left(e) => Left(e)
    }
}
```

---

# Savings Account

```scala
case class SavingsAccount(balance: Int) {
  def debit(amount: Int): Either[String, SavingsAccount] =
    if this.balance - amount >= 0 then
      Either.succeed(SavingsAccount(balance = this.balance - amount))
    else
      Either.fail("Cannot be over-debited")
    
  def credit(amount: Int): Either[String, SavingsAccount] =
    if this.balance + amount <= 500 then
      Either.succeed(SavingsAccount(balance = this.balance + amount))
    else
      Either.fail("Cannot be over-credited")
}
```

---

# Transferring Money

```scala
object SavingsAccount {
  def transfer(
                source: SavingsAccount,
                destination: SavingsAccount,
                amount: Int
              ): Either[String, (SavingsAccount, SavingsAccount)] =

    source.debit(amount) /* */ .flatMap { updatedSource =>
      destination.credit(amount) /* */ .map { updatedDestination =>
        (updatedSource, updatedDestination)
      }
    }
}

```

---

# Flatten Those `map`s and `flatMap`s!

```scala
object SavingsAccount {
  def transfer(
                source: SavingsAccount,
                destination: SavingsAccount,
                amount: Int
              ): Either[String, (SavingsAccount, SavingsAccount)] =

    for {
      updatedSource <- source.debit(amount)
      updatedDestination <- destination.credit(amount)
    } yield (updatedSource, updatedDestination)
}
```

---

#  Successful Transfer

```scala
val success = SavingsAccount.transfer(
  source = SavingsAccount(200),
  destination = SavingsAccount(300),
  amount = 50
)

assert(success == Right((SavingsAccount(150), SavingsAccount(350))))
```

---

# Failed Transfers

```scala

val overDebited = SavingsAccount.transfer(
  source = SavingsAccount(40),
  destination = SavingsAccount(300),
  amount = 50
)

assert(overDebited == Left("Cannot be over-debited"))

val overCredited = SavingsAccount.transfer(
  source = SavingsAccount(200),
  destination = SavingsAccount(400),
  amount = 150
)

assert(overCredited == Left("Cannot be over-credited"))
```

---

# Values in an **Impure** World
### Reifying Side-effects

---

# Infallible Program

---

# Infallible Program Reified

```scala
trait UIO[+A] {
  def flatMap[B](cont: A => UIO[B]): UIO[B] = Op.FlatMap(this, cont)
  def map[B](trans: A => B): UIO[B] = ???
}

object UIO {
  def succeed[A](a: => A): UIO[A] = Op.Succeed(() => a)

  enum Op[+A] extends UIO[A] {
    case Succeed(result: () => A) extends Op[A]
    case FlatMap[A0, A](io: UIO[A0], cont: A0 => UIO[A]) extends Op[A]
  }
}
```

---

# Interpreting Infallible Program

```scala
object Runtime {
  def unsafeRun[A](io: UIO[A]): A = {
    io match {
      case Op.Succeed(result) => ???
      case Op.FlatMap(ioA0, cont) => ???
    }
  }
}
```

---

# Elementary `Console` Programs

```scala
object Console {
  def printLine(o: Any): UIO[Unit] = UIO.succeed(println(o))
  val readLine: UIO[String] = UIO.succeed(StdIn.readLine())
}
```

---

# Fallible Program

---

# Success, Failure and Death

* **Succeed** with a **result**

* **Fail** with an **error**
  - _Expected_, recoverable
  - **Domain** error, **business** error, but not only
  - Materialized as a value

* **Die** with a **defect**
  - _Unexpected_, not recoverable
  - Materialized as an **Exception**

---

# Fallible Program Exit Reified

```scala
enum Exit[+E, +A] {
  case Succeed[A](result: A) extends Exit[Nothing, A]
  case FailCause[E](cause: Cause[E]) extends Exit[E, Nothing]
}

enum Cause[+E] {
  case Fail[E](error: E) extends Cause[E]
  case Die(defect: Throwable) extends Cause[Nothing]
}
```

---

# Fallible Program Reified

```scala
trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = ???
  def map[B](trans: A => B): IO[E, B] = ???
}

object IO {
  def succeed[A](result: => A): IO[Nothing, A] = ???
  def fail[E](error: => E): IO[E, Nothing] = ???
  def die(defect: Throwable): IO[Nothing, Nothing] = ???
  
  def attempt[A](result: => A): IO[Throwable, A] = ???
}
```

---

# Fallible Program Reified (error handling)

```scala
trait IO[+E, +A] { // ...
  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] = ???

  def mapError[E2](trans: E => E2): IO[E2, A] = ???
  def orDie(using toThrowable: E <:< Throwable): IO[Nothing, A] = ???
}

extension[E <: Throwable, A] (io: IO[E, A]) {
  def refineToOrDie[E2 <: E /* ... */]: IO[E2, A] = ???
}
```

---

# Interpreting Fallible Program

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = ???
}
```

---

# Elementary `Console` Programs (fallible)

```scala
object Console {
  def printLine(o: Any): IO[IOException, Unit] =
    IO.attempt(println(o))
      .refineToOrDie[IOException]

  val readLine: IO[IOException, String] =
    IO.attempt(StdIn.readLine())
      .refineToOrDie[IOException]
}
```

---

# Reading an Integer from Console

```scala
object ConsoleReader {
  def readInt: IO[IOException | String, Int] =
    for {
      s <- Console.readLine
      i <-
        IO.attempt(s.toInt)
          .refineToOrDie[NumberFormatException]
          .mapError(_ => s"Invalid integer string ($s)")
    } yield i
}
```

---

# Welcoming User

```scala
object WelcomeApp {
  case class User(name: String, age: Int)

  val program: IO[IOException | String, User] =
    for {
      name <- Console.printLine("What's your name?").flatMap(_ => Console.readLine)
      age <- Console.printLine("What's your age?").flatMap(_ => ConsoleReader.readInt)
      _ <- Console.printLine(s"You don't look like you're $age, $name.")
    } yield User(name, age)

  def main(args: Array[String]): Unit = {
    // PURE-only above ^^^^^ (programs are values)
    val exit: Exit[IOException | String, User] = Runtime.unsafeRun(program) // Only this line is IMPURE!!!
    println(s"exit=$exit")
  }
}
```

---

# **Into the Large**
### with the _ZIO_ ecosystem

---

# Assertion Reified

```scala
final case class Assertion[-A](/* ... */) {
  // Combinators
  def &&[A1 <: A](that: Assertion[A1]): Assertion[A1] = ???
  def ||[A1 <: A](that: Assertion[A1]): Assertion[A1] = ???
  def unary_! : Assertion[A] = ???
  // Interpreter
  def test(value: A) /* ...*/: Boolean = ???
}

object Assertion {
  // Factories
  def isGreaterThanEqualTo[A](reference: A)(using ord: Ordering[A]): Assertion[A] = ???
  def isLessThanEqualTo[A](reference: A)(using ord: Ordering[A]): Assertion[A] = ???
  def isRight[A](assertion: Assertion[A]): Assertion[Either[Any, A]] = ???
}
```

---

# Combining Assertions

```scala
val assertion1: Assertion[Int] =
  Assertion.isGreaterThanEqualTo(1) && Assertion.isLessThanEqualTo(5)

// def isRight[A](assertion: Assertion[A]): Assertion[Either[Any, A]]
val assertion2: Assertion[Int] =
  Assertion.isRight(Assertion.isGreaterThanEqualTo(1))

def between[A](min: A, max: A)(using ord: Ordering[A]): Assertion[A] =
  Assertion.isGreaterThanEqualTo(min) && Assertion.isLessThanEqualTo(max)
```

---

# Testing Assertion

```scala
val assertion: Assertion[Int] = between(min = 1, max = 10)
val result: Boolean = assertion.test(5)
```

---

# Generator Reified

```scala
final case class Gen[-R, +A](/* ... */) {
  // Combinators
  def map[B](f: A => B): Gen[R, B] = ???
  def zip[R1 <: R, B](that: Gen[R1, B]): Gen[R1, (A, B)] = ???
  // Interpreters
  def runCollectN(n: Int): ZIO[R, Nothing, List[A]] = ???
}

object Gen {
  // Factories
  def int(min: Int, max: Int): Gen[Any, Int] = ???
  def elements[A](as: A*): Gen[Any, A] = ???
  def localDate(min: LocalDate, max: LocalDate): Gen[Any, LocalDate] = ???
}
```

---

# Combining Generators

```scala
val idGen: Gen[Any, Int] = Gen.int(min = 1, max = 5000)
val firstNameGen: Gen[Any, String] = Gen.elements("Peter", "Paul", "Mary")
val lastNameGen: Gen[Any, String] = Gen.elements("Brown", "Jones", "Miller", "Davis")

val birthDateGen: Gen[Any, LocalDate] = Gen.localDate(
  min = LocalDate.of(1950, 1, 1).nn,
  max = LocalDate.of(1995, 12, 31).nn
)

case class Person(id: Int, firstName: String, lastName: String, birthDate: LocalDate)

val personGen: Gen[Any, Person] =
  idGen.zip(firstNameGen).zip(lastNameGen).zip(birthDateGen)
    .map(Person(_, _, _, _))
```

---

# Running Generator

```scala
val generatePeople: ZIO[Any, Nothing, List[Person]] = personGen.runCollectN(10)

val peopleExit: Exit[Nothing, List[Person]] = Unsafe.unsafe { unsafe ?=>
  Runtime.default.unsafe.run(generatePeople)
}
```
