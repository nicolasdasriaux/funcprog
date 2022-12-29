autoscale: true
footer: Practical Functional Programming - Handling I/O Errors
slidenumbers: true

# Practical Functional Programming

# [fit] **Handling I/O Errors**

## in Scala

---

# Previously, **Infallible I/O**

---

# Infallible I/O

```scala
trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] = ???
  def map[B](trans: A => B): IO[B] = ???
}

object IO {
  def succeed[A](a: => A): IO[A] = ???
  // ...
}
```

---

# Interpreting Infallible I/O

```scala
object Runtime {
  def unsafeRun[A](io: IO[A]): A = ???
}
```

---

# Fallible I/O

---

# Fallible I/O

```scala
trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = ???
  def map[B](trans: A => B): IO[E, B] = ???
  // ...
}

object IO {
  def succeed[A](result: => A): IO[Nothing, A] = ???
  def fail[E](error: => E): IO[E, Nothing] = ???
  def die(defect: Throwable): IO[Nothing, Nothing] = ???
  // ...
}
```

---

# Interpreting Fallible I/O

```scala
object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = ???
}
```

---

# Exit for Fallible I/O

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

# Cause for Failure

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

# Failing with a Cause (revisited)

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

# Title

```scala
trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = ???
  def map[B](trans: A => B): IO[E, B] = ???

  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] = ???

  // ...
}
```

---

# Title

```scala
inline def runFoldCauseIO[E0, A0, E, A](
                                         ioA0: IO[E0, A0], 
                                         onFailCause: Cause[E0] => IO[E, A], 
                                         onSucceed: A0 => IO[E, A]
                                       ) =
  unsafeRun(ioA0) match {
    case Exit.FailCause(cause /* Cause[E0] */) => dieOnException {
      unsafeRun(onFailCause(cause))
    }
    case Exit.Succeed(a0 /* A0 */) => dieOnException {
      unsafeRun(onSucceed(a0))
    }
  }
```
