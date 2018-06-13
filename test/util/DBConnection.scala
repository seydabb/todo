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
}
