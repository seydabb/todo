package util

import model.{TodoEdit, TodosWithComments}
import org.joda.time.DateTime
import repository.{Comment, Todo}
import utils.Guid

object TestData {

  def now: DateTime = new DateTime

  val UUID = Guid.generateUuid
  val ANY_TODOS_ID = "any id"
  val ANY_TODO = Todo(UUID, "some todos", isDone = false, now, now)

  val ANY_TODO_WITH_HARDCODED_ID = ANY_TODO.copy(id = ANY_TODOS_ID)

  val ANY_TODO_EDIT = TodoEdit(Some(ANY_TODO.todo), Some(ANY_TODO.isDone))
  val ANY_TODO_WILL_BE_EDITED = ANY_TODO_WITH_HARDCODED_ID

  val ANY_COMMENT_ID = "any comment id"
  val ANY_COMMENT = Comment(ANY_COMMENT_ID, ANY_TODOS_ID, "any comment", now, now)

  val TODOS_WITH_COMMENTS = List(TodosWithComments(ANY_TODOS_ID, ANY_TODO.todo, List(ANY_COMMENT)))

}
