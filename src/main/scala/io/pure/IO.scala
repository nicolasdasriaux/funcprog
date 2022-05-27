package io.pure

case class IO[+A](unsafeIO: () => A) { ioA =>
  def flatMap[B](cont: A => IO[B]): IO[B] = {
    IO(
      () => {
        val a: A = ioA.unsafeIO()
        val ioB: IO[B] = cont(a)
        val b: B = ioB.unsafeIO()
        b
      }
    )
  }

  def map[B](trans: A => B): IO[B] = {
    IO(
      () => {
        val a: A = ioA.unsafeIO()
        val b: B = trans(a)
        b
      }
    )
  }
}

object IO {
  def succeed[A](a: => A): IO[A] = IO(() => a)
}
