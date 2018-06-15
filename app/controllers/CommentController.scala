package controllers

import javax.inject.Inject

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents, Request}
import service.CommentService
import utils.CommentCouldNotBeAddedException
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
        .map(i => Created(getResult(Right(Json.obj("id" -> i)))))
        .recover {
          case ex: CommentCouldNotBeAddedException => BadRequest(getResult(Left("Related Todo could not be found.")))
          case e => InternalServerError(getResult(Left(e.getMessage)))
        }
      case None => Future.successful(BadRequest(getResult(Left("Json is not valid!"))))
    }
  }

  private def readTodoMessage(request: Request[JsValue]) = {
    (request.body \ "comment").asOpt[String]
  }

}
