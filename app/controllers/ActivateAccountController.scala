package controllers

import java.net.URLDecoder
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request}
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

class ActivateAccountController @Inject()(scc: SilhouetteControllerComponents
                                         )(implicit ex: ExecutionContext)
  extends SilhouetteController(scc) {

  def send(userName: String) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    val loginInfo = LoginInfo(CredentialsProvider.ID, userName)
    val result = Redirect(Calls.signIn).flashing("info" -> "Activation email sent")

    userService.retrieve(loginInfo).flatMap {
      case Some(user) if !user.activated =>
        val decodedEmail = URLDecoder.decode(user.email, "UTF-8")
        authTokenService.create(user.userId.get).map { authToken =>

          val url = routes.ActivateAccountController.activate(authToken.authTokenId).absoluteURL()

          mailerClient.send(Email(
            subject = "Activate account",
            from = "foobar@example.com",
            to = Seq(decodedEmail),
            bodyText = Some(s"Activate url: $url"),
          ))

          result
        }

      case None => Future.successful(result)
    }
  }

  def activate(token: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(token).flatMap {
      case Some(authToken) => userService.retrieve(authToken.userId).flatMap {
        case Some(user) =>
          userService.update(user.copy(activated = true, modified = Timestamp.from(Instant.now()))).map { _ =>
            Redirect(Calls.signIn).flashing("success" -> "account activated")
          }
        case _ => Future.successful(Redirect(Calls.signIn).flashing("error" -> "invalid activation link"))
      }
      case None => Future.successful(Redirect(Calls.signIn).flashing("error" -> "invalid activation link"))
    }
  }
}
