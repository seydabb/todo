package service

import java.sql.Connection
import javax.inject.Inject

import play.api.db.DBApi
import repository.{Todo, TodoEdit, TodosRepository}

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class TodoNotFoundException(message: String) extends Exception

class TodoService @Inject()(todosRepository: TodosRepository,
                            dBApi: DBApi) {

  implicit val connection: Connection = dBApi.database("default").getConnection()

  def insertTodo(todo: Todo): Future[String] = {
    todosRepository.insertTodos(todo)
  }

  def deleteTodo(id: String): Future[Int] = {
    todosRepository.delete(id).map {
      case 1 => 1
      case 0 => throw TodoNotFoundException("Todo not found!")
      case _ => throw new Exception("Unknown problem is occurred!")
    }
  }

  def updateTodo(id: String, todoEdit: TodoEdit): Future[Int] = {
    todosRepository.update(id, todoEdit).map {
      case 1 => 1
      case 0 => throw TodoNotFoundException("Todo not found!")
      case _ => throw new Exception("Unknown problem is occurred!")
    }
  }
}
