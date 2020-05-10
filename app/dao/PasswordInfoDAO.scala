package dao

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import javax.inject.Inject
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordInfoDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                               )(implicit ex: ExecutionContext, val classTag: ClassTag[PasswordInfo])
  extends HasDatabaseConfig[JdbcProfile] with DelegableAuthInfoDAO[PasswordInfo] {

  import profile.api._

  override val dbConfig = dbConfigProvider.get[JdbcProfile]

  case class DbPasswordInfo(userName: String, hasher: String, password: String, salt: Option[String])

  class PasswordInfos(tag: Tag) extends Table[DbPasswordInfo](tag, Some("public"), "password_info") {

    def userName = column[String]("user_name")

    def hasher = column[String]("hasher")

    def password = column[String]("password")

    def salt = column[Option[String]]("salt")

    def * = (userName, hasher, password, salt).mapTo[DbPasswordInfo]
  }

  object passwordInfos extends TableQuery(new PasswordInfos(_))

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    val query = passwordInfos.filter(_.userName === loginInfo.providerKey).result.headOption
    db.run(query).map {
      case Some(dbInfo) => Some(PasswordInfo(dbInfo.hasher, dbInfo.password, dbInfo.salt))
      case _ => None
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbPasswordInfo = DbPasswordInfo(loginInfo.providerKey, authInfo.hasher, authInfo.password, authInfo.salt)
    val query = passwordInfos += dbPasswordInfo
    db.run(query).map(_ => authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbPasswordInfo = DbPasswordInfo(loginInfo.providerKey, authInfo.hasher, authInfo.password, authInfo.salt)
    val query = passwordInfos.update(dbPasswordInfo)
    db.run(query).map(_ => authInfo)
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbPasswordInfo = DbPasswordInfo(loginInfo.providerKey, authInfo.hasher, authInfo.password, authInfo.salt)
    val query = passwordInfos.insertOrUpdate(dbPasswordInfo)
    db.run(query).map(_ => authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    val query = passwordInfos.filter(_.userName === loginInfo.providerKey).delete
    db.run(query).map(_ => Future.unit)
  }
}
