package utils

import java.util.UUID

object Guid {
  def generateUuid: String = {
    UUID.randomUUID().toString
  }
}
