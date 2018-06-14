package controller

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import controllers.TodoController
import model.TodoEdit
import org.mockito.Mockito.when
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.{FakeHeaders, FakeRequest}
import play.api.test.Helpers.{DELETE, PATCH, POST, GET, call, status, stubControllerComponents, stubPlayBodyParsers}
import service.TodoService
import util.TestData._
import util.TestUtil
import play.api.http.Status._
import repository.Todo
import org.mockito.ArgumentMatchers.any
import utils.TodoNotFoundException

import scala.concurrent.Future
import scala.concurrent.duration._

class TodoControllerSpec extends TestUtil {

  implicit val actorSystem = ActorSystem("test", ConfigFactory.load())
  implicit val materializer = ActorMaterializer()
  implicit val timeout = Timeout(5 seconds)

  val controllerComponents: ControllerComponents = stubControllerComponents(
    playBodyParsers = stubPlayBodyParsers(materializer)
  )
  implicit val ec = controllerComponents.executionContext

  var todoServiceMock: TodoService = _
  var cut: TodoController = _

  before {
    todoServiceMock = mock[TodoService]
    cut = new TodoController(controllerComponents, todoServiceMock)
  }

  val expectedContentType = "application/json"
  val headers = FakeHeaders(Seq("Accept" -> expectedContentType))

  private val createTodoFakeRequest = FakeRequest(POST, "/todos").withHeaders(headers)
  private val editTodoFakeRequest = FakeRequest(PATCH, s"/todos/$ANY_TODOS_ID").withHeaders(headers)
  private val deleteTodoFakeRequest = FakeRequest(DELETE, s"/todos/$ANY_TODOS_ID").withHeaders(headers)
  private val getTodosFakeRequest = FakeRequest(GET, s"/todos/").withHeaders(headers)

  "TodoController" when {

    "createTodo" should {
      "insert a new todo and returns with 201 status code" in {
        when(todoServiceMock.insertTodo(any[Todo])).thenReturn(Future.successful(ANY_TODOS_ID))
        val insertTodoRequest = createTodoFakeRequest.withBody(Json.obj("todo" -> ANY_TODO.todo))

        val result = call(cut.createTodo, insertTodoRequest)
        status(result) mustBe CREATED
      }

      "returns with 400 status code & does not insert todo if json is invalid" in {
        val insertTodoRequest = createTodoFakeRequest.withBody(Json.obj("invalid json" -> ANY_COMMENT.comment))

        val result = call(cut.createTodo, insertTodoRequest)
        status(result) mustBe BAD_REQUEST
      }

      "returns with 500 status code & does not insert todo if an exception occurs during insertion process" in {
        when(todoServiceMock.insertTodo(any[Todo])).thenReturn(Future.failed(new Exception))
        val insertTodoRequest = createTodoFakeRequest.withBody(Json.obj("todo" -> ANY_TODO.todo))

        val result = call(cut.createTodo, insertTodoRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

    }

    "editTodo" should {
      "edit an existing todo and returns with 200 status code" in {
        when(todoServiceMock.updateTodo(any[String], any[TodoEdit])).thenReturn(Future.successful(1))
        val editTodoRequest = editTodoFakeRequest
          .withBody(Json.obj("todo" -> ANY_TODO.todo, "isDone" -> true))

        val result = call(cut.editTodo(ANY_TODOS_ID), editTodoRequest)
        status(result) mustBe OK
      }

      "returns with 404 status code if related todo does not exist" in {
        when(todoServiceMock.updateTodo(any[String], any[TodoEdit])).thenReturn(Future.failed(new TodoNotFoundException("error")))
        val editTodoRequest = editTodoFakeRequest
          .withBody(Json.obj("todo" -> ANY_TODO.todo, "isDone" -> true))

        val result = call(cut.editTodo(ANY_TODOS_ID), editTodoRequest)
        status(result) mustBe NOT_FOUND
      }

      "returns with 500 status code if error occurs" in {
        when(todoServiceMock.updateTodo(any[String], any[TodoEdit])).thenReturn(Future.failed(new Exception))
        val editTodoRequest = editTodoFakeRequest
          .withBody(Json.obj("todo" -> ANY_TODO.todo, "isDone" -> true))

        val result = call(cut.editTodo(ANY_TODOS_ID), editTodoRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "returns with 400 status code if json is invalid" in {
        when(todoServiceMock.updateTodo(any[String], any[TodoEdit])).thenReturn(Future.successful(1))

        val editTodoRequest = editTodoFakeRequest
          .withBody(Json.obj("invalid json" -> ANY_TODO.todo))

        val result = call(cut.editTodo(ANY_TODOS_ID), editTodoRequest)
        status(result) mustBe BAD_REQUEST
      }
    }

    "deleteTodo" should {
      "delete an existing todo and returns with 200 status code" in {
        when(todoServiceMock.deleteTodo(ANY_TODOS_ID)).thenReturn(Future.successful(1))
        val deleteTodoRequest = deleteTodoFakeRequest.withBody(Json.obj())

        val result = call(cut.deleteTodo(ANY_TODOS_ID), deleteTodoRequest)
        status(result) mustBe OK
      }

      "returns with 404 status code if related todo does not exist" in {
        when(todoServiceMock.deleteTodo(ANY_TODOS_ID)).thenReturn(Future.failed(new TodoNotFoundException("error")))
        val deleteTodoRequest = deleteTodoFakeRequest.withBody(Json.obj())

        val result = call(cut.deleteTodo(ANY_TODOS_ID), deleteTodoRequest)
        status(result) mustBe NOT_FOUND
      }

      "returns with 500 status code if error occurs" in {
        when(todoServiceMock.deleteTodo(ANY_TODOS_ID)).thenReturn(Future.failed(new Exception))
        val deleteTodoRequest = deleteTodoFakeRequest.withBody(Json.obj())

        val result = call(cut.deleteTodo(ANY_TODOS_ID), deleteTodoRequest)
        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "getTodos" should {
      "get all todos and it's related comments and returns with 200 status code" in {
        when(todoServiceMock.getAllTodosWithComments())
          .thenReturn(Future.successful(TODOS_WITH_COMMENTS))
        val getTodosRequest = getTodosFakeRequest.withBody(Json.obj())

        val result = call(cut.getTodos, getTodosRequest)
        status(result) mustBe OK
      }

      "returns with 200 status code even if there is no todos in db" in {
        when(todoServiceMock.getAllTodosWithComments())
          .thenReturn(Future.successful(List.empty))
        val getTodosRequest = getTodosFakeRequest.withBody(Json.obj())

        val result = call(cut.getTodos, getTodosRequest)
        status(result) mustBe OK
      }
    }
  }

}
