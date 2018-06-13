package repository

import anorm.SQL
import play.api.db.DBApi

import scala.concurrent.ExecutionContext.Implicits
import util.{DBConnection, TestUtil}
import util.TestData._

class TodosRepositorySpec extends TestUtil {

  implicit val ec = Implicits.global

  var dbApiMock: DBApi = _
  var todosRepository: TodosRepository = _

  before {
    DBConnection.withConnection { implicit conn =>
      SQL(
        """
          CREATE TABLE IF NOT EXISTS test_todos
          |(
          |  id CHARACTER VARYING(96) NOT NULL,
          |  todo CHARACTER VARYING(500) NOT NULL,
          |  isDone BOOLEAN NOT NULL,
          |  createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
          |  updatedAt TIMESTAMP WITH TIME ZONE NOT NULL,
          |  CONSTRAINT test_todos_pkey PRIMARY KEY (id)
          |)""".stripMargin
      ).execute
    }

    dbApiMock = mock[DBApi]
    todosRepository = new TodosRepository(dbApiMock)(ec) {
      override val tableName: String = "test_todos"
    }
  }

  after {
    DBConnection.withConnection { implicit conn =>
      SQL("drop table test_todos").execute
    }
  }

  "insertTodos" should {
    "insert todos as a new one if it is not exist yet" in {
      DBConnection.withConnection { implicit conn =>
        val result = todosRepository.insertTodos(ANY_TODO).futureValue
        result mustBe ANY_TODO.id
      }
    }
  }

  "delete" should {
    "remove the existing todo from todos table" in {
      DBConnection.withConnection { implicit conn =>
        val inserted = todosRepository.insertTodos(ANY_TODO_WITH_HARDCODED_ID).futureValue
        inserted mustBe ANY_TODO_WITH_HARDCODED_ID.id
        val deleted = todosRepository.delete(ANY_TODO_WITH_HARDCODED_ID.id).futureValue
        deleted mustBe 1
      }
    }

    "return 0 if there is no todo for given todo-id" in {
      DBConnection.withConnection { implicit conn =>
        val deleted = todosRepository.delete("non-existing-id").futureValue
        deleted mustBe 0
      }
    }
  }

  "update" should {
    "updates the existing todo's fields" in {
      DBConnection.withConnection { implicit conn =>
        val inserted = todosRepository.insertTodos(ANY_TODO_WITH_HARDCODED_ID).futureValue
        inserted mustBe ANY_TODO_WITH_HARDCODED_ID.id
        val updated = todosRepository.update(ANY_TODO_WITH_HARDCODED_ID.id, ANY_TODO_EDIT).futureValue
        updated mustBe 1
      }
    }

    "return 0 if some problem occurs with update statement" in {
      DBConnection.withConnection { implicit conn =>
        val updated = todosRepository.update("non-existing", ANY_TODO_EDIT).futureValue
        updated mustBe 0
      }
    }
  }

}
