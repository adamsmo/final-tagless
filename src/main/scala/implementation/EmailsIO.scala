package implementation

import cats.effect.IO
import domain.EmailType
import interfaces.Emails

import scala.concurrent.ExecutionContext

class EmailsIO(implicit ec: ExecutionContext) extends Emails[IO] {
  override def sendEmail(user: String, email: EmailType): IO[EmailType] =
    IO {
      //todo instead of printing email call a service?
      println(s"sending $email to $email")
      email
    }
}