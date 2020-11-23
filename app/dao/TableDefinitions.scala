package dao

import silAuth.User
import slick.jdbc.JdbcProfile

trait TableDefinitions {

  protected val profile: JdbcProfile

  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def username = column[String]("username")

    def email = column[Option[String]]("email")

    def * = (id.?, username, email) <> ((User.apply _).tupled, User.unapply)
  }

  object slickUsers extends TableQuery(new Users(_))

  case class DbPasswordInfo(username: String, hasher: String, password: String, salt: Option[String])

  class PasswordInfos(tag: Tag) extends Table[DbPasswordInfo](tag, "password_infos") {
    def username = column[String]("username", O.PrimaryKey)

    def hasher   = column[String]("hasher")
    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def * = (username, hasher, password, salt) <> ((DbPasswordInfo.apply _).tupled, DbPasswordInfo.unapply)
  }

  object slickPasswordInfos extends TableQuery(new PasswordInfos(_))

}
