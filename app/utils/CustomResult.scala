package utils

import play.api.libs.json.{Format, JsValue, Json}

case class CustomResult(message: String)

object CustomResult {
  implicit val format: Format[CustomResult] = Json.format[CustomResult]

  def getResult(message: String): JsValue = {
    Json.toJson(CustomResult(message))
  }
}
