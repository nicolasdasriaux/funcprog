package error.either

import error.Zippable
import error.io.IO
import error.validation.Validation

import scala.reflect.ClassTag

enum Either[+E, +A] { va =>
  case Left[E](error: E) extends Either[E, Nothing]
  case Right[A](result: A) extends Either[Nothing, A]

  def map[B](trans: A => B): Either[E, B] =
    va match {
      case Right(a) => Right(trans(a))
      case Left(e) => Left(e)
    }

  def flatMap[E2 >: E, B](cont: A => Either[E2, B]): Either[E2, B] =
    va match {
      case Right(a) => cont(a)
      case Left(e) => Left(e)
    }

  def mapError[E2](trans: E => E2): Either[E2, A] =
    va match {
      case Right(a) => Right(a)
      case Left(e) => Left(trans(e))
    }

  def zip[E2 >: E, B](vb: Either[E2, B])
                     (using zippable: Zippable[A, B]): Either[E2, zippable.Out] =

    va.flatMap(a => vb.map(b => zippable.zip(a, b)))

  def <*>[E2 >: E, B](vb: Either[E2, B])
                     (using zippable: Zippable[A, B]): Either[E2, zippable.Out] =

    va.zip(vb)

  def toValidation: Validation[E, A] =
    va match {
      case Right(a) => Validation.succeed(a)
      case Left(e) => Validation.fail(e)
    }

  def fold[T](failCase: E => T, succeedCase: A => T): T =
    va match {
      case Right(a) => succeedCase(a)
      case Left(e) => failCase(e)
    }

  def refineOrDie[E2](failCase: PartialFunction[E, E2])
                     (using toThrowable: E <:< Throwable): Either[E2, A] =

    va.fold(
      failCase = { e =>
        val handleError: E => Option[E2] = failCase.lift
        val maybeError: Option[E2] = handleError(e)
        def die(e: E): Nothing = throw toThrowable(e)
        maybeError.fold(die(e))(Either.fail)
      },
      succeedCase = Either.succeed
    )
}

object Either {
  def succeed[A](result: A): Either[Nothing, A] = Right(result)
  def fail[E](error: E): Either[E, Nothing] = Left(error)

  def attempt[A](result: => A): Either[Throwable, A] =
    try succeed(result)
    catch {
      case defect: Throwable =>  fail(defect)
    }

  def fromValidation[E, A](validation: Validation[E, A]): Either[Seq[E], A] = validation.toEither

  extension [E <: Throwable, A](either: Either[E, A]) {
    def refineToOrDie[E2 <: E : ClassTag]: Either[E2, A] =
      either.refineOrDie({ case e: E2 => e })
  }
}
