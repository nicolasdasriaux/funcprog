package ziotest

import zio.test.*

object AssertionReified {
  def main(args: Array[String]): Unit = {
    val assertion1: Assertion[Int] =
      Assertion.isGreaterThanEqualTo(1) && Assertion.isLessThanEqualTo(5)

    val assertion2: Assertion[Either[Any, Int]] =
      Assertion.isRight(Assertion.isGreaterThanEqualTo(1))

    def between[A](min: A, max: A)(using ord: Ordering[A]): Assertion[A] =
      Assertion.isGreaterThanEqualTo(min) && Assertion.isLessThanEqualTo(max)

    val assertion: Assertion[Int] = between(min = 1, max = 10)
    val result: Boolean = assertion.test(5)
  }
}
