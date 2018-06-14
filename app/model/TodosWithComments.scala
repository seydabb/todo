package model

import play.api.libs.json.{Format, Json}
import repository.Comment

case class TodosWithComments(id: String, todo: String, comments: List[Comment])

object TodosWithComments {
  implicit val format: Format[TodosWithComments] = Json.format[TodosWithComments]

}
