package utils

case class TodoNotFoundException(message: String) extends Exception
case class CommentCouldNotBeAddedException(message: String) extends Exception
