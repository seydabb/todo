package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.JodaParameterMetaData._
import anorm.SqlParser.{get, str}
import anorm.{Row, SQL, SimpleSql, ~, _}
import org.joda.time.DateTime
import play.api.db.DBApi

import scala.concurrent.{ExecutionContext, Future}

case class Todo(id: String,
                todo: String,
                isDone: Boolean = false,
                createdAt: DateTime = new DateTime,
                updatedAt: DateTime = new DateTime)

@javax.inject.Singleton
class TodosRepository @Inject()(dbapi: DBApi)(implicit ec: ExecutionContext) {

  val date = new DateTime
  val tableName = "todos"

  private val db = dbapi.database("default")

  def findById(id: String)(implicit c: Connection): Future[Option[Todo]] = Future {
    val query: SimpleSql[Row] = SQL(
      s"""
         |select *
         |from $tableName
         |where id = {id}
        """.stripMargin)
      .on("id" -> id)
    query.as(todosParser.singleOpt)
  }

  def insertTodos(todo: Todo)(implicit c: Connection): Future[Int] = Future {
    val id = SQL(s"insert into $tableName(id, todo, isDone, createdAt, updatedAt) values ({id}, {todo}, {isDone}, {createdAt}, {updatedAt})")
      .on(
        'id -> todo.id,
        'todo -> todo.todo,
        'isDone -> todo.isDone,
        'createdAt -> todo.createdAt,
        'updatedAt -> todo.updatedAt).executeInsert(str(1).+)

    if (id.nonEmpty) {
      1
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

  def update(id: String, newTodo: Todo)(implicit c: Connection): Future[Int] = Future {
    SQL(
      s"""
         |update $tableName
         |set todo = {todo}, isDone = {isDone}, createdAt = {createdAt}, updatedAt = {updatedAt}
         |where id = {id}
        """.stripMargin)
      .on(
        'id -> id,
        'todo -> newTodo.todo,
        'isDone -> newTodo.isDone,
        'createdAt -> newTodo.createdAt,
        'updatedAt -> newTodo.updatedAt).executeUpdate()

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

