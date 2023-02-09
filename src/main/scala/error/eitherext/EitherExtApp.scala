package error.eitherext

object EitherExtApp {
  def main(args: Array[String]): Unit = {
    val failure = Either.fail("Error")
    val success = Either.succeed(1)
    def parseInt(s: String): Either[String, Int] =
      Either.attempt(s.toInt)
        .refineToOrDie[NumberFormatException]
        .mapError(_ => s"Invalid integer ($s)")

    println(parseInt("42"))
    println(parseInt("boom"))
    println(parseInt("42").z.zip(parseInt("24")).z.zip(parseInt("66")))
  }
}
