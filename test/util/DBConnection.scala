package util

import java.sql.{Connection, DriverManager}

object DBConnection {

  private val url = "jdbc:postgresql://localhost:5432/postgres"
  private val user = "postgres"
  private val password = "kjiY&*ui3rUY"

  private val driver = Class.forName("org.postgresql.Driver")

  def withConnection[T](block: Connection => T): T = {
    val connection = DriverManager.getConnection(url, user, password)
    try {
      block(connection)
    } finally {
      connection.close()
    }
  }

  //withTransaction method is just used for testing purposes
  //Resource: https://gist.github.com/bmc/3783883#file-dbutil-scala
  def withTransaction[T](code: java.sql.Connection => Either[String,T]): Either[String, T] = {
    val connection = DriverManager.getConnection(url, user, password)
    val autoCommit = connection.getAutoCommit

    try {
      connection.setAutoCommit(false)
      var result = code(connection)
      result.fold(
        { error  => throw new Exception(error) },
        { _      => connection.commit() }
      )
      result
    }
    catch {
      case e: Throwable =>
        connection.rollback()
        Left("Error, rolling back transaction")
    }
    finally {
      connection.setAutoCommit(autoCommit)
    }
  }

}
