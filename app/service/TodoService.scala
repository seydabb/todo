package service

import java.sql.Connection
import javax.inject.Inject

import model.{TodoEdit, TodosWithComments}
import play.api.db.DBApi
import repository.{Todo, TodosRepository}
import utils.TodoNotFoundException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class TodoService @Inject()(todosRepository: TodosRepository,
                            dBApi: DBApi) {

  implicit val connection: Connection = dBApi.database("default").getConnection()

  def insertTodo(todo: Todo): Future[String] = {
    todosRepository.insertTodos(todo)
  }

  def deleteTodo(id: String): Future[Int] = {
    Future {
      todosRepository.delete(id)
    }.map {
      case Left(_) => throw new Exception("Delete operation could not be completed!")
      case Right(resultOfTransactionalDelete) => if(!resultOfTransactionalDelete) throw TodoNotFoundException("Related todo could not be found!") else 1
    }
  }

  def updateTodo(id: String, todoEdit: TodoEdit): Future[Int] = {
    val updateResult = for {
      maybeTodo <- todosRepository.findById(id)
      newTodo <- createNewTodo(maybeTodo, todoEdit)
      result <- updateIfTodoIsChanged(newTodo)
    } yield result

    updateResult.map {
      case 1 => 1
      case -1 => 0 //Nothing is changed, so update process is not happened!
      case 0 => throw TodoNotFoundException("Todo not found!")
      case _ => throw new Exception("Unknown problem is occurred!")
    }
  }

  def getAllTodosWithComments(): Future[List[TodosWithComments]] = {
    todosRepository.getAllTodosWithComments()
  }

  private def createNewTodo(maybeOldTodo: Option[Todo], todoEdit: TodoEdit): Future[Option[Todo]] = Future {
   if (maybeOldTodo.isDefined) {
      val oldTodo = maybeOldTodo.get
      val newTodo = todoEdit match {
        case TodoEdit(Some(todoMsg), Some(isDone)) => Some(oldTodo.copy(todo = todoMsg, isDone = isDone))
        case TodoEdit(Some(todoMsg), None) => Some(oldTodo.copy(todo = todoMsg))
        case TodoEdit(None, Some(isDone)) => Some(oldTodo.copy(isDone = isDone))
        case _ => None //Nothing is changed
      }
      newTodo
    } else None
  }

  private def updateIfTodoIsChanged(newTodoAsOpt: Option[Todo]): Future[Int] = {
    newTodoAsOpt match {
      case Some(newTodo) => todosRepository.update(newTodo)
      case None => Future.successful(-1) //Nothing is changed!
    }
  }
}
