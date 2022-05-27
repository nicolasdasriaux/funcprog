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
      case Op.Succeed(result) =>
        // Die if exception thrown
        dieOnException {
          Exit.succeed(result())
        }

      case Op.FailCause(cause) =>
        // Die if exception thrown
        dieOnException {
          Exit.failCause(cause())
        }

      case Op.Attempt(result) =>
        // Fail with the exception as an error if exception thrown
        failOnException {
          Exit.succeed(result())
        }

      case Op.FlatMap(ioA0, cont) =>
        unsafeRun(ioA0) /* RECURSE */ match {
          case Exit.Succeed(a0) =>
            dieOnException {
              unsafeRun(cont(a0)) /* RECURSE */
            }

          case exit@Exit.FailCause(_) => exit
        }

      case Op.FoldCauseIO(ioA0, onFailCause, onSucceed) =>
        unsafeRun(ioA0) /* RECURSE */ match {
          case Exit.Succeed(a0) =>
            dieOnException {
              unsafeRun(onSucceed(a0)) /* RECURSE */
            }

          case Exit.FailCause(cause) =>
            dieOnException {
              unsafeRun(onFailCause(cause)) /* RECURSE */
            }
        }
    }
  }
}
