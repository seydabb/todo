package controllers

import javax.inject.Inject

import model.TodoEdit
import play.api.libs.json.{JsError, JsSuccess, JsValue, Json}
import play.api.mvc._
import repository.Todo
import service.TodoService
import utils.CustomResult.getResult
import utils.TodoNotFoundException

import scala.concurrent.{ExecutionContext, Future}

class TodoController @Inject()(cc: ControllerComponents,
                               todoService: TodoService) extends AbstractController(cc) {

  implicit val ec: ExecutionContext = cc.executionContext

  def getTodos: Action[AnyContent] = Action.async { implicit request =>
    todoService.getAllTodosWithComments()
      .map(list => Ok(Json.prettyPrint(Json.toJson(list))))
      .recover {
        case e => InternalServerError(getResult(e.getMessage))
      }
  }

  def createTodo: Action[JsValue] = Action.async(parse.json) { request =>
    val todoAsOpt = parseTodoMessage(request)
    todoAsOpt match {
      case Some(todo) => todoService.insertTodo(Todo(todo = todo))
        .map(i => Created(getResult(s"Todo is inserted - id: $i")))
        .recover {
          case e => InternalServerError(getResult(e.getMessage))
        }
      case _ => Future.successful(BadRequest(getResult("Json is not valid!")))
    }
  }

  def editTodo(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    val todoEditAsOpt = parseTodoEdit(request)

    todoEditAsOpt match {
      case Some(todoEdit) =>
        todoService.updateTodo(id, todoEdit)
          .map(i => getUpdateMessage(i, id))
          .recover {
            case ex: TodoNotFoundException => NotFound(getResult(s"Related todo could not be found with id: $id"))
            case e => InternalServerError(getResult(e.getMessage))
          }
      case None => Future.successful(BadRequest(getResult("Json is not valid!")))
    }
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async { implicit request =>
    todoService.deleteTodo(id)
      .map(i => Ok(getResult(s"$i Todo is deleted")))
      .recover {
        case ex: TodoNotFoundException => NotFound(getResult(s"Related todo could not be found with id: $id"))
        case e => InternalServerError(getResult(e.getMessage))
      }
  }

  private def getUpdateMessage(updatedRowCount: Int, id: String): Result = {
    updatedRowCount match {
      case i if i >= 1 => Ok(getResult(s"$i Todo is edited - id: $id"))
      case 0 => Ok(getResult(s"Nothing is edited. Related todo could not be found or there is no change!"))
      case _ => Ok
    }
  }

  private def parseTodoMessage(request: Request[JsValue]) = {
    (request.body \ "todo").asOpt[String]
  }

  private def parseTodoEdit(request:  Request[JsValue]) = {
    val todoAsOpt = (request.body \ "todo").asOpt[String]
    val isDoneAsOpt = (request.body \ "isDone").asOpt[Boolean]
    (todoAsOpt, isDoneAsOpt) match {
      case (None, None) => None
      case _ => Some(TodoEdit(todoAsOpt, isDoneAsOpt))
    }
  }
}
