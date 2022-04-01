package error.io

object Runtime {
  import IO.Op

  def unsafeRun[E, A](io: IO[E, A]): Exit[E, A] = {
    def failOnException[E <: Throwable, A](result: => Exit[E, A]): Exit[E, A] =
      try result catch {
        case ex: E => Exit.fail(ex)
      }

    def dieOnException[E, A](result: => Exit[E, A]): Exit[E, A] =
      try result catch {
        case ex: Throwable => Exit.die(ex)
      }

    io match {
      case Op.Succeed(result: (() => A)) =>
        // Die if exception thrown by result()
        dieOnException {
          Exit.succeed(result())
        }

      case Op.FailCause(cause: (() => Cause[E])) =>
        // Die if exception thrown by cause()
        dieOnException {
          Exit.failCause(cause())
        }

      case Op.Attempt(result: (() => A)) =>
        // Fail if exception thrown by result()
        failOnException {
          Exit.succeed(result())
        }

      case Op.FlatMap(io: IO[Any, Any], cont: (Any => IO[E, A])) =>
        unsafeRun(io) /* RECURSE */ match {
          case Exit.Succeed(a0) =>
            // Die if exception thrown by continuation()
            dieOnException {
              unsafeRun(cont(a0)) /* RECURSE */
            }

          case exit@Exit.FailCause(_) => exit
        }

      case Op.FoldCauseIO(io: IO[Any, Any], onFailCause: (Cause[Any] => IO[E, A]), onSucceed: (Any => IO[E, A])) =>
        unsafeRun(io) /* RECURSE */ match {
          case Exit.Succeed(a0) =>
            // Die if exception thrown by `onSucceed()` (or `unsafeRun()`)
            dieOnException {
              unsafeRun(onSucceed(a0)) /* RECURSE */
            }

          case Exit.FailCause(cause) =>
            // Die if exception thrown by `onFailCause()` (or `unsafeRun()`)
            dieOnException {
              unsafeRun(onFailCause(cause)) /* RECURSE */
            }
        }
    }
  }
}
