autoscale: true
footer: Practical Immutability
slidenumbers: true

# Practical
# [fit] **Immutability**
## in Scala

---

# Immutable Classes

---

# Immutable Class

* **Constructor** returns a new object
* **Methods** do not modify the object but return a **new object** with the modifications applied instead
* For an immutable class, Scala generates
  - a constructor to create instance :thumbsup:
  - a `copy` method to modify instances :thumbsup:

---

# Declaring an Immutable Class

```scala
case class Customer(id: Int, firstName: String, lastName: String)
```

---

# Creating an Instance

```scala
val customer = Customer(id = 1, firstName = "John", lastName = "Doe")
```

---

# Modifying an Instance (one attribute)

```scala
val modifiedCustomer = customer.copy(lastName = "Martin")
```

* Returns a **new instance** that is modified
* Previous instance remains unchanged
* Only **one attribute** modified

---

# Modifying an Instance (multiple attributes)

```scala
val modifiedCustomer = customer.copy(firstName = "Paul", lastName = "Martin")
```

* Several attributes modified with no intermediary instances
* Also allows modifying **multiple attributes** that should remain **consistent** with each other

---

# Calculating an Attribute from Other Attributes

```scala
case class Customer(id: Int, firstName: String, lastName: String) {
  def fullName: String = s"$firstName $lastName"
}
```

* From the outside, calculated attribute looks exactly the same as other attributes :thumbsup:
* **Uniform access principle**

---

# Reminder on Comparing

* By **value**, comparing **attributes** of object
* By **reference**, comparing **object identity** (pointer, address, reference...)

---

# Comparing Immutable Instances

* Immutable class implies **comparison by value**
* Scala generates consistent
    - `.equals(other)` :thumbsup:
    - `.hashCode()` :thumbsup:
* Can ultimately be customized by code
* Greatly simplifies unit test assertions :thumbsup:

---

# Comparing Immutable Instances

```scala
val customer1 = Customer(id = 1, firstName = "John", lastName = "Doe")
val customer2 = Customer(id = 1, firstName = "John", lastName = "Doe")

assert(customer1 == customer2) // Same attributes (calls equals())
assert(customer1.hashCode == customer2.hashCode)

val customer3 = Customer(id = 1, firstName = "Paul", lastName = "Martin")

assert(customer1 != customer3) // Different attributes (calls equals)
assert(customer1.hashCode != customer3.hashCode) // Most likely
```

---

# Printing Immutable Instance

* Scala generates useful `.toString()` automatically :thumbsup:
* Can ultimately be overridden by code
* Simplifies logging :thumbsup:
* Simplifies unit test debugging :thumbsup:
    - Compare with clipboard trick

---

# Printing Immutable Instance

```scala
val customer = Customer(id = 1, firstName = "John", lastName = "Doe")
println(customer.toString)
 ```

Will output something like

```
Customer(1,John,Doe)
```

---

# Preventing `null` attributes

* Attributes should never be `null`
    - `null` is evil! :smiling_imp:
* Scala will reject `null` (compiler option) :thumbsup:
* Optional attribute should be explicit using an **option type**
    - Scala `Option` is a good ... option :wink:
    - More later

---

# Scala prevents absence of attributes at creation

```scala
val customer = Customer(id = 1, lastName = "Doe")
```

Will fail to compile

```
-- Error: /Users/nicolasdasriaux/Development/presentations/funcprog/src/main/scala/immutability/Examples.scala:53:29 
53 |      val customer = Customer(id = 1, lastName = "Doe")
   |                     ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
   |missing argument for parameter firstName of method apply in object Customer: (id: Int, firstName: String, lastName: String): Customer
```

---

# Scala prevents `null` attributes

```scala
val customer = Customer(id = 1, firstName = null, lastName = "Doe")
```

Will will fail to compile

```
-- [E007] Type Mismatch Error: /Users/nicolasdasriaux/Development/presentations/funcprog/src/main/scala/immutability/Examples.scala:58:50 
58 |      val customer = Customer(id = 1, firstName = null, lastName = "Doe")
   |                                                  ^^^^
   |                                                  Found:    Null
   |                                                  Required: String
```

---

# Immutable Collections

---

# Immutable Collections

* A method that transforms an immutable collection
  - always return a **new collection** with the transformation applied
  - and keep the **original collection unchanged**
* Immutable collections **compare by value**
  - Scala implements `.equals(other)` and `.hashCode()` consistently :thumbsup:
* In principle, they **should not accept `null`** as element
  - but Scala does :imp:
* Immutable collections are special efficient data structures called **persistent data structures**

---

# Scala Immutable Collections

| Mutable (Java) | Immutable (Scala ) |
|----------------|--------------------|
| `Collection`   | `Seq`              |
| `List`         | `IndexedSeq`       |
| `Set`          | `Set`              |
| `Map`          | `Map`              |

Can be converted from and to Java

---

# Immutable Sequence

```scala
val ids: Seq[Int] = Seq(1, 2, 3, 4, 5)

val availableIds: Seq[String] =
  (0 +: ids :+ 6)
    // Add 0 at head of list
    // Add 6 as last element of list
    .filter(i => i % 2 == 0) // Keep only even numbers
    .map(i => "#" + i) // Transform to rank
```

`availableIds` will print as

```
List(#0, #2, #4, #6)
```

---

# Immutable Indexed Sequence

```scala
val commands: IndexedSeq[String] =
  IndexedSeq("command", "ls", "pwd", "cd", "man")

val availableCommands: IndexedSeq[String] =
  commands
    .tail // Drop head of list keeping only tail
    .filter(_ != "man"); // Remove man command
```

`availableCommands` will print as

```
Vector(ls, pwd, cd)
```

---

# Immutable Set

```scala
val greetings: Set[String] = Set("hello", "goodbye")

val availableGreetings =
  greetings ++ Set("hi", "bye", "hello") // Add more greetings
```

`availableGreetings` will print as

```
Set(hello, goodbye, hi, bye)
```
 
---

# Immutable Map

```scala
val idToName: Map[Int, String] = Map(
  1 -> "Peter",
  2 -> "John",
  3 -> "Mary",
  4 -> "Kate"
)

val updatedIdToName: Map[Int, String] = idToName
        .removed(1) // Remove entry with key 1
        .updated(5, "Bart") // Add entry
        .map((k, v) => (k, v.toUpperCase.nn)) // Value to upper case
```

`updatedIdToName` will print as

```
Map(2 -> JOHN, 3 -> MARY, 4 -> KATE, 5 -> BART)
```

---

# Immutable Option Type
## with _Vavr_

---

# Option Type

* An option type is a generic type such as Scala `Option[T]` that models the **presence** or the **absence** of a value of type `T`.
* Options **compare by value** :thumbsup:
* In principle, options **should not accept `null`** as present value
  - but Scala does :imp:

___

# Present Value (`Some`)

```scala
val maybeTitle: Option[String] = Some("Mister")


val displayedTitle: String = maybeTitle
        .map(_.toUpperCase.nn) // Transform value, as present
        .getOrElse("<No Title>") // Get value, as present
```

`displayedTitle` will print as

```
MISTER
```

---

# Absent Value (`None`)

```scala
val maybeTitle: Option[String] = None

val displayedTitle: String = maybeTitle
        .map(_.toUpperCase.nn) // Does nothing, as absent
        .getOrElse("<No Title>") // Returns default value, as absent
```

`displayedTitle` will print as

```
<No Title>
```

---

# Bridging with Nullable

From nullable to `Option`

```java
final Option<String> maybeTitle =
        Option.of(nullableTitle);
```

From `Option` to nullable

```java
final String nullableTitle =
        maybeTitle.getOrNull();
```

---

