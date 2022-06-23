package error.validation

import error.either.Either


object FormValidationApp {
  object IntField {
    def parse(s: String): Either[String, Int] =
      Either.attempt(s.toInt)
        .refineToOrDie[NumberFormatException]
        .mapError(_ => s"Invalid integer ($s)")
  }

  case class Point(x: Int, y: Int)
  case class PointForm(x: String, y: String)

  object PointForm {
    def parse(form: PointForm): Validation[String, Point] =
      (
        IntField.parse(form.x).toValidation.mapError(e => s"x: $e") <&>
          IntField.parse(form.y).toValidation.mapError(e => s"y: $e")
        ).map((x, y) => Point(x, y))
  }

  case class Rectangle(p1: Point, p2: Point)
  case class RectangleForm(p1: PointForm, p2: PointForm)

  object RectangleForm {
    def parse(form: RectangleForm): Validation[String, Rectangle] =
      (
        PointForm.parse(form.p1).mapError(e => s"p1.$e") <&>
          PointForm.parse(form.p2).mapError(e => s"p2.$e")
        ).map((p1, p2) => Rectangle(p1, p2))
  }

  def main(args: Array[String]): Unit = {
    import Validation.*

    {
      val success = PointForm.parse(PointForm(x = "1", y = "2"))
      assert(success == Success(Point(1, 2)))

      val xFailure = PointForm.parse(PointForm(x = "XXX", y = "2"))
      assert(xFailure == Failure(Seq("x: Invalid integer (XXX)")))

      val yFailure = PointForm.parse(PointForm(x = "1", y = "YYY"))
      assert(yFailure == Failure(Seq("y: Invalid integer (YYY)")))

      val xAndYFailure = PointForm.parse(PointForm(x = "XXX", y = "YYY"))
      assert(xAndYFailure == Failure(Seq("x: Invalid integer (XXX)", "y: Invalid integer (YYY)")))
    }

    {
      val rectangleForm = RectangleForm(
        p1 = PointForm(x = "P1X", y = "2"),
        p2 = PointForm(x = "3", y = "P2Y")
      )

      val failure = RectangleForm.parse(rectangleForm)
      assert(failure == Failure(Seq("p1.x: Invalid integer (P1X)", "p2.y: Invalid integer (P2Y)")))
    }
  }
}
