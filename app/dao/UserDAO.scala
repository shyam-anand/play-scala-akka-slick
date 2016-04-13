package dao

import javax.inject.Inject

import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * Created by shyam on 31/03/16.
  */
class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends HasDatabaseConfigProvider[JdbcProfile] {

  import slick.driver.MySQLDriver.api._

  private val Users = TableQuery[UsersTable]

  def listAll: Future[Seq[User]] = {
    db.run(Users.result)
  }

  def get(id: String): Future[Option[User]] = {
    db.run(Users.filter(_.id === id).result.headOption)
  }

  private class UsersTable(tag: Tag) extends Table[User](tag, "users") {

    import slick.lifted._

    def id: Rep[String] = column[String]("id")

    def name: Rep[String] = column[String]("name")

    def gcmID: Rep[String] = column[String]("gcm_id")

    def * : ProvenShape[User] = (id, name, gcmID) <> (User.tupled, User.unapply _)

  }
}
