package controllers

import javax.inject._

import play.api.libs.json._
import play.api.mvc._
import repository.{Todo, TodoEdit}
import service.{TodoNotFoundException, TodoService}
import utils.CustomResult._

import scala.concurrent.{ExecutionContext, Future}


@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               todoService: TodoService) extends AbstractController(cc) {

  implicit val ec: ExecutionContext = cc.executionContext

  def index = Action {
    Ok(views.html.index("Hello world!"))
  }

  def createTodo: Action[JsValue] = Action.async(parse.json) { request =>
    val todoAsOpt = readTodoMessage(request)
    todoAsOpt match {
      case Some(todo) => todoService.insertTodo(Todo(todo = todo))
        .map(i => Ok(getResult(s"Todo is inserted - id: $i")))
        .recover {
        case ex => InternalServerError(getResult("bok"))
      }
      case _ =>  Future.successful(BadRequest(getResult("Json is not valid!")))
    }
  }

  def editTodo(id: String): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[TodoEdit] match {
      case todoEdit: JsSuccess[TodoEdit] => {
        todoService.updateTodo(id, todoEdit.get)
          .map(i => Ok(getResult(s"Todo is edited - id: $i")))
          .recover {
            case ex: TodoNotFoundException => NotFound(getResult(s"Related todo could not be found with id: $id"))
            case e => InternalServerError(getResult(e.getMessage))
          }
      }
      case _: JsError => Future.successful(BadRequest)
    }
  }

  def deleteTodo(id: String): Action[AnyContent] = Action.async { request =>
    todoService.deleteTodo(id) //TODO delete also it's comments
      .map(i => Ok(getResult(s"$i Todo is deleted")))
      .recover {
        case ex: TodoNotFoundException => NotFound(getResult(s"Related todo could not be found with id: $id"))
        case e => InternalServerError(getResult(e.getMessage))
      }
  }

  private def readTodoMessage(request: Request[JsValue]) = {
    (request.body \ "todo").asOpt[String]
  }

}
