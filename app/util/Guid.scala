package util

import java.util.UUID

object Guid {
  def generateUuid: String = {
    UUID.randomUUID().toString
  }
}
