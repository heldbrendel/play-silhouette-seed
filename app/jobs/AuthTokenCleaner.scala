package jobs

import akka.actor.Actor
import javax.inject.Inject
import jobs.AuthTokenCleaner.Clean
import play.api.Logging
import services.AuthTokenService

import scala.concurrent.ExecutionContext

class AuthTokenCleaner @Inject()(
                                  service: AuthTokenService
                                )(implicit ex: ExecutionContext) extends Actor with Logging {

  override def receive: Receive = {
    case Clean =>
      service.clean.map { deleted =>
        logger.info("Deleted %s auth token(s)".format(deleted.length))
      }.recover {
        case e => logger.error("Error cleaning up auth tokens", e)
      }
  }
}

object AuthTokenCleaner {

  case object Clean

}
