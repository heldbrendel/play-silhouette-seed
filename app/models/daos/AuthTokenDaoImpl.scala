package models.daos

import java.sql.Timestamp
import java.util.UUID

import javax.inject.Inject
import models.AuthToken
import play.api.db.slick.DatabaseConfigProvider

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Give access to the [[AuthToken]] object.
 */
class AuthTokenDaoImpl @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends AuthTokenDao with DaoSlick {

  import profile.api._

  /**
   * Finds a token by its ID.
   *
   * @param id The unique token ID.
   * @return The found token or None if no token for the given ID could be found.
   */
  def find(id: UUID): Future[Option[AuthToken]] = db.run {
    slickAuthTokens.filter(_.id === id).result.headOption
  }

  /**
   * Finds expired tokens.
   *
   * @param timeStamp The current date time.
   */
  def findExpired(timeStamp: Timestamp): Future[Seq[AuthToken]] = db.run {
    slickAuthTokens.filter(_.expiry < timeStamp).result
  }

  /**
   * Saves a token.
   *
   * @param token The token to save.
   * @return The saved token.
   */
  def save(token: AuthToken): Future[AuthToken] = {
    val q = for {
      _ <- slickAuthTokens += token
    } yield token
    db.run(q)
  }

  /**
   * Removes the token for the given ID.
   *
   * @param id The ID for which the token should be removed.
   * @return A future to wait for the process to be completed.
   */
  def remove(id: UUID): Future[Int] = db.run {
    slickAuthTokens.filter(_.id === id).delete
  }
}
