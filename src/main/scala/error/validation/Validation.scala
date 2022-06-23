package error.validation

import scala.annotation.targetName

import error.Zippable
import error.either.Either
import error.validation.Validation.Failure

enum Validation[+E, +A] { va =>
  case Failure[E](errors: Seq[E]) extends Validation[E, Nothing]
  case Success[A](result: A) extends Validation[Nothing, A]

  def map[B](trans: A => B): Validation[E, B] =
    va match {
      case Success(a) => Success(trans(a))
      case Failure(e) => Failure(e)
    }

  def flatMap[E2 >: E, B](cont: A => Validation[E2, B]): Validation[E2, B] =
    va match {
      case Success(a) => cont(a)
      case Failure(e) => Failure(e)
    }

  def mapError[E2](trans: E => E2): Validation[E2, A] =
    va match {
      case Success(a) => Success(a)
      case Failure(e) => Failure(e.map(trans))
    }

  def zip[E2 >: E, B](vb: Validation[E2, B])(using zippable: Zippable[A, B]): Validation[E2, zippable.Out] =
    va.flatMap(a => vb.map(b => zippable.zip(a, b)))

  def <*>[E2 >: E, B](vb: Validation[E2, B])(using zippable: Zippable[A, B]): Validation[E2, zippable.Out] =
    va.zip(vb)

  def zipPar[E2 >: E, B](vb: Validation[E2, B]): Validation[E2, (A, B)] =
    (va, vb) match {
      case (Success(a), Success(b)) => Success((a, b))
      case (Failure(e1), Success(_)) => Failure(e1)
      case (Success(_), Failure(e2)) => Failure(e2)
      case (Failure(e1), Failure(e2)) => Failure(e1 ++ e2)
    }

  def <&>[E2 >: E, B](vb: Validation[E2, B]): Validation[E2, (A, B)] =
    va.zipPar(vb)

  def toEither: Either[Seq[E], A] =
    va match {
      case Success(a) => Either.succeed(a)
      case Failure(e) => Either.fail(e)
    }
}

object Validation {
  def succeed[A](result: A): Validation[Nothing, A] = Success(result)
  def fail[E](error: E): Validation[E, Nothing] = Failure(List(error))

  def fromEither[E, A](either: Either[E, A]): Validation[E, A] = either.toValidation
}
