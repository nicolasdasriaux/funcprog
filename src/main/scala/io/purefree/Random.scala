package io.purefree

object Random {
  def nextIntBetween(min: Int, max: Int): IO[Int] =
    IO.succeed(scala.util.Random.nextInt(max - min + 1) + min)
}
