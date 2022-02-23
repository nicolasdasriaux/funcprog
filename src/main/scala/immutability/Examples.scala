package immutability

def section(name: String)(a: => Any): Unit = a

@main
def run(): Unit = {
  section("Immutable objects") {
    case class Customer(
                         id: Int,
                         firstName: String,
                         lastName: String
                       ) {
      def fullName: String = s"$firstName $lastName"
    }

    section("Create") {
      val customer = Customer(id = 1, firstName = "John", lastName = "Doe")
      println(s"customer = $customer")
      val name = customer.firstName
    }

    section("Modify") {
      val customer = Customer(id = 1, firstName = "John", lastName = "Doe")

      section("Modify single attribute") {
        val modifiedCustomer = customer.copy(lastName = "Martin")
        println(s"modifiedCustomer = $modifiedCustomer")
      }

      section("Modify multiple attributes") {
        val modifiedCustomer = customer.copy(firstName = "Paul", lastName = "Martin")
        println(s"modifiedCustomer = $modifiedCustomer")
      }
    }

    section("== / toString") {
      val customer1 = Customer(id = 1, firstName = "John", lastName = "Doe")
      val customer2 = Customer(id = 1, firstName = "John", lastName = "Doe")

      assert(customer1 ne customer2) // Different by reference (ne)
      assert(customer1 == customer2) // Same by value (== calls equals)
      assert(customer1.hashCode == customer2.hashCode)

      val customer3 = Customer(id = 1, firstName = "Paul", lastName = "Martin")

      assert(customer1 != customer3) // Different by value
      assert(customer1.hashCode != customer3.hashCode) // Not a general property!
    }

    section("toString") {
      val customer = Customer(id = 1, firstName = "John", lastName = "Doe")
      println(customer)
      // Customer(1,John,Doe)
    }

    section("No absent attribute") {
      // val customer = Customer(id = 1, lastName = "Doe")
      // Does not compile
    }

    section("No null") {
      // val customer = Customer(id = 1, firstName = null, lastName = "Doe")
      // customer.copy(firstName = null)
      // Does not compile
    }
  }

  section("Immutable Collections") {
    section("Seq") {
      val ids: Seq[Int] = Seq(1, 2, 3, 4, 5)

      println(s"ids = $ids")

      section("Alphabetic") {
        val availableIds: Seq[String] = ids
          .prepended(0) // Add 0 at head of list
          .appended(6) // Add 6 as last element of list
          .filter(i => i % 2 == 0) // Keep only even numbers
          .map(i => "#" + i) // Transform to rank

        println(s"availableIds = $availableIds")
      }

      section("Symbolic") {
        val availableIds: Seq[String] =
          (0 +: ids :+ 6)
            // Add 0 at head of list
            // Add 6 as last element of list
            .filter(i => i % 2 == 0) // Keep only even numbers
            .map(i => "#" + i) // Transform to rank

        println(s"availableIds = $availableIds")
      }
    }

    section("IndexedSeq") {
      val commands: IndexedSeq[String] =
        IndexedSeq("command", "ls", "pwd", "cd", "man")

      println(s"commands = $commands")

      val availableCommands: IndexedSeq[String] =
        commands
          .tail // Drop head of list keeping only tail
          .filter(_ != "man"); // Remove man command

      println(s"availableCommands = $availableCommands")
    }

    section("Set") {
      val greetings: Set[String] = Set("hello", "goodbye")

      println(s"greetings = $greetings")

      section("Alphabetic") {
        val availableGreetings =
          greetings.concat(Set("hi", "bye", "hello")) // Add more greetings

        println(s"availableGreetings = $availableGreetings")
      }

      section("Symbolic") {
        val availableGreetings =
          greetings ++ Set("hi", "bye", "hello") // Add more greetings

        println(s"availableGreetings = $availableGreetings")
      }
    }

    section("Map") {
      val idToName: Map[Int, String] = Map(
        1 -> "Peter",
        2 -> "John",
        3 -> "Mary",
        4 -> "Kate"
      )

      println(s"idToName = $idToName")

      section("Aplhabetic") {
        val updatedIdToName: Map[Int, String] = idToName
          .removed(1) // Remove entry with key 1
          .updated(5, "Bart") // Add entry
          .map((k, v) => (k, v.toUpperCase.nn)) // Value to upper case

        println(s"updatedIdToName = $updatedIdToName")
      }

      section("Symbolic") {
        val updatedIdToName: Map[Int, String] =
          (
            idToName
           - 1 // Remove entry with key 1
           + (5 -> "Bart") // Add entry
          )
          .map((k, v) => (k, v.toUpperCase.nn)) // Value to upper case

        println(s"updatedIdToName = $updatedIdToName")
      }
    }
  }

  section("Immutable Option") {
    section("Some") {
      val maybeTitle: Option[String] = Some("Mister")

      println(s"maybeTitle=$maybeTitle")

      val displayedTitle: String = maybeTitle
        .map(_.toUpperCase.nn) // Transform value, as present
        .getOrElse("<No Title>") // Get value, as present

      println(s"displayedTitle = $displayedTitle")
    }

    section("None") {
      val maybeTitle: Option[String] = None

      println(s"maybeTitle=$maybeTitle")

      val displayedTitle: String = maybeTitle
        .map(_.toUpperCase.nn) // // Does nothing, as absent
        .getOrElse("<No Title>") // Returns default value, as absent

      println(s"displayedTitle = $displayedTitle")
    }

    section("Option attribute") {
      case class Customer(
                           id: Int,
                           title: Option[String],
                           firstName: String,
                           lastName: String
                         )

      section("creating") {
        section("with title") {
          val titledCustomer = Customer(id = 1, title = Some("Mr"), firstName = "John", lastName = "Doe")
          println(s"titledCustomer = $titledCustomer")
        }

        section("without title") {
          val untitledCustomer = Customer(id = 1, title = None, firstName = "John", lastName = "Doe")
          println(s"untitledCustomer = $untitledCustomer")
        }
      }

      section("modifying") {
        val titledCustomer = Customer(id = 1, title = Some("Mr"), firstName = "John", lastName = "Doe")
        val untitledCustomer = Customer(id = 1, title = None, firstName = "John", lastName = "Doe")

        section("unsetting title") {
          val modifiedCustomer = titledCustomer.copy(title = None)
          println(s"modifiedCustomer = $modifiedCustomer")
        }

        section("setting title") {
          val modifiedCustomer = untitledCustomer.copy(title = Some("Miss"), firstName = "Paula")
          println(s"modifiedCustomer = $modifiedCustomer")
        }
      }
    }
  }

  section("TodoList") {
    extension [A] (self: IndexedSeq[A]) {
      def updatedWith[B >: A](index: Int, f: A => B): IndexedSeq[B] =
        self.updated(index, f(self(index)))

      def removedWhere(p: A => Boolean): IndexedSeq[A] = {
        val index = self.indexWhere(p)
        if index >= 0 then self.patch(index, IndexedSeq.empty, 1) else self
      }
    }

    case class Todo(id: Int, name: String, done: Boolean = false) {
      def markAsDone: Todo =
        this.copy(done = true)
    }

    case class TodoList(
                         name: String,
                         todos: IndexedSeq[Todo] = IndexedSeq.empty
                       ) {

      def doneCount: Int = this.todos.count(_.done)
      def pendingCount: Int = this.todos.count(!_.done)

      def addTodo(todo: Todo): TodoList = {
        val modifiedTodos = this.todos :+ todo
        this.copy(todos = modifiedTodos)
      }

      def removeTodo(todoId: Int): TodoList = {
        val todoIndex = this.todos.indexWhere(_.id == todoId)

        if (todoIndex >= 0) {
          val modifiedTodos = this.todos.patch(todoIndex, IndexedSeq.empty, 1)
          this.copy(todos = modifiedTodos)
        } else this
      }

      def markTodoAsDone(todoId: Int): TodoList = {
        val todoIndex = this.todos.indexWhere(_.id == todoId)

        if (todoIndex >= 0) {
          val todo = this.todos(todoIndex)
          val modifiedTodo = todo.markAsDone
          val modifiedTodos = this.todos.updated(todoIndex, modifiedTodo)
          this.copy(todos = modifiedTodos)
        } else this
      }
    }

    val todoList = TodoList("Food")
      .addTodo(Todo(1, "Leek"))
      .addTodo(Todo(2, "Turnip"))
      .addTodo(Todo(3, "Cabbage"))

    println(s"todoList = $todoList")

    val modifiedTodoList = todoList
      .markTodoAsDone(3)
      .removeTodo(2)

    println(s"modifiedTodoList=$modifiedTodoList")

    val doneCount = modifiedTodoList.doneCount

    println(s"doneCount = $doneCount")
  }

  section("Expressions") {
    section("if") {
      section("if ... then ... else ...") {
        val enabled: Boolean = true // ???
        val status = if enabled then "On" else "Off"
        println(s"status=$status")
      }

      section("if ... then ... else if ... then ... else ...") {
        val mark: Int = 4 // ???

        val mood =
          if 1 <= mark && mark <= 3 then "Bad"
          else if mark == 4 then "OK"
          else if 5 <= mark && mark <= 7 then "Good"
          else ??? // Should never happen, fails

        println(s"mood=$mood")
      }
    }

    section("match") {
      enum Color {
        case Red, Orange, Green
      }

      import Color.*

      val color: Color = Red // ???

      val mark = color match {
        case Red => 2
        case Orange => 4
        case Green => 6
      }

      println(s"mark=$mark")
    }

    section("try catch") {
      val input: String = "1" // ???

      val number: Option[Int] =
        try Some(input.toInt)
        catch  {
          case _: NumberFormatException => None
        }
    }

    section("Block") {
      val slope: Int = 2 // ???
      val threshold: Int = 10 // ???
      val t: Int = 2 // ???

      val altitude = {
        val y = slope * t

        if y < -threshold then -threshold
        else if y > threshold then threshold
        else y
      }
    }
  }

  section("ADT") {
    enum Direction {
      case North
      case South
      case West
      case East
    }

    import Direction.*

    case class Position(x: Int, y: Int) {
      def move(direction: Direction): Position =
        direction match {
          case North => this.copy(y = this.y - 1)
          case South => this.copy(y = this.y + 1)
          case West => this.copy(x = this.x - 1)
          case East => this.copy(x = this.x + 1)
        }
    }

    enum Action {
      case Sleep
      case Walk(direction: Direction)
      case Jump(position: Position)
    }

    import Action.*

    val actions: Seq[Action] = Seq(
      Jump(Position(5, 8)),
      Walk(North),
      Sleep,
      Walk(East)
    )

    case class Player(position: Position) {
      def act(action: Action): Player =
        action match {
          case Sleep => this
          case Walk(direction) => Player(position.move(direction))
          case Jump(position) => Player(position)
        }
    }

    val initialPlayer = Player(Position(1, 1))
    val playerActions = Seq(Jump(Position(5, 8)), Walk(North), Sleep, Walk(East))

    val finalPlayer: Player =
      playerActions.foldLeft(initialPlayer)(
        (player, action) => player.act(action)
      )

    println(s"finalPlayer=$finalPlayer")

    val successivePlayers: Seq[Player] =
      playerActions.scanLeft(initialPlayer)(
        (player, action) => player.act(action)
      )

    println(s"successivePlayers=$successivePlayers")
  }

  section("Pattern Matching") {
    section("Matching by Condition") {
      val number: Int = 19 // ???

      val label = number match {
        case 0 => "Zero"
        case n if n < 0 => "Negative"
        case 19 | 23 | 29 => "Chosen primes"
        case n if n % 2 == 0 => s"Even ($n)"
        case n => s"Odd ($n)"
      }
    }

    section("Matching by Pattern") {
      val maybeNumber: Option[Int] = Some(5) // ???

      val label = maybeNumber match {
        case Some(0) => "Zero"
        case Some(n) if n < 0 => s"Negative ($n)"
        case Some(n) if n > 0 => s"Positive ($n)"
        case None => "Absent"
      }
    }

    section("Matching by Pattern on case class") {
      case class Point(x: Int, y: Int)

      val point: Point = Point(10, 10) // ???

      val label = point match {
        case Point(0, 0) => "Center"
        case Point(x, 0) => "First axis"
        case Point(0, y) => "Second axis"
        case Point(x, y) if x == y => "First diagonal"
        case Point(x, y) if x == -y => "Second diagonal"
        case p => "Other"
      }
    }

    section("Matching by Pattern on enum") {
      enum Operation {
        case Credit(account: Int, amount: Double)
        case Debit(account: Int, amount: Double)
        case Transfer(sourceAccount: Int, targetAccount: Int, amount: Double)
      }

      import Operation.*

      case class Bank(accounts: Map[Int, Double]) {
        def process(operation: Operation): Bank = {
          operation match {
            case Credit(account, amount) => updateAccount(account, amount)
            case Debit(account, amount) => updateAccount(account, -amount)

            case Transfer(sourceAccount, destinationAccount, amount) =>
              updateAccount(sourceAccount, -amount)
                .updateAccount(destinationAccount, amount)
          }
        }

        def updateAccount(account: Int, amountDelta: Double): Bank = {
          val updatedAccounts = accounts.updatedWith(account) {
            case Some(amount) => Some(amount + amountDelta)
            case None => None
          }

          Bank(accounts = updatedAccounts)
        }
      }
    }
  }
}

