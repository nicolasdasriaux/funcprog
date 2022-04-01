package error

object TupleApp {
  def main(args: Array[String]): Unit = {
    val t2: (Int, String) = (1, "Pen")
    val t3: (Int, String, Boolean) = (1, "Pen", true)
    val t4: (Int, String, Boolean, Int) = (1, "Pen", true, 500)

    val t30: (
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String,
      Int, String
    ) = (
      1, "A",
      2, "B",
      3, "C",
      4, "D",
      5, "E",
      6, "F",
      7, "G",
      8, "H",
      9, "I",
      10, "J",
      11, "K",
      12, "L",
      13, "M",
      14, "N",
      15, "O"
    )

    val t0: EmptyTuple = Tuple()
    val t1: Tuple1[Int] = Tuple(1)
  }
}
