package actors

import javax.inject.Inject

import akka.actor.Actor
import dao.WSLogDAO
import models.WSLog
import play.api.{Logger, Configuration}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

/**
  * Created by shyam on 04/04/16.
  */
object WSLogger {
  case class LogRow(message: String, userId: String, responseStatus: String, refId: String, attempt: Int)
}

class WSLogger @Inject()(wsLogDAO: WSLogDAO, conf: Configuration)
                        (implicit val exContext: ExecutionContext) extends Actor {
  import actors.WSLogger.LogRow

  override def receive: Receive = {

    case LogRow(message, userId, responseStatus, refId, attempt) =>

      val wsLog = WSLog(0, message, userId, responseStatus, refId, attempt, null)

      wsLogDAO.save(wsLog) match {
        case Success(log) => Logger.debug(s"Insert successful - [${log.id}] ${log.message} ${log.refId} ${log.response_status}")
        case Failure(t: Exception) => Logger.warn(s"Insert failed: ${t.getMessage} [${t.getCause}]")
        case Failure(_) => Logger.warn("Insert failed for unknown reason")
      }
  }

}
