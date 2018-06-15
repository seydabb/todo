package model

import play.api.libs.json.{Format, Json}
import repository.{Comment, Todo}

case class TodosWithComments(todo: Todo, comments: List[Comment])

object TodosWithComments {
  implicit val format: Format[TodosWithComments] = Json.format[TodosWithComments]

}
