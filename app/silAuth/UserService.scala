package silAuth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import javax.inject.Inject

import scala.concurrent.Future

class UserService @Inject() (userDAO: UserDAO) extends IdentityService[User] {

  override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = {
    val userName = loginInfo.providerKey
    userDAO.findByUsername(userName)
  }

  def save(user: User): Future[User] = {
    userDAO.save(user)
  }
}
