package boilerplate

import java.util.UUID

import akka.http.scaladsl.unmarshalling.Unmarshaller

import scala.util.Try

object AkkaHttp {
  implicit val uuidFromStringUnmarshaller: Unmarshaller[String, UUID] =
    Unmarshaller.strict[String, UUID] { string ⇒
      Try(UUID.fromString(string)).toOption match {
        case Some(uuid) => uuid
        case None       => throw new IllegalArgumentException(s"unable to parse '$string' as UUID")
      }
    }
}
