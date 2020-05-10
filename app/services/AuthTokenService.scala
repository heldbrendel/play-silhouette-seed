package services

import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

import com.mohiva.play.silhouette.api.util.Clock
import dao.AuthTokenDAO
import javax.inject.Inject
import models.AuthToken
import org.joda.time.DateTimeZone

import scala.concurrent.duration.{FiniteDuration, _}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class AuthTokenService @Inject()(
                                  authTokenDAO: AuthTokenDAO,
                                  clock: Clock
                                )(implicit ex: ExecutionContext) {

  def create(userId: Long, expiry: FiniteDuration = 5 minutes) = {

    val expire = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt)
    val expireTimestamp = new Timestamp(expire.getMillis)
    val token = AuthToken(UUID.randomUUID(), userId, expireTimestamp)
    authTokenDAO.save(token)
  }

  def validate(id: UUID) = authTokenDAO.findById(id)

  def clean = {
    val now = Timestamp.from(Instant.now())
    authTokenDAO.findExpired(now).flatMap { tokens =>
      Future.sequence(tokens.map { token =>
        authTokenDAO.remove(token.authTokenId).map(_ => token)
      })
    }
  }
}
