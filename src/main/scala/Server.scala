import java.util.UUID

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.stream.ActorMaterializer
import boilerplate.AkkaHttp._
import boilerplate._
import cats.effect.IO
import domain.{ DomainError, EmailType, NoCustomer, NoProduct }
import implementation._
import logic.{ ApplicationApi, CustomerEmails, Purchases }
import org.slf4j.LoggerFactory
import slick.jdbc.H2Profile.api._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContextExecutor, Future }
import scala.util.Try

object Server extends App {
  //for akka http
  implicit val system: ActorSystem = ActorSystem()
  implicit val dispatcher: ExecutionContextExecutor = system.dispatcher
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  //for app logic
  val log = LoggerFactory.getLogger(getClass)
  implicit val database: DB = new DB()

  //setup h2 db
  val db = database.db
  Await.ready(database.setUpDB(), 10.seconds)

  implicit val customerLogic: CustomersDBIO = new CustomersDBIO()
  implicit val productLogic: ProductsDBIO = new ProductsDBIO()
  implicit val emailLogic: EmailsIO = new EmailsIO()
  implicit val monadDBIO: MonadDBIO = new MonadDBIO()

  implicit val emails: CustomerEmails[IO] = new CustomerEmails[IO]
  implicit val purchase: Purchases[DBIO] = new Purchases[DBIO]

  implicit val dbioToIo: DBIOtoIO = new DBIOtoIO()
  implicit val ioToIo: IOtoIO = new IOtoIO()

  val api = new ApplicationApi[DBIO, IO, IO]()

  val routes =
    Route.seal {
      respondWithHeader(`Content-Type`(`text/plain(UTF-8)`)) {
        authenticateBasic(realm = "shop", shopAuthenticator) { authenticatedCustomer =>
          path("customer" / JavaUUID / "purchase") { customerId =>
            authorize(authenticatedCustomer == customerId) {
              post {
                formFields('productId.as[UUID]) { productId =>
                  val result: IO[Either[Seq[DomainError], Seq[EmailType]]] = api.makePurchase(customerId, productId)

                  onSuccess(result.unsafeToFuture()) {
                    case Right(_) =>
                      complete(StatusCodes.OK -> "OK")

                    case Left(errors) if errors.contains(NoCustomer) =>
                      complete(StatusCodes.NotFound -> "no customer")

                    case Left(errors) if errors.contains(NoProduct) =>
                      complete(StatusCodes.BadRequest -> "no product")
                  }
                }
              }
            }
          } ~ path("products" / Segment) { name =>
            get {
              val result: IO[Either[NoCustomer.type, Seq[domain.Product]]] = api.findProduct(authenticatedCustomer, name)
              onSuccess(result.unsafeToFuture()) {
                case Right(products) =>
                  //todo encode to json
                  complete(StatusCodes.OK -> products.toString)
                case Left(NoCustomer) =>
                  complete(StatusCodes.Unauthorized -> "")
              }
            }
          }
        }
      }
    }

  def shopAuthenticator(credentials: Credentials): Option[UUID] =
    credentials match {
      case c @ Credentials.Provided(id) if c.verify("1234") => Try(UUID.fromString(id)).toOption
      case _ => None
    }

  val binding: Future[Http.ServerBinding] =
    Http().bindAndHandle(routes, "127.0.0.1", 8080)

  log.info("starting server on 127.0.0.1:8080")

  Runtime.getRuntime.addShutdownHook(new Thread() {
    override def run(): Unit = {
      binding
        .map(_.terminate(5.seconds))
        .flatMap(_ => system.terminate())
    }
  })

}