package error.either

object FormEitherApp {
  object IntField {
    def parse(s: String): Either[String, Int] =
      if s.nonEmpty && s.forall(_.isDigit) then
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

  def main(args: Array[String]): Unit = {
    import  Either.*

    val success: Either[String, Point] = PointForm.parse(PointForm(x = "1", y = "2"))
    assert(success == Right(Point(1,2)))

    val xFailure: Either[String, Point] = PointForm.parse(PointForm(x = "XXX", y = "2"))
    assert(xFailure == Left("x: Invalid integer (XXX)"))

    val yFailure: Either[String, Point] = PointForm.parse(PointForm(x = "1", y = "YYY"))
    assert(yFailure == Left("y: Invalid integer (YYY))"))

    val xAndYFailure: Either[String, Point] = PointForm.parse(PointForm(x = "XXX", y = "YYY"))
    assert(xAndYFailure == Left("x: Invalid integer (XXX)"))
    // Just the first error. What about the second error?
  }
}
