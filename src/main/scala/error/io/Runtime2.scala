package error.io

import error.io.IO.Op

object Runtime2 {
  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = {
    inline def dieOnException[E, A](result: => Exit[E, A]): Exit[E, A] =
      try {
        result
      } catch {
        case ex: Throwable => Exit.die(ex)
      }

    io match {
      case Op.Succeed(result) => dieOnException {
        Exit.succeed(result())
      }

      case Op.FailCause(cause) => dieOnException {
        Exit.failCause(cause())
      }

      case Op.Attempt(result) =>
        try {
          Exit.succeed(result())
        } catch {
          // Fail with the exception as an error if exception thrown
          case defect: Throwable => Exit.fail(defect).asInstanceOf[Exit[E, Nothing]]
        }

      case Op.FlatMap(ioA0, cont) =>
        unsafeRun(ioA0) match {
          case Exit.Succeed(a0) => dieOnException {
            unsafeRun(cont(a0))
          }

          case failCause@Exit.FailCause(_) /* _: E */ => failCause
        }

      case Op.FoldCauseIO(ioA0, failCauseCase, succeedCase) =>
        unsafeRun(ioA0) match {
          case Exit.FailCause(cause) => dieOnException {
            unsafeRun(failCauseCase(cause))
          }

          case Exit.Succeed(a0) => dieOnException {
            unsafeRun(succeedCase(a0))
          }
        }
    }
  }
}
