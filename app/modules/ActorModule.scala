package modules

import actors.{WSLogger, WSActor, Supervisor}
import akka.routing.BalancingPool
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * Created by shyam on 16/03/16.
  */
class ActorModule extends AbstractModule with AkkaGuiceSupport {
  override def configure(): Unit = {
    bindActor[Supervisor]("supervisor")
    bindActor[WSActor]("ws-actor", BalancingPool(5).props)
    bindActor[WSLogger]("ws-logger")
  }
}
