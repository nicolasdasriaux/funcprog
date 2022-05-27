package io.purefree

import IO.Op

object Runtime {
  def unsafeRun[A](io: IO[A]): A = {
    io match {
      case Op.Succeed(result /* () => A */) => result()

      case Op.FlatMap(ioA0 /* IO[A0] */, cont /* A0 => IO[A] */) =>
        val a0 = /* RECURSE */ unsafeRun(ioA0)
        val ioA = cont(a0)
        val a = /* TAIL_CALL_RECURSE */ unsafeRun(ioA)
        a
    }
  }
}
