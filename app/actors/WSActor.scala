/*
   Copyright 2016 Shyam Anand

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package actors

import javax.inject.Inject

import actors.Supervisor.WSResponse
import akka.actor.Actor
import akka.util.Timeout
import models.User
import play.api.libs.ws.WSClient
import play.api.{Configuration, Logger}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
/**
  * Created by shyam on 16/03/16.
  */

object WSActor {

  case class Send(message: String, user: User, attempt:Int = 0)
  case object GetStatus

  trait Factory {
    def apply(actorName: String): Actor
  }
}

class WSActor @Inject()(ws: WSClient, conf: Configuration)
                       (implicit val exContext: ExecutionContext) extends Actor {
  import WSActor.{GetStatus, Send}
  import Supervisor.Status

  val WS_URL = conf.getString("GCM_URL").get
  var isDone = false

  override def receive: Receive = {

    case Send(message, user, attempt) =>

      Logger.debug(s"${self.path.name} - Received message '$message', attempt $attempt")

      sender ! WSResponse(wsCall(message, user.id, user.name, user.gcmId), message, user, attempt)
      isDone = true

    case GetStatus => sender ! Status(isDone)
  }

  def wsCall(message: String, userId: String, username: String, uid: String): String = {

    Logger.info(s"${self.path.name} - $username ($userId) - $uid")

    implicit val timeout = Timeout(6 seconds)

    val queryString = Map(
      "uid" -> uid,
      "name" -> username
    )

    val wsResponse = ws.url(WS_URL).withQueryString(queryString.toList: _*).withFollowRedirects(true).get()

    val response = Await.result(wsResponse, timeout.duration)

    Logger.debug(s"Response from GCM: ${response.body}")

    response.body
  }

  @throws[Exception](classOf[Exception])
  override def preStart(): Unit = {
    Logger.debug( s"pre-start ${self.path.name}" )
  }

  @throws[Exception](classOf[Exception])
  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    Logger.debug(s"pre-restart ${self.path.name}, reason ${reason.getMessage}")
  }
}
