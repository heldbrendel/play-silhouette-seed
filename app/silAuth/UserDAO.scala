package silAuth

import dao.SlickDb
import javax.inject.Inject
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ExecutionContext, Future}

class UserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit
    executionContext: ExecutionContext
) extends SlickDb {

  import profile.api._

  def findByUsername(userName: String): Future[Option[User]] = {
    db.run(slickUsers.filter(_.username === userName).result.headOption)
  }

  def save(user: User): Future[User] = {
    db.run(
      (slickUsers returning slickUsers.map(_.id)
        into ((user,id) => user.copy(id=Some(id)))
        ) += user
    )
  }
}
