package io.pure

object Runtime {
  def unsafeRun[A](io: IO[A]): A = io.unsafeIO()
}
