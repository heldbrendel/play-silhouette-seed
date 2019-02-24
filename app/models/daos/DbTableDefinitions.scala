package models.daos

import java.sql.Timestamp
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.AuthToken
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait DbTableDefinitions {

  protected val driver: JdbcProfile

  import driver.api._

  case class DBUser(
    userID: UUID,
    firstName: Option[String],
    lastName: Option[String],
    fullName: Option[String],
    email: Option[String],
    avatarURL: Option[String],
    activated: Boolean)

  class Users(tag: Tag) extends Table[DBUser](tag, "USER") {
    def id = column[UUID]("USERID", O.PrimaryKey)

    def firstName = column[Option[String]]("FIRSTNAME")

    def lastName = column[Option[String]]("LASTNAME")

    def fullName = column[Option[String]]("FULLNAME")

    def email = column[Option[String]]("EMAIL")

    def avatarURL = column[Option[String]]("AVATARURL")

    def activated = column[Boolean]("ACTIVATED")

    def * = (id, firstName, lastName, fullName, email, avatarURL, activated) <> (DBUser.tupled, DBUser.unapply)
  }

  case class DBLoginInfo(
    id: Option[Long],
    providerID: String,
    providerKey: String
  )

  class LoginInfos(tag: Tag) extends Table[DBLoginInfo](tag, "LOGININFO") {
    def id = column[Long]("ID", O.PrimaryKey, O.AutoInc)

    def providerID = column[String]("PROVIDERID")

    def providerKey = column[String]("PROVIDERKEY")

    def * = (id.?, providerID, providerKey) <> (DBLoginInfo.tupled, DBLoginInfo.unapply)
  }

  case class DBUserLoginInfo(
    userID: UUID,
    loginInfoId: Long
  )

  class UserLoginInfos(tag: Tag) extends Table[DBUserLoginInfo](tag, "USERLOGININFO") {
    def userID = column[UUID]("USERID")

    def loginInfoId = column[Long]("LOGININFOID")

    def * = (userID, loginInfoId) <> (DBUserLoginInfo.tupled, DBUserLoginInfo.unapply)
  }

  case class DBPasswordInfo(
    hasher: String,
    password: String,
    salt: Option[String],
    loginInfoId: Long
  )

  class PasswordInfos(tag: Tag) extends Table[DBPasswordInfo](tag, "PASSWORDINFO") {
    def hasher = column[String]("HASHER")

    def password = column[String]("PASSWORD")

    def salt = column[Option[String]]("SALT")

    def loginInfoId = column[Long]("LOGININFOID")

    def * = (hasher, password, salt, loginInfoId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
  }

  class AuthTokens(tag: Tag) extends Table[AuthToken](tag, "AUTHTOKEN") {
    def id = column[UUID]("ID")

    def userID = column[UUID]("USERID")

    def expiry = column[Timestamp]("EXPIRY")

    def * = (id, userID, expiry) <> (AuthToken.tupled, AuthToken.unapply)
  }

  // table query definitions
  val slickUsers = TableQuery[Users]
  val slickLoginInfos = TableQuery[LoginInfos]
  val slickUserLoginInfos = TableQuery[UserLoginInfos]
  val slickPasswordInfos = TableQuery[PasswordInfos]
  val slickAuthTokens = TableQuery[AuthTokens]

  // queries used in multiple places
  def loginInfoQuery(loginInfo: LoginInfo) =
    slickLoginInfos.filter(dbLoginInfo => dbLoginInfo.providerID === loginInfo.providerID && dbLoginInfo.providerKey === loginInfo.providerKey)
}
