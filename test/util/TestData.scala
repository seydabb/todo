package util

import org.joda.time.{DateTime, DateTimeZone}
import repository.Todo

object TestData {

  def now: DateTime = new DateTime

  val UUID = Guid.generateUuid
  val ANY_TODOS_ID = "any id"
  val ANY_TODO = Todo(UUID, "some todos", isDone = false, now, now)
  val ANY_TODO_WITH_HARDCODED_ID = ANY_TODO.copy(id = "hard coded id")

}
