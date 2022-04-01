package error.io

enum Exit[+E, +A] {
  case Succeed[A](result: A) extends Exit[Nothing, A]
  case FailCause[E](cause: Cause[E]) extends Exit[E, Nothing]
}

object Exit {
  def succeed[A](result: A): Exit[Nothing, A] = Succeed(result)
  def failCause[E](cause: Cause[E]): Exit[E, Nothing] = FailCause(cause)
  def fail[E](error: E): Exit[E, Nothing] = failCause(Cause.fail(error))
  def die(defect: Throwable): Exit[Nothing, Nothing] = failCause(Cause.die(defect))
}
