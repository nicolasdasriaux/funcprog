package error.io

enum Cause[+E] {
  case Fail[E](error: E) extends Cause[E]
  case Die(defect: Throwable) extends Cause[Nothing]

  def fold[T](failCase: E => T, dieCase: Throwable => T): T =
    this match {
      case Fail(error) => failCase(error)
      case Die(defect) => dieCase(defect)
    }
}

object Cause {
  def fail[E](error: E): Cause[E] = Fail(error)
  def die(defect: Throwable): Cause[Nothing] = Die(defect)
}
