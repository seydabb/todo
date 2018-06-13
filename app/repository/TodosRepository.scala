package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.JodaParameterMetaData._
import anorm.SqlParser.{get, str}
import anorm.{SQL, ~, _}
import org.joda.time.DateTime
import play.api.db.DBApi
import play.api.libs.json.{Format, Json}
import utils.Guid

import scala.concurrent.{ExecutionContext, Future}

case class Todo(id: String = Guid.generateUuid,
                todo: String,
                isDone: Boolean = false,
                createdAt: DateTime = new DateTime,
                updatedAt: DateTime = new DateTime)

case class TodoEdit(todo: String, isDone: Boolean)

object TodoEdit {
  implicit val format: Format[TodoEdit] = Json.format[TodoEdit]
}

object Todo {

  def apply(todo: String, isDone: Boolean): Todo = {
    new Todo(todo = todo, isDone = isDone)
  }
}

@javax.inject.Singleton
class TodosRepository @Inject()(dbapi: DBApi)(implicit ec: ExecutionContext) {

  private val date = new DateTime

  val tableName = "todos"

  private val db = dbapi.database("default")

  def insertTodos(todo: Todo)(implicit c: Connection): Future[String] = Future {
    val id = SQL(s"insert into $tableName(id, todo, isDone, createdAt, updatedAt) values ({id}, {todo}, {isDone}, {createdAt}, {updatedAt})")
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

  def delete(id: String)(implicit c: Connection): Future[Int] = Future {
    SQL(
      s"""
         |delete
         |from $tableName
         |where id = {id}
        """.stripMargin)
      .on("id" -> id).executeUpdate()

  }

  def update(id: String, todoEdit: TodoEdit)(implicit c: Connection): Future[Int] = Future {
    SQL(
      s"""
         |update $tableName
         |set todo = {todo}, isDone = {isDone}, updatedAt = {updatedAt}
         |where id = {id}
        """.stripMargin)
      .on(
        'id -> id,
        'todo -> todoEdit.todo,
        'isDone -> todoEdit.isDone,
        'updatedAt -> date).executeUpdate()

  }

  private val todosParser = {
    get[String](s"$tableName.id") ~
      get[String](s"$tableName.todo") ~
      get[Boolean](s"$tableName.isDone") ~
      get[DateTime](s"$tableName.createdAt") ~
      get[DateTime](s"$tableName.updatedAt") map {
      case id ~ todo ~ isDone ~ createdAt ~ updatedAt =>
        Todo(id, todo, isDone, createdAt, updatedAt)
    }
  }
}

