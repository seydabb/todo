package repository

import java.sql.Connection
import javax.inject.Inject

import anorm.SqlParser.{get, str}
import anorm.{Row, SQL, SimpleSql, ~}
import org.joda.time.DateTime
import play.api.db.DBApi
import anorm.JodaParameterMetaData._
import util.Guid

import scala.concurrent.{ExecutionContext, Future}

case class Comment(id: String,
                   fkTodoId: String,
                   comment: String,
                   createdAt: DateTime = new DateTime,
                   updatedAt: DateTime = new DateTime)

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
