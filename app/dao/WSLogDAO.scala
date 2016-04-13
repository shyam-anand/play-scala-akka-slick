package dao

import java.sql.Timestamp
import javax.inject.Inject

import models.WSLog
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile
import slick.profile.SqlProfile.ColumnOption.SqlType

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.util.Try

/**
  * Created by shyam on 30/03/16.
  */
class WSLogDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import slick.driver.MySQLDriver.api._

  import scala.concurrent.ExecutionContext.Implicits.global

  private val Log = TableQuery[GCMLogTable]

  def log(log: WSLog): Future[Unit] = {
    db.run(Log += log).map{ _ => () }
  }

  /**
    * Insert log data and returns log row with auto increment id
    *
    * @param log GCMLog
    * @return Success[GCMLog] or Failure
    */
  def save(log: WSLog): Try[WSLog] = Try {
    val x = (Log returning Log.map(_.id)) into ((log, newId) => log.copy(id = newId)) += log
    Await.result(db.run(x), 5 seconds)
  }

  private class GCMLogTable(tag: Tag) extends Table[WSLog](tag, "gcm_log") {

    import slick.lifted._

    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def message: Rep[String] = column[String]("message")
    def userID: Rep[String] = column[String]("user_id")
    def responseStatus: Rep[String] = column[String]("response_status")
    def refId: Rep[String] = column[String]("ref_id")
    def attempt: Rep[Int] = column[Int]("attempt")
    def timeStamp: Rep[Timestamp] = column[Timestamp]("time_stamp", SqlType("TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"))

    override def * : ProvenShape[WSLog] = (id, message, userID, responseStatus, refId, attempt, timeStamp) <> (WSLog.tupled, WSLog.unapply _)
  }
}
