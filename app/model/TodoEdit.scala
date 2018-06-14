package model

import play.api.libs.json._

case class TodoEdit(todo: Option[String], isDone: Option[Boolean])

object TodoEdit {
  implicit val format: Format[TodoEdit] = Json.format[TodoEdit]
}
