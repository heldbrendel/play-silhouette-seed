package services

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import dao.UserDAO
import javax.inject.Inject
import models.User

import scala.concurrent.{ExecutionContext, Future}

class UserService @Inject()(
                             userDAO: UserDAO
                           )(implicit ec: ExecutionContext)
  extends IdentityService[User] {

  def retrieve(userId: Long): Future[Option[User]] = userDAO.findById(userId)

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.findByLoginInfo(loginInfo)

  def insert(user: User): Future[User] = userDAO.insert(user)

  def update(user: User): Future[Int] = userDAO.update(user)

}
