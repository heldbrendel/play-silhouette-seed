package models.services

import java.sql.Timestamp
import java.time.Instant
import java.time.temporal.{ ChronoUnit, TemporalUnit }
import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api.util.Clock
import models.AuthToken
import models.daos.AuthTokenDao
import org.joda.time.DateTimeZone

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.duration._
import scala.language.postfixOps

/**
 * Handles actions to auth tokens.
 *
 * @param authTokenDAO The auth token DAO implementation.
 * @param clock        The clock instance.
 * @param ex           The execution context.
 */
class AuthTokenServiceImpl @Inject() (
  authTokenDAO: AuthTokenDao,
  clock: Clock
)(
  implicit
  ex: ExecutionContext
) extends AuthTokenService {

  /**
   * Creates a new auth token and saves it in the backing store.
   *
   * @param userID The user ID for which the token should be created.
   * @param expiry The duration a token expires.
   * @return The saved auth token.
   */
  def create(userID: UUID, expiry: FiniteDuration = 5 minutes) = {
    val token = AuthToken(UUID.randomUUID(), userID, Timestamp.from(Instant.now().plus(expiry.toSeconds, ChronoUnit.SECONDS)))
    authTokenDAO.save(token)
  }

  /**
   * Validates a token ID.
   *
   * @param id The token ID to validate.
   * @return The token if it's valid, None otherwise.
   */
  def validate(id: UUID) = authTokenDAO.find(id)

  /**
   * Cleans expired tokens.
   *
   * @return The list of deleted tokens.
   */
  def clean = authTokenDAO.findExpired(Timestamp.from(Instant.now())).flatMap { tokens =>
    Future.sequence(tokens.map { token =>
      authTokenDAO.remove(token.id).map(_ => token)
    })
  }
}
