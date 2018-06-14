package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.JodaParameterMetaData._
import anorm.SqlParser.{get, str}
import anorm.{SQL, ~, _}
import model.TodosWithComments
import org.joda.time.DateTime
import play.api.db.DBApi
import utils.Guid

import scala.concurrent.{ExecutionContext, Future}

case class Todo(id: String = Guid.generateUuid,
                todo: String,
                isDone: Boolean = false,
                createdAt: DateTime = new DateTime,
                updatedAt: DateTime = new DateTime)

object Todo {

  def apply(todo: String, isDone: Boolean): Todo = {
    new Todo(todo = todo, isDone = isDone)
  }
}

@javax.inject.Singleton
class TodosRepository @Inject()(dbapi: DBApi)(implicit ec: ExecutionContext) {

  private val date = new DateTime

  val todosTableName = "todos"
  val commentsTableName = "comments"

  private val db = dbapi.database("default")

  def insertTodos(todo: Todo)(implicit c: Connection): Future[String] = Future {
    val id = SQL(s"insert into $todosTableName(id, todo, isDone, createdAt, updatedAt) values ({id}, {todo}, {isDone}, {createdAt}, {updatedAt})")
      .on(
        'id -> todo.id,
        'todo -> todo.todo,
        'isDone -> todo.isDone,
        'createdAt -> todo.createdAt,
        'updatedAt -> todo.updatedAt).executeInsert(str(1).+)

    if (id.nonEmpty) {
      id.head
    } else {
      throw new Exception(s"Todo could not be inserted! $todo")
    }
  }

  def delete(todoId: String): Either[String, Boolean] = {
    db.withTransaction { implicit connection =>
      val deletedRowCount = SQL(s"delete from $commentsTableName WHERE fkTodoId = {todoId}").on("todoId" -> todoId).executeUpdate()
      SQL(s"delete from $todosTableName WHERE id = {todoId}").on("todoId" -> todoId).executeUpdate()
      if (isDeleted(deletedRowCount)) Right(true) else Right(false)
    }
  }

  def update(newTodo: Todo)(implicit c: Connection): Future[Int] = Future {
    SQL(
      s"""
         |update $todosTableName
         |set todo = {todo}, isDone = {isDone}, updatedAt = {updatedAt}
         |where id = {id}
    """.stripMargin)
      .on(
        'id -> newTodo.id,
        'todo -> newTodo.todo,
        'isDone -> newTodo.isDone,
        'updatedAt -> date)
      .executeUpdate()
  }

  def findById(id: String)(implicit c: Connection): Future[Option[Todo]] = Future {
    val query: SimpleSql[Row] = SQL(
      s"""
         |select *
         |from $todosTableName
         |where id = {id}
        """.stripMargin)
      .on("id" -> id)
    query.as(todosParser.singleOpt)
  }

  def getAllTodosWithComments()(implicit c: Connection) = Future {
    SQL("""SELECT * FROM todos t
         LEFT OUTER JOIN comments c ON(c.fkTodoId = t.id)
        """).as(joinedParser *)
      .groupBy(_._1)
      .mapValues(_.flatMap(_._2))
      .toList
      .map { case (todos, comments) => todos.copy(comments = comments) }
  }

  private def isDeleted(deletedRowCount: Int): Boolean = {
    if (deletedRowCount > 0) true else false
  }

  private val todosParser = {
    get[String](s"$todosTableName.id") ~
      get[String](s"$todosTableName.todo") ~
      get[Boolean](s"$todosTableName.isDone") ~
      get[DateTime](s"$todosTableName.createdAt") ~
      get[DateTime](s"$todosTableName.updatedAt") map {
      case id ~ todo ~ isDone ~ createdAt ~ updatedAt =>
        Todo(id, todo, isDone, createdAt, updatedAt)
    }
  }

  val simpleTodosWithComments: RowParser[TodosWithComments] = {
    get[String](s"$todosTableName.id") ~
      get[String](s"$todosTableName.todo") map {
      case id~todo => TodosWithComments(id, todo, Nil)
    }
  }

  val joinedParser: RowParser[(TodosWithComments, Option[Comment])] = {
    simpleTodosWithComments ~ Comment.commentParser.? map {
      case todo~comment => (todo, comment)
    }
  }
}

