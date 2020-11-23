package silAuth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import dao.SlickDb
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

class PasswordInfoDao @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    val classTag: ClassTag[PasswordInfo],
    executionContext: ExecutionContext
) extends DelegableAuthInfoDAO[PasswordInfo]
    with SlickDb {

  import profile.api._

  override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
    db.run(
      slickPasswordInfos.filter(_.username === loginInfo.providerKey).result.headOption
    ).map {
      case Some(dbInfo) => Some(toPasswordInfo(dbInfo))
      case None         => None
    }
  }

  override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbInfo = fromPasswordInfo(loginInfo, authInfo)
    db.run(slickPasswordInfos += dbInfo)
      .map(_ => authInfo)
  }

  override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbInfo = fromPasswordInfo(loginInfo, authInfo)
    db.run(slickPasswordInfos.update(dbInfo))
      .map(_ => authInfo)
  }

  override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val dbInfo = fromPasswordInfo(loginInfo, authInfo)
    db.run(slickPasswordInfos.insertOrUpdate(dbInfo)).map(_ => authInfo)
  }

  override def remove(loginInfo: LoginInfo): Future[Unit] = {
    db.run(slickPasswordInfos.filter(_.username === loginInfo.providerKey).delete)
      .map(_ => ())
  }

  private def toPasswordInfo(dbPasswordInfo: DbPasswordInfo): PasswordInfo = {
    PasswordInfo(dbPasswordInfo.hasher, dbPasswordInfo.password, dbPasswordInfo.salt)
  }

  private def fromPasswordInfo(loginInfo: LoginInfo, passwordInfo: PasswordInfo): DbPasswordInfo = {
    DbPasswordInfo(loginInfo.providerKey, passwordInfo.hasher, passwordInfo.password, passwordInfo.salt)
  }
}
