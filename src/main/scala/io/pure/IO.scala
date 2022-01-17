package io.pure

case class IO[+A](unsafeIO: () => A) { ioA =>
  def flatMap[B](cont: A => IO[B]): IO[B] = {
    val ioB: IO[B] = IO { () =>
      val a: A = ioA.unsafeIO()
      val ioB: IO[B] = cont(a)
      val b: B = ioB.unsafeIO()
      b
    }

    ioB
  }

  def map[B](trans: A => B): IO[B] = {
    val ioB: IO[B] = IO { () =>
      val a: A = ioA.unsafeIO()
      val b: B = trans(a)
      b
    }

    ioB
  }
}

object IO {
  def succeed[A](a: A): IO[A] = IO(() => a)
  def attempt[A](a: => A): IO[A] = IO(() => a)
}
