package error.either

object EitherAttemptApp {
  def main(args: Array[String]): Unit = {
    object IntField {
      def parse(s: String): Either[String, Int] =
        Either.attempt(s.toInt)
          .refineToOrDie[NumberFormatException]
          .mapError(_ => s"Invalid integer ($s)")
    }

    val success = IntField.parse("12")
    println(s"success = $success")

    val failure = IntField.parse("AAA")
    println(s"failure = $failure")
  }
}
