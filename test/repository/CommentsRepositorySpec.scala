package repository

import anorm.SQL
import anorm.SqlParser.str
import play.api.db.DBApi
import util.TestData._
import util.{DBConnection, TestUtil}
import org.scalatest.Matchers._
import anorm.JodaParameterMetaData._

import scala.concurrent.ExecutionContext.Implicits

class CommentsRepositorySpec extends TestUtil {
  implicit val ec = Implicits.global

  var dbApiMock: DBApi = _
  var todosRepositoryMock: TodosRepository = _
  var commentsRepository: CommentsRepository = _

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

      SQL(
        """
          CREATE TABLE IF NOT EXISTS test_comments
          |(
          |  id CHARACTER VARYING(96) NOT NULL,
          |  fkTodoId CHARACTER VARYING(96) NOT NULL,
          |  comment CHARACTER VARYING(500) NOT NULL,
          |  createdAt TIMESTAMP WITH TIME ZONE NOT NULL,
          |  updatedAt TIMESTAMP WITH TIME ZONE NOT NULL,
          |  CONSTRAINT test_comments_pkey PRIMARY KEY (ID),
          |  CONSTRAINT test_todo_fk_constraint FOREIGN KEY (fkTodoId) REFERENCES test_todos (id)
          |)""".stripMargin
      ).execute


    }

    dbApiMock = mock[DBApi]
    todosRepositoryMock = mock[TodosRepository]
    commentsRepository = new CommentsRepository(dbApiMock, todosRepositoryMock)(ec) {
      override val tableName: String = "test_comments"
    }
  }

  after {
    DBConnection.withConnection { implicit conn =>
      SQL("drop table test_comments").execute
      SQL("drop table test_todos").execute
    }
  }


  "insertComment" should {
    "insert comment as a new one if it is not exist yet with todo foreign key" in {
      DBConnection.withConnection { implicit conn =>
        //because of foreign key constraint, firstly a record needs to be added to todos table
        val id = SQL(s"insert into test_todos(id, todo, isDone, createdAt, updatedAt) values ({id}, {todo}, {isDone}, {createdAt}, {updatedAt})")
          .on('id -> ANY_TODOS_ID, 'todo -> ANY_TODO.todo, 'isDone -> false, 'createdAt -> ANY_TODO.createdAt, 'updatedAt -> ANY_TODO.updatedAt)
          .executeInsert(str(1).+)


        val result = commentsRepository.insertComment(id.head, ANY_COMMENT.comment).futureValue
        result must not be empty
      }
    }
  }
}
