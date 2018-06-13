package util

import org.joda.time.{DateTime, DateTimeZone}
import repository.{Comment, Todo, TodoEdit}
import utils.Guid

object TestData {

  def now: DateTime = new DateTime

  val UUID = Guid.generateUuid
  val ANY_TODOS_ID = "any id"
  val ANY_TODO = Todo(UUID, "some todos", isDone = false, now, now)
  val ANY_TODO_WITH_HARDCODED_ID = ANY_TODO.copy(id = "hard coded id")

  val ANY_TODO_EDIT = TodoEdit(ANY_TODO.todo, ANY_TODO.isDone)

  val ANY_COMMENT_ID = "any comment id"
  val ANY_COMMENT = Comment(ANY_COMMENT_ID, ANY_TODOS_ID, "any comment", now, now)

}
