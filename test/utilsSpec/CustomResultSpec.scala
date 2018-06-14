package utilsSpec

import play.api.libs.json.Json
import util.TestUtil
import utils.CustomResult

class CustomResultSpec extends TestUtil {

  val customResultJsonStr: String =
    """{
      |  "message": "any message"
      |}""".stripMargin

  val customResult = CustomResult("any message")

  "writes" should {
    "create a correct json for CustomResult with Json.toJson method" in {
      Json.parse(customResultJsonStr) must be(Json.toJson(customResult))
    }
  }

  "reads" should {
    "create a correct object for CustomResult from a json" in {
      val expectedTodoEditObject = Json.parse(customResultJsonStr).as[CustomResult]

      expectedTodoEditObject must be(customResult)
    }
  }
}
