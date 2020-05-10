package dao

import java.sql.Timestamp

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.User
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                       )(implicit ex: ExecutionContext)
  extends HasDatabaseConfig[JdbcProfile] {

  import profile.api._

  override val dbConfig = dbConfigProvider.get[JdbcProfile]

  class Users(tag: Tag) extends Table[User](tag, Some("public"), "users") {

    def userId = column[Long]("user_id", O.AutoInc)

    def userName = column[String]("username")

    def email = column[String]("email")

    def firstName = column[String]("first_name")

    def lastName = column[String]("last_name")

    def activated = column[Boolean]("activated")

    def created = column[Timestamp]("created")

    def modified = column[Timestamp]("modified")

    def * = (userId.?, userName, email, firstName.?, lastName.?, activated, created, modified).mapTo[User]
  }

  object users extends TableQuery(new Users(_))

  def findById(userId: Long): Future[Option[User]] = {
    db.run(users.filter(_.userId === userId).result.headOption)
  }

  def findByUserName(userName: String): Future[Option[User]] = {
    db.run(users.filter(_.userName === userName).result.headOption)
  }

  def findByLoginInfo(loginInfo: LoginInfo): Future[Option[User]] = {
    findByUserName(loginInfo.providerKey)
  }

  def insert(user: User): Future[User] = {
    val userWithId = (
      users returning users.map(_.userId)
        into ((user, userId) => user.copy(userId = Some(userId)))
      ) += user
    db.run(userWithId)
  }

  def update(user: User): Future[Int] = {
    db.run(users.update(user))
  }
}
