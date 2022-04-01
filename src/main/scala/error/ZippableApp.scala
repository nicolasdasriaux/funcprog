package error

object ZippableApp {
  def example[T](name: String)(code: => T): T = {
    code
  }

  def main(args: Array[String]): Unit = {
    def zip[A, B](a: A, b: B)(using zippable: Zippable[A, B]): zippable.Out =
      zippable.zip(a, b)

    example("pair") {
      val value1: Int = 1
      val value2: String = "A"
      val zipped: (Int, String) = zip(value1, value2)
    }

    example("prepend") {
      val value1: Int = 1
      val tuple2: (String, String) = ("A", "B")
      val zipped: (Int, String, String) = zip(value1, tuple2)
    }

    example("append") {
      val tuple1: (Int, Int) = (1, 2)
      val value2: String = "A"
      val zipped: (Int, Int, String) = zip(tuple1, value2)
    }

    example("concat") {
      val tuple1: (Int, Int) = (1, 2)
      val tuple2: (String, String) = ("A", "B")
      val zipped: (Int, Int, String, String) = zip(tuple1, tuple2)
    }

    example("prepend unit ignored") {
      val unit: Unit = ()
      val value2: String = "A"
      val tuple2: (String, String) = ("A", "B")

      val zipped1: String = zip(unit, value2)
      val zipped2: (String, String) = zip(unit, tuple2)
    }

    example("append unit ignored") {
      val value1: Int = 1
      val tuple1: (Int, Int) = (1, 2)
      val unit: Unit = ()

      val zipped1: Int = zip(value1, unit)
      val zipped2: (Int, Int) = zip(tuple1, unit)
    }

    example("pair unit") {
      val unit: Unit = ()
      val zipped1: Unit = zip(unit, unit)
    }
  }
}
