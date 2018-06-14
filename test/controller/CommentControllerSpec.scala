package controller

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import controllers.CommentController
import org.mockito.Mockito.when
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, call, status, stubControllerComponents, stubPlayBodyParsers}
import service.CommentService
import util.TestData._
import util.TestUtil

import scala.concurrent.Future
import scala.concurrent.duration._

class CommentControllerSpec extends TestUtil {

  implicit val actorSystem = ActorSystem("test", ConfigFactory.load())
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(5 seconds)

  val controllerComponents: ControllerComponents = stubControllerComponents(
    playBodyParsers = stubPlayBodyParsers(materializer)
  )
  implicit val ec = controllerComponents.executionContext

  var commentServiceMock: CommentService = _
  var cut: CommentController = _

  before {
    commentServiceMock = mock[CommentService]
    cut = new CommentController(controllerComponents, commentServiceMock)
  }

  private val fakeRequest = FakeRequest(POST, s"/addComment/$ANY_TODOS_ID")

  "CommentController" when {
    "addComment" should {
      "insert a new comment and returns with 201 status code" in {
        when(commentServiceMock.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)).thenReturn(Future.successful(ANY_COMMENT_ID))
        val addCommentRequest = fakeRequest.withBody(Json.obj("comment" -> ANY_COMMENT.comment))

        val result = call(cut.addComment(ANY_TODOS_ID), addCommentRequest)
        status(result) mustBe CREATED
      }

      "returns with 400 status code & does not insert comment if json is invalid" in {
        when(commentServiceMock.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)).thenReturn(Future.failed(new Exception))
        val addCommentRequest = fakeRequest.withBody(Json.obj("invalid json" -> ANY_COMMENT.comment))

        val result = call(cut.addComment(ANY_TODOS_ID), addCommentRequest)
        status(result) mustBe BAD_REQUEST
      }

      "returns with 500 status code & does not insert comment if an exception occurs during insertion process" in {
        when(commentServiceMock.insertComment(ANY_TODOS_ID, ANY_COMMENT.comment)).thenReturn(Future.failed(new Exception))
        val addCommentRequest = fakeRequest.withBody(Json.obj("comment" -> ANY_COMMENT.comment))

        val result = call(cut.addComment(ANY_TODOS_ID), addCommentRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

    }
   }

}
