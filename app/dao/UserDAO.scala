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
