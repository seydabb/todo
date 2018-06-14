package controllers

import javax.inject.Inject

import play.api.libs.json.JsValue
import play.api.mvc.{AbstractController, Action, ControllerComponents, Request}
import service.CommentService
import utils.CustomResult.getResult

import scala.concurrent.{ExecutionContext, Future}

class CommentController @Inject()(cc: ControllerComponents,
                                  commentService: CommentService) extends AbstractController(cc) {

  implicit val ec: ExecutionContext = cc.executionContext

  def addComment(todoId: String): Action[JsValue] = Action.async(parse.json) { request =>
    val commentAsOpt = readTodoMessage(request)
    commentAsOpt match {
      case Some(comment) =>
        commentService.insertComment(todoId, comment)
        .map(i => Created(getResult(s"Comment is inserted - id: $i")))
        .recover {
          case e => InternalServerError(getResult(e.getMessage))
        }
      case None => Future.successful(BadRequest(getResult("Json is not valid!")))
    }
  }

  private def readTodoMessage(request: Request[JsValue]) = {
    (request.body \ "comment").asOpt[String]
  }

}
