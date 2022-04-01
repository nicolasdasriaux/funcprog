package error.io

import error.io.Cause.Die
import error.io.IO.{Op, failCause, succeed}

import java.io.IOException
import scala.io.StdIn
import scala.reflect.ClassTag

sealed trait IO[+E, +A] {
  def flatMap[E2 >: E, B](cont: A => IO[E2, B]): IO[E2, B] = Op.FlatMap(this, cont)
  def map[B](trans: A => B): IO[E, B] = this.flatMap(a => IO.succeed(trans(a)))

  def foldCauseIO[E2, B](
                          failCauseCase: Cause[E] => IO[E2, B],
                          succeedCase: A => IO[E2, B]
                        ): IO[E2, B] =

    Op.FoldCauseIO(this, failCauseCase, succeedCase)

  def foldIO[E2, B](
                     failCase: E => IO[E2, B],
                     succeedCase: A => IO[E2, B]
                   ): IO[E2, B] =

    this.foldCauseIO(
      failCauseCase = { cause =>
        cause.fold(
          failCase = failCase,
          dieCase = IO.die
        )
      },
      succeedCase = succeedCase
    )

  def fold[B](
               failCase: E => B,
               succeedCase: A => B
             ): IO[Nothing, B] =

    this.foldIO(e => IO.succeed(failCase(e)), a => IO.succeed(succeedCase(a)))

  def mapError[E2](trans: E => E2): IO[E2, A] =
    this.foldIO(
      failCase = { e => IO.fail(trans(e)) },
      succeedCase = IO.succeed
    )

  def catchAllCause[E2, B >: A](failCauseCase: Cause[E] => IO[E2, B]): IO[E2, B] =
    this.foldCauseIO(failCauseCase = failCauseCase, succeedCase = IO.succeed)

  def catchAll[E2, B >: A](failCase: E => IO[E2, B]): IO[E2, B] =
    this.foldIO(failCase = failCase, succeedCase = IO.succeed)

  def catchSomeCause[E2 >: E, B >: A](failCauseCase: PartialFunction[Cause[E], IO[E2, B]]): IO[E2, B] = {
    this.foldCauseIO(
      failCauseCase = { cause =>
        failCauseCase.applyOrElse(cause, IO.failCause)
      },
      succeedCase = IO.succeed
    )
  }

  def catchSome[E2 >: E, B >: A](failCase: PartialFunction[E, IO[E2, B]]): IO[E2, B] = {
    this.foldCauseIO(
      failCauseCase = { cause =>
        cause.fold(
          failCase = { error => failCase.applyOrElse(error, IO.fail) },
          dieCase = IO.die
        )
      },
      succeedCase = IO.succeed
    )
  }

  def orDie(using toThrowable: E <:< Throwable): IO[Nothing, A] =
    this.orDieWith(error => toThrowable(error))

  def orDieWith(failCase: E => Throwable): IO[Nothing, A] =
    this.foldIO(
      failCase = { error => IO.die(failCase(error)) },
      succeedCase = IO.succeed
    )

  def refineOrDie[E2](failCase: PartialFunction[E, E2])
                     (using toThrowable: E <:< Throwable): IO[E2, A] =

    this.refineOrDieWith(failCase)(toThrowable)

  def refineOrDieWith[E2](failCase: PartialFunction[E, E2])
                         (toThrowable: E => Throwable): IO[E2, A] =

    this.foldIO(
      failCase = { error =>
        val handleError: E => Option[E2] = failCase.lift
        val maybeError: Option[E2] = handleError(error)
        def die(error: E): IO[Nothing, Nothing] = IO.die(toThrowable(error))
        maybeError.fold(die(error))(IO.fail)
      },
      succeedCase = IO.succeed
    )
}

object IO {
  def succeed[A](result: => A): IO[Nothing, A] = Op.Succeed(() => result)
  def failCause[E](cause: => Cause[E]): IO[E, Nothing] = Op.FailCause(() => cause)
  def fail[E](error: => E): IO[E, Nothing] = failCause(Cause.fail(error))
  def die(defect: Throwable): IO[Nothing, Nothing] = failCause(Cause.die(defect))

  def attempt[A](result: => A): IO[Throwable, A] = Op.Attempt(() => result)

  enum Op[+E, +A] extends IO[E, A] {
    case Succeed[A](value: () => A) extends Op[Nothing, A]
    case FailCause[E](cause: () => Cause[E]) extends Op[E, Nothing]
    case Attempt[E <: Throwable, A](result: () => A) extends Op[E, A]

    case FlatMap[E, A0, A](
                            io: IO[E, A0],
                            cont: A0 => IO[E, A]
                          ) extends Op[E, A]

    case FoldCauseIO[E0, E, A0, A](
                                    io: IO[E0, A0],
                                    failCauseCase: Cause[E0] => IO[E, A],
                                    succeedCase: A0 => IO[E, A]
                                  ) extends Op[E, A]
  }

  extension[E <: Throwable, A] (zio: IO[E, A]) {
    def refineToOrDie[E2 <: E : ClassTag]: IO[E2, A] =
      zio.refineOrDie({ case e: E2 => e })
  }

  def fromEither[E, A](either: => Either[E, A]): IO[E, A] =
    succeed(either).flatMap(_.fold(fail, succeed))
}
