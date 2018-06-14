package model

import play.api.libs.json.Json
import util.TestUtil

class TodoEditSpec extends TestUtil {

  val todoEditJsonStr: String =
    """{
      |  "todo": "any todo",
      |  "isDone": false
      |}""".stripMargin

  val todoEditJsonStrWithOnlyTodo: String =
    """{
      |  "todo": "any todo"
      |}""".stripMargin

  val todoEditJsonStrWithOnlyIsDone: String =
    """{
      |  "isDone": false
      |}""".stripMargin


  val todoEdit = TodoEdit(todo = Some("any todo"), isDone = Some(false))

  "writes" should {
    "create a correct json for TodoEdit with Json.toJson method" in {
      Json.parse(todoEditJsonStr) must be(Json.toJson(todoEdit))
    }

    "create a correct json for TodoEdit without isDone field with Json.toJson method" in {
      Json.parse(todoEditJsonStrWithOnlyTodo) must be(Json.toJson(todoEdit.copy(isDone = None)))
    }

    "create a correct json for TodoEdit without todo field with Json.toJson method" in {
      Json.parse(todoEditJsonStrWithOnlyIsDone) must be(Json.toJson(todoEdit.copy(todo = None)))
    }
  }

  "reads" should {
    "create a correct object for TodoEdit from a json" in {
      val expectedTodoEditObject = Json.parse(todoEditJsonStr).as[TodoEdit]

      expectedTodoEditObject must be(todoEdit)
    }

    "create a correct object for TodoEdit without todo field from a json" in {
      val expectedTodoEditObject = Json.parse(todoEditJsonStrWithOnlyIsDone).as[TodoEdit]

      expectedTodoEditObject must be(todoEdit.copy(todo = None))
    }
  }
}
