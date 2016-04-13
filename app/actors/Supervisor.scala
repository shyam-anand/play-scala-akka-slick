package actors

import javax.inject.{Inject, Named}

import actors.Supervisor.{LoggingFailed, WSResponse, SendMessage}
import akka.actor._
import dao.{WSLogDAO, UserDAO}
import models.{WSLog, User}
import play.api.Logger
import play.api.libs.concurrent.InjectedActorSupport
import play.api.libs.json._

import scala.concurrent.Await
import scala.concurrent.duration._

/**
  * Created by shyam on 16/03/16.
  */
object Supervisor {

  var handledMessages: Int = 0

  case class SendMessage(message: String)
  case class WSResponse(response: String, message: String, user: User, attempt: Int)
  case class Status(status: Boolean)
  case object LoggingFailed
}

class Supervisor @Inject()(
                            @Named("ws-actor") wsActor: ActorRef,
                            @Named("ws-logger") wsLogger: ActorRef,
                            userDAO: UserDAO,
                            wsLogDao: WSLogDAO
                              ) extends Actor with InjectedActorSupport {


  def receive = {
    case SendMessage(message) =>

      Supervisor.handledMessages += 1

      var count = 0;

      val userList = Await.result( userDAO.listAll, 3 seconds )
      val userListCount = userList.count(_ => true)

      Logger.debug(s"Sending to $userListCount users")

      userList foreach { user =>
        wsActor ! WSActor.Send(message, user)
        count += 1
      }

      Logger.info(s"Done. $count users")
      sender ! s"Sent to $count users"

    case WSResponse(response, message, user, attempt) =>
      Logger.info(s"Response: ${sender.path.name} - $response - ${user.name} - ${user.id} - attempt $attempt")

      val gcmJson: JsValue = Json.parse(response)
      val responseStatus = (gcmJson \ "result").as[String].toUpperCase
      val refId = (gcmJson \ "ref").as[String]

      if ( responseStatus.equalsIgnoreCase("FAILURE") && attempt < 3) {
        wsActor ! WSActor.Send(message, user, attempt + 1)
      }

      wsLogger ! WSLogger.LogRow(message, user.id, responseStatus, refId, attempt)


    case LoggingFailed =>
      Logger.info("Logging failed")

    case Terminated(actor) =>
      Logger.warn(s"${actor.path.name} terminated")

  }

  override def unhandled(message: Any): Unit = {
    Logger.warn(s"Received message of unhandled type ${message.getClass}")
  }

}
