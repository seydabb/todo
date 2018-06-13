package service

import java.sql.Connection

import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import play.api.db.{DBApi, Database}
import repository.CommentsRepository
import util.TestData._
import util.{DBConnection, TestUtil}

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

class CommentServiceSpec extends TestUtil {

  private implicit val ec = Implicits.global

  private var sut: CommentService = _
  private var dbApiMock: DBApi = _
  private var databaseMock: Database = _
  private var connectionMock: Connection = _
  private var commentsRepositoryMock: CommentsRepository = _

  before {
    commentsRepositoryMock = mock[CommentsRepository]
    dbApiMock = mock[DBApi]
    databaseMock = mock[Database]
  }

  "insertComment" should {
    "calls insertComment method of repository and insert comment & returns id" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(commentsRepositoryMock.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)).thenReturn(Future.successful(ANY_COMMENT.id))

        sut = new CommentService(commentsRepositoryMock, dbApiMock)

        val result = sut.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment).futureValue

        result mustBe ANY_COMMENT.id

        verify(commentsRepositoryMock).insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)(conn)
        verifyNoMoreInteractions(commentsRepositoryMock)
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(commentsRepositoryMock.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)).thenReturn(Future.failed(new Exception))

        sut = new CommentService(commentsRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment).futureValue
        }
      }
    }
  }
}
