package service

import java.sql.Connection
import javax.inject.Inject

import play.api.db.DBApi
import repository.{Todo, TodosRepository}

import scala.concurrent.Future

class TodoService @Inject() (todosRepository: TodosRepository,
                             dBApi: DBApi) {

  implicit val connection: Connection = dBApi.database("default").getConnection()

  def getSingleTodo(id: String): Future[Option[Todo]] = {
    todosRepository.findById(id)
  }

  def insertTodo(todo: Todo): Future[Int] = {
    todosRepository.insertTodos(todo)
  }

  def deleteTodo(id: String): Future[Int] = {
    todosRepository.delete(id)
  }

  def updateTodo(id: String, newTodo: Todo): Future[Int] = {
    todosRepository.update(id, newTodo)
  }

}
