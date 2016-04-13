package controllers

import javax.inject.{Inject, Named, Singleton}

import actors.Supervisor.SendMessage
import akka.actor.ActorRef
import akka.pattern.ask
import akka.util.Timeout
import models.Response
import play.api.Logger
import play.api.libs.json.{Json, JsPath, Writes}
import play.api.libs.functional.syntax._
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class WSController @Inject()(@Named("supervisor") supervisor: ActorRef)
                            (implicit context: ExecutionContext) extends Controller {

  val logger: Logger = Logger(this.getClass)

  def send = Action.async { request =>

    val msg = request.getQueryString("m").getOrElse("EMPTY")
    Logger.debug(s"Request: $msg")

    implicit val responseWrites: Writes[Response] = (
        (JsPath \ "request").write[String] and
        (JsPath \ "response").write[String]
      )(unlift(Response.unapply))

    implicit val timeout: Timeout = 3.seconds

    (supervisor ? SendMessage(msg)).mapTo[String].map { reply =>
      val jsonResponse = Json.toJson(Response(msg, reply))
      Ok(jsonResponse)
    }
  }
}
