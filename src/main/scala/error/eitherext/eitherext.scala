package error.eitherext

import error.Zippable

import scala.reflect.ClassTag

extension (self: Either.type) {
  def succeed[A](result: A): Either[Nothing, A] = Right(result)
  def fail[E](error: E): Either[E, Nothing] = Left(error)

  def attempt[A](result: => A): Either[Throwable, A] =
    try succeed(result)
    catch {
      case defect: Throwable => fail(defect)
    }
}

given eitherToZEither[E, A]: Conversion[scala.util.Either[E, A], ZEither[E, A]] = new ZEither[E, A](_)

class ZEither[E, A](va: Either[E, A]) {
  inline def z: ZEither[E, A] = this

  def zip[E2 >: E, B](vb: Either[E2, B])
                     (using zippable: Zippable[A, B]): Either[E2, zippable.Out] =

    va.flatMap(a => vb.map(b => zippable.zip(a, b)))

  def mapError[E2](trans: E => E2): Either[E2, A] =
    va match {
      case success@Right(_) => success.asInstanceOf[Either[E2, A] ]
      case Left(e) => Left(trans(e))
    }

  def refineOrDie[E2](failCase: PartialFunction[E, E2])
                     (using toThrowable: E <:< Throwable): Either[E2, A] =

    va.fold(
      fa = { e =>
        val handleError: E => Option[E2] = failCase.lift
        val maybeError: Option[E2] = handleError(e)

        def die(e: E): Nothing = throw toThrowable(e)

        maybeError.fold(die(e))(Either.fail)
      },
      fb = Either.succeed
    )
}

extension[E <: Throwable, A] (either: Either[E, A]) {
  def refineToOrDie[E2 <: E : ClassTag]: Either[E2, A] =
    either.refineOrDie({ case e: E2 => e })
}
