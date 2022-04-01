package error.either

object EitherApp {
  def main(args: Array[String]): Unit = {
    object IntField {
      def parse(s: String): Either[String, Int] =
        if s.forall(_.isDigit) then
          Either.succeed(s.toInt)
        else
          Either.fail(s"Invalid integer ($s)")
    }

    case class Point(x: Int, y: Int)
    case class PointForm(x: String, y: String)

    object PointForm {
      def parse(form: PointForm): Either[String, Point] =
        for {
          x <- IntField.parse(form.x).mapError(e => s"x: $e")
          y <- IntField.parse(form.y).mapError(e => s"y: $e")
        } yield Point(x, y)
    }

    val success: Either[String, Point] = PointForm.parse(PointForm("1", "2"))
    println(s"success = $success")
    // success = Right(Point(1,2))

    val xFailure: Either[String, Point] = PointForm.parse(PointForm("AAA", "2"))
    println(s"xFailure = $xFailure")
    // xFailure = Left(x: Invalid integer (AAA))

    val yFailure: Either[String, Point] = PointForm.parse(PointForm("1", "BBB"))
    println(s"yFailure = $yFailure")
    // yFailure = Left(y: Invalid integer (BBB))

    val xAndYFailure: Either[String, Point] = PointForm.parse(PointForm("AAA", "BBB"))
    println(s"xAndYFailure = $xAndYFailure")
    // xAndYFailure = Left(x: Invalid integer (AAA))
    // Just the first error. What about the second error?
    // Never tested actually
  }
}
