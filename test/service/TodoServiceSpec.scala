package service

import java.sql.Connection

import org.mockito.Mockito.{verify, verifyNoMoreInteractions, when}
import play.api.db.{DBApi, Database}
import repository.TodosRepository
import util.TestData._
import util.{DBConnection, TestUtil}

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future

class TodoServiceSpec extends TestUtil {

  private implicit val ec = Implicits.global

  private var sut: TodoService = _
  private var dbApiMock: DBApi = _
  private var databaseMock: Database = _
  private var connectionMock: Connection = _
  private var todosRepositoryMock: TodosRepository = _

  before {
    todosRepositoryMock = mock[TodosRepository]
    dbApiMock = mock[DBApi]
    databaseMock = mock[Database]
  }

  "getSingleTodo" should {
    "calls findById method of repository and returns related todo from db" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.findById(ANY_TODOS_ID)).thenReturn(Future.successful(Some(ANY_TODO)))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.getSingleTodo(ANY_TODOS_ID).futureValue

        result.isDefined mustBe true

        verify(todosRepositoryMock).findById(ANY_TODOS_ID)(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.findById(ANY_TODOS_ID)).thenReturn(Future.failed(new Exception))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.getSingleTodo(ANY_TODOS_ID).futureValue
        }
      }
    }
  }

  "insertTodo" should {
    "calls insertTodos method of repository and insert todo & returns inserted row count" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.insertTodos(ANY_TODO)).thenReturn(Future.successful(1))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.insertTodo(ANY_TODO).futureValue

        result mustBe 1

        verify(todosRepositoryMock).insertTodos(ANY_TODO)(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.insertTodos(ANY_TODO)).thenReturn(Future.failed(new Exception))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.insertTodo(ANY_TODO).futureValue
        }
      }
    }
  }

  "deleteTodo" should {
    "calls delete method of repository and deletes the related todo & return deleted row count" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Future.successful(1))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.deleteTodo(ANY_TODOS_ID).futureValue

        result mustBe 1

        verify(todosRepositoryMock).delete(ANY_TODOS_ID)(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Future.failed(new Exception))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.deleteTodo(ANY_TODOS_ID).futureValue
        }
      }
    }
  }

  "updateTodo" should {
    "calls update method of repository and updates given todo object & returns updated row count" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.update(ANY_TODOS_ID, ANY_TODO)).thenReturn(Future.successful(1))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.updateTodo(ANY_TODOS_ID, ANY_TODO).futureValue

        result mustBe 1

        verify(todosRepositoryMock).update(ANY_TODOS_ID, ANY_TODO)(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Future.failed(new Exception))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.deleteTodo(ANY_TODOS_ID).futureValue
        }
      }
    }
  }
}
