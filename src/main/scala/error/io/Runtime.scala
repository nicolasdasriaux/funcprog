package error.io

import IO.Op

object Runtime {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = {
    inline def dieOnException[E, A](result: => Exit[E, A]): Exit[E, A] =
      try result
      catch {
        case ex: Throwable => Exit.die(ex)
      }

    inline def runSucceed[A](result: () => A): Exit[Nothing, A] = dieOnException {
      Exit.succeed(result())
    }

    inline def runFailCause(cause: () => Cause[E]): Exit[E, Nothing] = dieOnException {
      Exit.failCause(cause())
    }

    inline def runAttempt[E <: Throwable](result: () => A): Exit[E, A] =
      try {
        Exit.succeed(result())
      } catch {
        // Fail with the exception as an error if exception thrown
        case defect: Throwable => Exit.fail(defect).asInstanceOf[Exit[E, Nothing]]
      }

    inline def runFlatMap[E, A0, A](ioA0: IO[E, A0], cont: A0 => IO[E, A]): Exit[E, A] =
      unsafeRun(ioA0) match {
        case Exit.Succeed(a0) /* a0: A0 */ => dieOnException {
          unsafeRun(cont(a0))
        }

        case failCause@Exit.FailCause(_) /* _: E */ => failCause
      }

    inline def runFoldCauseIO[E0, A0, E, A](
                                             ioA0: IO[E0, A0],
                                             onFailCause: Cause[E0] => IO[E, A],
                                             onSucceed: A0 => IO[E, A]
                                           ): Exit[E, A] =
      unsafeRun(ioA0) match {
        case Exit.FailCause(cause) /* cause: Cause[E0] */ => dieOnException {
          unsafeRun(onFailCause(cause))
        }

        case Exit.Succeed(a0) /* a0: A0 */ => dieOnException {
          unsafeRun(onSucceed(a0))
        }
      }

    io match {
      case Op.Succeed(result) => runSucceed(result)
      case Op.FailCause(cause) => runFailCause(cause)
      case Op.Attempt(result) => runAttempt(result)
      case Op.FlatMap(ioA0, cont) => runFlatMap(ioA0, cont)

      case Op.FoldCauseIO(ioA0, onFailCause , onSucceed) =>
        runFoldCauseIO(ioA0, onFailCause, onSucceed)
    }
  }
}
