package implementation

import org.scalatest.concurrent.Eventually
import org.scalatest.prop.PropertyChecks
import org.scalatest.time.{ Second, Seconds, Span }
import org.scalatest.{ FlatSpec, Matchers }

abstract class DBIOSpec extends FlatSpec with Matchers with PropertyChecks with Eventually {
  implicit override def patienceConfig: PatienceConfig = PatienceConfig(Span(10, Seconds), Span(1, Second))
}
