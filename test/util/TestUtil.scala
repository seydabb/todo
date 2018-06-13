package util

import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.play.PlaySpec
import org.scalatest.mockito.MockitoSugar

trait TestUtil extends PlaySpec with MockitoSugar with ScalaFutures with BeforeAndAfter {

}
