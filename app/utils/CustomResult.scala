package utils

import play.api.libs.json.{Format, JsValue, Json}

case class CustomResult(message: String)

object CustomResult {
  implicit val format: Format[CustomResult] = Json.format[CustomResult]

  def getResult(messageOrResult: Either[String, JsValue]): JsValue = {
    messageOrResult match {
      case Left(message) => Json.toJson(CustomResult(message))
      case Right(result) => result
    }

  }
}
