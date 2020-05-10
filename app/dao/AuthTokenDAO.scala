package dao

import java.sql.Timestamp
import java.util.UUID

import javax.inject.Inject
import models.AuthToken
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfig}
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

class AuthTokenDAO @Inject()(protected val dbConfigProvider: DatabaseConfigProvider
                            )(implicit ex: ExecutionContext)
  extends HasDatabaseConfig[JdbcProfile] {

  import profile.api._

  override val dbConfig = dbConfigProvider.get[JdbcProfile]

  class AuthTokens(tag: Tag) extends Table[AuthToken](tag, Some("public"), "auth_token") {

    def authTokenId = column[UUID]("auth_token_id")

    def userId = column[Long]("user_id")

    def expiry = column[Timestamp]("expiry")

    def * = (authTokenId, userId, expiry).mapTo[AuthToken]
  }

  object authTokens extends TableQuery(new AuthTokens(_))

  def findById(id: UUID): Future[Option[AuthToken]] = {
    db.run(authTokens.filter(_.authTokenId === id).result.headOption)
  }

  def findExpired(timestamp: Timestamp): Future[Seq[AuthToken]] = {
    val query = authTokens.filter(_.expiry < timestamp).result
    db.run(query)
  }

  def save(token: AuthToken): Future[AuthToken] = {
    db.run(authTokens += token).map(_ => token)
  }

  def remove(id: UUID): Future[Int] = {
    db.run(authTokens.filter(_.authTokenId === id).delete)
  }

}
