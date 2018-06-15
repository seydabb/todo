package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.JodaParameterMetaData._
import anorm.SqlParser.{get, str}
import anorm.{SQL, ~, _}
import model.TodosWithComments
import org.joda.time.DateTime
import play.api.db.DBApi
import play.api.libs.json.{Format, Json}
import utils.Guid

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

case class Todo(id: String = Guid.generateUuid,
                todo: String,
                isDone: Boolean = false,
                createdAt: DateTime = new DateTime,
                updatedAt: DateTime = new DateTime)

object Todo {
  import play.api.libs.json.JodaWrites._
  import play.api.libs.json.JodaReads._

  implicit val format: Format[Todo] = Json.format[Todo]

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
      //TODO: firstly I can check the existence of comment and try instead of Try
      val deletedCommentCountAsOpt = Try(SQL(s"delete from $commentsTableName WHERE fkTodoId = {todoId}").on("todoId" -> todoId).executeUpdate()).toOption
      val deletedTodosCountAsOpt = Try(SQL(s"delete from $todosTableName WHERE id = {todoId}").on("todoId" -> todoId).executeUpdate()).toOption
      Right(isDeleted(deletedCommentCountAsOpt, deletedTodosCountAsOpt))
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

  private def isDeleted(deletedCommentCountAsOpt: Option[Int], deletedTodoCountAsOpt: Option[Int]): Boolean = {
    (deletedCommentCountAsOpt, deletedTodoCountAsOpt) match {
      case (Some(_), Some(i)) if i > 0 => true
      case _ => false
    }
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
    todosParser map {
      case todo => TodosWithComments(todo, Nil)
    }
  }

  val joinedParser: RowParser[(TodosWithComments, Option[Comment])] = {
    simpleTodosWithComments ~ Comment.commentParser.? map {
      case todo~comment => (todo, comment)
    }
  }
}

