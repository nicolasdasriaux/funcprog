package io.purefree

import scala.io.StdIn
import IO.Op

trait IO[+A] {
  def flatMap[B](cont: A => IO[B]): IO[B] = Op.FlatMap(this, cont)
  def map[B](trans: A => B): IO[B] = this.flatMap(a => IO.succeed(trans(a)))
}

object IO {
  def succeed[A](a: A): IO[A] = Op.Succeed(a)
  def attempt[A](a: => A): IO[A] = Op.Attempt(() => a)

  // An ADT (Algebraic Data Type) with a type parameter
  // known as GADT (Generalized Algebraic Data Type)
  enum Op[+A] extends IO[A] {
    case Succeed(result: A) extends Op[A]
    case Attempt(result: () => A) extends Op[A]

    case FlatMap[A0, A](
                         io: IO[A0],
                         cont: A0 => IO[A]
                       ) extends Op[A]
  }
}
