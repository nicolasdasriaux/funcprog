autoscale: true
footer: Practical Functional Programming - Handling I/O Errors
slidenumbers: true

# Practical Functional Programming

# [fit] **Handling I/O Errors**

## in Scala

---

# Previously, **Infallible I/O**

---

# Core Operations

```scala
trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] = ???
  // ...
}

object IO {
  def succeed[A](a: => A): IO[A] = ???
  // ...
}
```

---

# Core Interpreter

```scala
object Runtime {
  def unsafeRun[A](io: IO[A]): A = ???
}
```

---

# **Core Operations** as Value

```scala
object IO { // ...
  enum Op[+A] extends IO[A] {
    case Succeed(result: () => A) extends Op[A]

    case FlatMap[A0, A](
                         io: IO[A0],
                         cont: A0 => IO[A]
                       ) extends Op[A]
  }
}

```

---

# **Infallible I/O** as Value


```scala
trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] = Op.FlatMap(this, cont)
  // ...
}

object IO {
  def succeed[A](a: => A): IO[A] = Op.Succeed(() => a)
  // ...
 }
```

---

# Transforming Result (built over `flatMap`)

```scala
trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] = Op.FlatMap(this, cont)

  def map[B](trans: A => B): IO[B] = this.flatMap(a => IO.succeed(trans(a)))
}
```

---

# Core Interpreter Implemented

```scala
object Runtime {
  def unsafeRun[A](io: IO[A]): A = {
    io match {
      case Op.Succeed(result) => result()

      case Op.FlatMap(ioA0, cont) =>
        val a0 = unsafeRun(ioA0)
        val ioA = cont(a0)
        val a = unsafeRun(ioA)
        a
    }
  }
}
```

---

# **Fallible I/O**

---

# **Fallible I/O**
### Exit as Value

---

# Success, Failure or Death

* **Succeed** with a **result**

* **Fail** with an **error**
  - **Expected**, recoverable
  - Domain error, business error, but not only
  - Materialized as a value

* **Die** with a **defect**
    - _Unexpected_, not recoverable
    - Materialized as an **Exception**

---

# **Exit for Fallible I/O** as Value

```scala
enum Exit[+E, +A] {
  case Succeed[A](result: A) extends Exit[Nothing, A]
  case FailCause[E](cause: Cause[E]) extends Exit[E, Nothing]
}

object Exit {
  def succeed[A](result: A): Exit[Nothing, A] = Succeed(result)
  def failCause[E](cause: Cause[E]): Exit[E, Nothing] = FailCause(cause)
  // ...
}
```

---

# **Cause for Failure** as Value

```scala
enum Cause[+E] {
  case Fail[E](error: E) extends Cause[E]
  case Die(defect: Throwable) extends Cause[Nothing]
  // ...
}

object Cause {
  def fail[E](error: E): Cause[E] = Fail(error)
  def die(defect: Throwable): Cause[Nothing] = Die(defect)
}
```

---

# **Exit for Fallible I/O** as Value (revisited)

```scala
enum Exit[+E, +A] {
  case Succeed[A](result: A) extends Exit[Nothing, A]
  case FailCause[E](cause: Cause[E]) extends Exit[E, Nothing]
}

object Exit {
  def succeed[A](result: A): Exit[Nothing, A] = Succeed(result)
  def failCause[E](cause: Cause[E]): Exit[E, Nothing] = FailCause(cause)

  // Usefull shortcuts vvv
  def fail[E](error: E): Exit[E, Nothing] = failCause(Cause.fail(error))
  def die(defect: Throwable): Exit[Nothing, Nothing] = failCause(Cause.die(defect))
}
```

---

# **Fallible I/O**
### Minimal Core

---

# Core Operations

```scala
trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = ??? // ...

  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] = ??? // ...
}

object IO {
  def succeed[A](result: => A): IO[Nothing, A] = ???
  def failCause[E](cause: => Cause[E]): IO[E, Nothing] = ???
  def attempt[A](result: => A): IO[Throwable, A] = ???
  // ...
}
```

---

# Core Interpreter

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = ???
}
```

---

# **Fallible I/O**
### Core Operations As Value

---

# **Core Operations** as Value (elements)

```scala
object IO { // ...
  enum Op[+E, +A] extends IO[E, A] {
    case Succeed[A](result: () => A) extends Op[Nothing, A]
    case FailCause[E](cause: () => Cause[E]) extends Op[E, Nothing]
    case Attempt[E <: Throwable, A](result: () => A) extends Op[E, A] // ...
  } // ...
}
```

---

# **Core Operations** as Value (combinators)

```scala
object IO { // ...
  enum Op[+E, +A] extends IO[E, A] { // ...
    case FlatMap[E, A0, A](
                            io: IO[E, A0],
                            cont: A0 => IO[E, A]
                          ) extends Op[E, A]

    case FoldCauseIO[E0, E, A0, A](
                                    io: IO[E0, A0],
                                    failCauseCase: Cause[E0] => IO[E, A],
                                    succeedCase: A0 => IO[E, A]
                                  ) extends Op[E, A] // ...
  } // ...
}
```

---

# **Fallible I/O** as Value (elements)

```scala
object IO { // ...
  def succeed[A](result: => A): IO[Nothing, A] = Op.Succeed(() => result)
  def failCause[E](cause: => Cause[E]): IO[E, Nothing] = Op.FailCause(() => cause)
  def attempt[A](result: => A): IO[Throwable, A] = Op.Attempt(() => result) // ...
} 
```

---

# **Fallible I/O** as Value (combinators)

```scala
trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = Op.FlatMap(this, cont)
 
  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] =

    Op.FoldCauseIO(this, failCauseCase, succeedCase)
}
```

---

# **Fallible I/O**
### Building Over Minimal Core

---

# Transforming Result (built over `flatMap`)

```scala
trait IO[+E, +A] { // ...
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = Op.FlatMap(this, cont)

  def map[B](trans: A => B): IO[E, B] = this.flatMap(a => IO.succeed(trans(a))) //...
}
```

---

# Handling Error (built over `foldCauseIO`)

```scala
trait IO[+E, +A] { // ...
  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] = Op.FoldCauseIO(this, failCauseCase, succeedCase)
    
  // ...
  def mapError[E2](trans: E => E2): IO[E2, A] = ???

  def catchAllCause[E2, B >: A](failCauseCase: Cause[E] => IO[E2, B]): IO[E2, B] = ???
  def catchAll[E2, B >: A](failCase: E => IO[E2, B]): IO[E2, B] = ???

  def catchSomeCause[E2 >: E, B >: A](failCauseCase: PartialFunction[Cause[E], IO[E2, B]]): IO[E2, B] = ???
  def catchSome[E2 >: E, B >: A](failCase: PartialFunction[E, IO[E2, B]]): IO[E2, B] = ???
}
```

---

# Turning Error to Defect (built over `foldCauseIO`)

````scala
trait IO[+E, +A] { // ...
  def orDie(using toThrowable: E <:< Throwable): IO[Nothing, A] = ???
  def orDieWith(failCase: E => Throwable): IO[Nothing, A] = ???
  def refineOrDie[E2](failCase: PartialFunction[E, E2])
                     (using toThrowable: E <:< Throwable): IO[E2, A] = ???
  def refineOrDieWith[E2](failCase: PartialFunction[E, E2])
                         (toThrowable: E => Throwable): IO[E2, A] = ???
}

object IO { // ...
  extension[E <: Throwable, A] (io: IO[E, A]) {
    def refineToOrDie[E2 <: E : ClassTag]: IO[E2, A] = ???
  } //...
}
````

---

# **Fallible I/O**
### Core Interpreter Implemented

---

# Pattern Matching on Operation

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = { // ...
    io match {
      case Op.Succeed(result) => ???
      case Op.FailCause(cause) => ???
      case Op.Attempt(result) => ???
      
      case Op.FlatMap(ioA0, cont) => ???
      case Op.FoldCauseIO(ioA0, failCauseCase , succeedCase) => ???
    }
  }
}
```

---

# Handling Defects

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = {
    inline def dieOnException[E, A](result: => Exit[E, A]): Exit[E, A] =
      try {
        result
      } catch {
        case ex: Throwable => Exit.die(ex)
      }

    // ...
  }
}
```

---

# Running `Succeed` and `FailCause` Operations

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = { // ...
    io match {
      case Op.Succeed(result) => dieOnException {
        Exit.succeed(result())
      }

      case Op.FailCause(cause) => dieOnException {
        Exit.failCause(cause())
      } // ...
    }
  }
}
```

---

# Running `Attempt` Operation

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = { // ..
    io match { // ...
      case Op.Attempt(result) =>
        try {
          Exit.succeed(result())
        } catch {
          // Fail with the exception as an error if exception thrown
          case defect: Throwable => Exit.fail(defect).asInstanceOf[Exit[E, Nothing]]
        } // ...
    }
  }
}
```

---

# Running `FlatMap` Operation

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = { // ..
    io match { // ...
      case Op.FlatMap(ioA0, cont) =>
        unsafeRun(ioA0) match {
          case Exit.Succeed(a0) => dieOnException {
            unsafeRun(cont(a0))
          }

          case failCause@Exit.FailCause(_) => failCause
        } // ...
    }
  }
}
```

---

# Running `FoldCauseIO` Operation

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = { // ..
    io match { // ...
      case Op.FoldCauseIO(ioA0, failCauseCase, succeedCase) =>
        unsafeRun(ioA0) match {
          case Exit.FailCause(cause) => dieOnException {
            unsafeRun(failCauseCase(cause))
          }

          case Exit.Succeed(a0) => dieOnException {
            unsafeRun(succeedCase(a0))
          }
        } // ...
    }
  }
}
```
