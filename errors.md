autoscale: true
footer: Practical Functional Programming - Handling Errors
slidenumbers: true

# Practical Functional Programming

# [fit] **Handling Errors**

## in Scala

---

# Subtyping
## Beyond Inheritance

---

![inline](top-type-bottom-type.png)


---

![inline](subtyping.png)

---

# Result or First Error, `Either`

---

# Either an Error or a Result

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

![inline](either.png)

---

# Chaining After a Result

```scala
enum Either[+E, +A] { va => // `va` becomes a alias for `this`
  def flatMap[E2 >: E, B](cont: A => Either[E2, B]): Either[E2, B] =
    va match {
      case Right(a) => cont(a)
      case Left(e) => Left(e)
    }
  // ...
}
```

---

# Transforming Result

```scala
enum Either[+E, +A] { va => // ...
  def map[B](trans: A => B): Either[E, B] =
    va match {
      case Right(a) => Right(trans(a))
      case Left(e) => Left(e)
    }
  // ...
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

    source.debit(amount).flatMap { updatedSource =>
      destination.credit(amount).map { updatedDestination =>
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

#  Successfull Transfer

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

# Transforming Error

```scala
enum Either[+E, +A] { va => // ...
  def mapError[E2](trans: E => E2): Either[E2, A] =
    va match {
      case Right(a) => Right(a)
      case Left(e) => Left(trans(e))
    }
  // ...
}
```

---

# Parsing an Integer

```scala
object IntField {
  def parse(s: String): Either[String, Int] =
    if s.nonEmpty && s.forall(_.isDigit) then
      Either.succeed(s.toInt)
    else
      Either.fail(s"Invalid integer ($s)")
}
```

---

# Parsing a Point

```scala
case class PointForm(x: String, y: String)
case class Point(x: Int, y: Int)

object PointForm {
  def parse(form: PointForm): Either[String, Point] =
    for {
      x <- IntField.parse(form.x).mapError(e => s"x: $e")
      y <- IntField.parse(form.y).mapError(e => s"y: $e")
    } yield Point(x, y)
}
```

---

# First Error Only

```scala
val success: Either[String, Point] = PointForm.parse(PointForm(x = "1", y = "2"))
assert(success == Right(Point(1,2)))

val xFailure: Either[String, Point] = PointForm.parse(PointForm(x = "XXX", y = "2"))
assert(xFailure == Left("x: Invalid integer (XXX)"))

val yFailure: Either[String, Point] = PointForm.parse(PointForm(x = "1", y = "YYY"))
assert(yFailure == Left("y: Invalid integer (YYY))"))

val xAndYFailure: Either[String, Point] = PointForm.parse(PointForm(x = "XXX", y = "YYY"))
assert(xAndYFailure == Left("x: Invalid integer (XXX)"))
// Just the first error. What about the second error?
```

---

# A Mental Model for Errors

---

# Success, Failure and Death

* **Succeed** with a **result**

* **Fail** with an **error**
  - **Domain** error, **business** error
  - _Expected_, recoverable
  - Materialized by a value (`Either.Left[E]`, `Validation.Failure[E]`)

* **Die** with a **defect**
  - _Unexpected_, not recoverable
  - Signaled by an **Exception**

---

# Turning Exception to Error

```scala
object Either {
  def attempt[A](result: => A): Either[Throwable, A] =
    try succeed(result)
    catch {
      case defect: Throwable => fail(defect)
    }
  // ...
}
```

---

# Refining Errors

```scala
object Either { // ...
  // Provide `refineToOrDie` method to `Either` instances
  // when error type is a subtype of Throwable
  extension [E <: Throwable, A](either: Either[E, A]) {
    def refineToOrDie[E2 <: E /* ... */]: Either[E2, A] = 
      ??? // Do something with `either`
  }
}
```

* Refine error type from `E` to subtype `E2`
* Rethrows any suppressed exception that is not subtype of `E2`

---

# Parsing an Integer (Handling Exception)

```scala
object IntField {
  def parse(s: String): Either[String, Int] =
    Either.attempt(s.toInt)
      .refineToOrDie[NumberFormatException]
      .mapError(_ => s"Invalid integer ($s)")
}
```

---

# Result or Errors, `Validation`

---

# Multiple Errors

```scala
enum Validation[+E, +A] { // ...
  case Failure[E](errors: Seq[E]) extends Validation[E, Nothing]
  case Success[A](result: A) extends Validation[Nothing, A]
  //...
}

object Validation {
  def succeed[A](result: A): Validation[Nothing, A] = Success(result)
  def fail[E](error: E): Validation[E, Nothing] = Failure(List(error))
  // ...
}
```

---

# Merge Validations

```scala
enum Validation[+E, +A] { va => // ...
  def zipPar[E2 >: E, B](vb: Validation[E2, B]): Validation[E2, (A, B)] =
    (va, vb) match {
      case (Success(a), Success(b)) => Success((a, b))
      case (Failure(e1), Success(_)) => Failure(e1)
      case (Success(_), Failure(e2)) => Failure(e2)
      case (Failure(e1), Failure(e2)) => Failure(e1 ++ e2)
    }
  // ...
}
```

---

# Merge Operator

```scala
enum Validation[+E, +A] { va => // ...
  def <&>[E2 >: E, B](vb: Validation[E2, B]): Validation[E2, (A, B)] =
    va.zipPar(vb)
  // ...
}
```

---

# Transforming Result

```scala
enum Validation[+E, +A] { va => // ...
  def map[B](trans: A => B): Validation[E, B] =
    va match {
      case Success(a) => Success(trans(a))
      case Failure(e) => Failure(e)
    }
  // ...
}
```
---

# Parsing a Boolean

```scala
object BooleanField {
  def parse(value: String): Validation[String, Boolean] =
    value.toLowerCase match {
      case "true" | "on" => Validation.succeed(true)
      case "false" | "off" => Validation.succeed(false)
      case _ => Validation.fail(s"Invalid boolean string ($value)")
    }
}
```

---

# Parsing Feature Flags

```scala
case class FeatureFlags(feature1: Boolean, feature2: Boolean)
case class FeatureFlagsForm(feature1: String, feature2: String)

object FeatureFlagsForm {
  def parse(form: FeatureFlagsForm): Validation[String, FeatureFlags] = {
    val feature1: Validation[String, Boolean] = BooleanField.parse(form.feature1)
    val feature2: Validation[String, Boolean] = BooleanField.parse(form.feature2)
    val features: Validation[String, (Boolean, Boolean)] = feature1 <&> feature2
 
    val featureFlags: Validation[String, FeatureFlags] =
      features.map((feature1, feature2) => FeatureFlags(feature1, feature2))
      
    featureFlags
  }
}
```

---

# Transforming Errors

```scala
enum Validation[+E, +A] { va => // ...
  def mapError[E2](trans: E => E2): Validation[E2, A] = 
    va match {
      case Success(a) => Success(a)
      case Failure(e /* : List[E] */) => Failure(e.map(trans))
    }
  // ...
}
```

---

# Either to Validation

```scala
enum Either[+E, +A] { va => // ...
  def toValidation: Validation[E, A] =
    va match {
      case Right(a) => Validation.succeed(a)
      case Left(e) => Validation.fail(e)
    }
  // ...
}
```

---

# Parsing a Point

```scala
case class Point(x: Int, y: Int)
case class PointForm(x: String, y: String)

object PointForm {
  def parse(form: PointForm): Validation[String, Point] =
    (
      IntField.parse(form.x).toValidation.mapError(e => s"x: $e") <&>
      IntField.parse(form.y).toValidation.mapError(e => s"y: $e")
    ).map((x, y) => Point(x, y))
}
```

---

# Keeps All Errors

```scala
val success = PointForm.parse(PointForm(x = "1", y = "2"))
assert(success == Success(Point(1, 2)))

val xFailure = PointForm.parse(PointForm(x = "XXX", y = "2"))
assert(xFailure == Failure(List("x: Invalid integer (XXX)")))

val xAndYFailure = PointForm.parse(PointForm(x = "XXX", y = "YYY"))

assert(xAndYFailure ==
  Failure(
    List(
      "x: Invalid integer (XXX)",
      "y: Invalid integer (YYY)"
    )
  )
)
```

---

# Parsing a Rectangle

```scala
case class Rectangle(p1: Point, p2: Point)
case class RectangleForm(p1: PointForm, p2: PointForm)

object RectangleForm {
  def parse(form: RectangleForm): Validation[String, Rectangle] =
    (
      PointForm.parse(form.p1).mapError(e => s"p1.$e") <&>
      PointForm.parse(form.p2).mapError(e => s"p2.$e")
    ).map((p1, p2) => Rectangle(p1, p2))
}
```

---

# Really Keeps All Errors

```scala
val rectangleForm = RectangleForm(
  p1 = PointForm(x = "P1X", y= "2"),
  p2 = PointForm(x = "3", y= "P2Y")
)

val failure = RectangleForm.parse(rectangleForm)

assert(failure ==
  Failure(
    List(
      "p1.x: Invalid integer (P1X)",
      "p2.y: Invalid integer (P2Y)"
    )
  )
)
```
