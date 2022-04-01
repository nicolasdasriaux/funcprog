package error.validation

import error.either.Either


object ValidationApp {
  def main(args: Array[String]): Unit = {
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

      {
        val success = PointForm.parse(PointForm(x = "1", y = "2"))
        println(s"success = $success")
        // success = Success(Point(1,2))

        val xFailure = PointForm.parse(PointForm(x = "AAA", y = "2"))
        println(s"xFailure = $xFailure")
        // xFailure = Failure(List(x: Invalid integer (AAA)))

        val yFailure = PointForm.parse(PointForm(x = "1", y = "BBB"))
        println(s"yFailure = $yFailure")
        // yFailure = Failure(List(y: Invalid integer (BBB)))

        val xAndYFailure = PointForm.parse(PointForm(x = "AAA", y = "BBB"))
        println(s"xAndYFailure = $xAndYFailure")
        // xAndYFailure = Failure(List(x: Invalid integer (AAA), y: Invalid integer (BBB)))
        // Both errors!
      }

      {
        val rectangleForm = RectangleForm(p1 = PointForm("XXX1", "YYY1"), p2 = PointForm("3", "YYY2"))
        val failure = RectangleForm.parse(rectangleForm)
        println(s"failure = $failure")
        // failure = Failure(List(p1.x: Invalid integer (XXX1), p1.y: Invalid integer (YYY1), p2.y: Invalid integer (YYY2)))
        // All errors
      }
    }
  }
}
