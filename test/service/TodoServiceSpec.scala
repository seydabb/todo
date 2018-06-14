package service

import java.sql.Connection

import org.mockito.Mockito.{verify, verifyNoMoreInteractions, verifyZeroInteractions, when}
import play.api.db.{DBApi, Database}
import repository.{Todo, TodosRepository}
import util.{DBConnection, TestUtil}
import util.TestData._

import scala.concurrent.ExecutionContext.Implicits
import scala.concurrent.Future
import org.mockito.ArgumentMatchers.any

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

  "insertTodo" should {
    "calls insertTodos method of repository and insert todo & returns id" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.insertTodos(ANY_TODO)).thenReturn(Future.successful(ANY_TODO.id))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.insertTodo(ANY_TODO).futureValue

        result mustBe ANY_TODO.id

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
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Right(true))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.deleteTodo(ANY_TODOS_ID).futureValue

        result mustBe 1

        verify(todosRepositoryMock).delete(ANY_TODOS_ID)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "does not delete todo & comment if todo deletion did not occur 'Right(false) case" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Right(false))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.deleteTodo(ANY_TODOS_ID).futureValue
        }
      }
    }

    "throws exception if future fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.delete(ANY_TODOS_ID)).thenReturn(Left("Problem occurred!"))

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
        when(todosRepositoryMock.findById(any[String])(any[Connection])).thenReturn(Future.successful(Some(ANY_TODO_WILL_BE_EDITED)))
        when(todosRepositoryMock.update(any[Todo])(any[Connection])).thenReturn(Future.successful(1))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.updateTodo(ANY_TODOS_ID, ANY_TODO_EDIT).futureValue

        result mustBe 1

        verify(todosRepositoryMock).findById(ANY_TODO_WILL_BE_EDITED.id)(conn)
        verify(todosRepositoryMock).update(ANY_TODO_WILL_BE_EDITED)(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "does not update if findById returns with None (there is no related todo with id)" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.findById(any[String])(any[Connection])).thenReturn(Future.successful(None))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.updateTodo(ANY_TODOS_ID, ANY_TODO_EDIT).futureValue

        result mustBe 0

        verify(todosRepositoryMock).findById(ANY_TODO_WILL_BE_EDITED.id)(conn)
        verifyZeroInteractions(todosRepositoryMock)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }

    "throws exception if repository.update fails" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.update(any[Todo])(any[Connection])).thenReturn(Future.failed(new Exception))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        intercept[Exception] {
          sut.updateTodo(ANY_TODO.id, ANY_TODO_EDIT).futureValue
        }
      }
    }
  }

  "getAllTodosWithComments" should {
    "calls getAllTodosWithComments method of repository and returns all todos with it's comments" in {
      DBConnection.withConnection { implicit conn =>

        when(dbApiMock.database("default")).thenReturn(databaseMock)
        when(databaseMock.getConnection).thenReturn(conn)
        when(todosRepositoryMock.getAllTodosWithComments()).thenReturn(Future.successful(TODOS_WITH_COMMENTS))

        sut = new TodoService(todosRepositoryMock, dbApiMock)

        val result = sut.getAllTodosWithComments().futureValue

        result.size mustBe 1

        verify(todosRepositoryMock).getAllTodosWithComments()(conn)
        verifyNoMoreInteractions(todosRepositoryMock)
      }
    }
  }
}
