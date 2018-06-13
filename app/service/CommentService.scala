package service

import java.sql.Connection
import javax.inject.Inject

import play.api.db.DBApi
import repository.CommentsRepository

import scala.concurrent.Future

class CommentService@Inject() (commentsRepository: CommentsRepository,
                               dBApi: DBApi) {

  implicit val connection: Connection = dBApi.database("default").getConnection()

  def insertComment(todoId: String, comment: String): Future[String] = {
    commentsRepository.insertComment(todoId, comment)
  }

}
