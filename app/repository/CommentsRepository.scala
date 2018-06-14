package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.SqlParser.{get, str}
import anorm.{SQL, ~}
import org.joda.time.DateTime
import play.api.db.DBApi
import anorm.JodaParameterMetaData._
import play.api.libs.json.{Format, Json}
import utils.Guid


import scala.concurrent.{ExecutionContext, Future}

case class Comment(id: String,
                   fkTodoId: String,
                   comment: String,
                   createdAt: DateTime = new DateTime,
                   updatedAt: DateTime = new DateTime)

object Comment {
  import play.api.libs.json.JodaWrites._
  import play.api.libs.json.JodaReads._

  implicit val format: Format[Comment] = Json.format[Comment]

  //read it from config
  val tableName = "comments"

  val commentParser = {
    get[String](s"$tableName.id") ~
      get[String](s"$tableName.fkTodoId") ~
      get[String](s"$tableName.comment") ~
      get[DateTime](s"$tableName.createdAt") ~
      get[DateTime](s"$tableName.updatedAt") map {
      case id ~ fkTodoId ~ comment ~ createdAt ~ updatedAt =>
        Comment(id, fkTodoId, comment, createdAt, updatedAt)
    }
  }
}

@javax.inject.Singleton
class CommentsRepository @Inject()(dbapi: DBApi, todosRepository: TodosRepository)(implicit ec: ExecutionContext) {

  private val date = new DateTime
  val tableName = "comments"

  private val db = dbapi.database("default")

  def insertComment(todoId: String, comment: String)(implicit c: Connection): Future[String] = Future {
    val id = SQL(s"insert into $tableName(id, fkTodoId, comment, createdAt, updatedAt) values ({id}, {fkTodoId}, {comment}, {createdAt}, {updatedAt})")
      .on(
        'id -> Guid.generateUuid,
        'fkTodoId -> todoId,
        'comment -> comment,
        'createdAt -> date,
        'updatedAt -> date).executeInsert(str(1).+)

    if (id.nonEmpty) {
      id.head
    } else {
      throw new Exception(s"Comment could not be added! $comment")
    }
  }
}
