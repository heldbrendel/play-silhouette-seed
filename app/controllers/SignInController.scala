package controllers

import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.services.AuthenticatorResult
import com.mohiva.play.silhouette.api.util.Credentials
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignInForm
import javax.inject.Inject
import models.User
import play.api.mvc.{AnyContent, Request, RequestHeader}
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

class SignInController @Inject()(
                                  scc: SilhouetteControllerComponents
                                )(implicit ec: ExecutionContext) extends SilhouetteController(scc) {

  def view = silhouette.UnsecuredAction { implicit request =>
    Ok(views.html.signIn(SignInForm.form))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    SignInForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signIn(form))),
      data => {
        val credentials = Credentials(data.userName, data.password)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          userService.retrieve(loginInfo).flatMap {
            case Some(user) => authenticateUser(user, data.rememberMe)
            case None => Future.failed(new IdentityNotFoundException("Could not find user"))
          }
        }
      }.recover {
        case _: ProviderException =>
          Redirect(Calls.signIn).flashing("error" -> "Invalid credentials")
      }
    )
  }

  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit request: RequestHeader): Future[AuthenticatorResult] = {
    val result = Redirect(Calls.home)

    val loginInfo = LoginInfo(CredentialsProvider.ID, user.userName)
    authenticatorService.create(loginInfo).map {
      case authenticator if rememberMe =>

        val t = clock
        authenticator.copy(
          expirationDateTime = clock.now.plus(scc.rememberMeConfig.expiry.toMillis),
          idleTimeout = scc.rememberMeConfig.idleTimeout,
          cookieMaxAge = scc.rememberMeConfig.cookieMaxAge)

      case authenticator => authenticator
    }.flatMap { authenticator =>
      eventBus.publish(LoginEvent(user, request))
      authenticatorService.init(authenticator).flatMap { v =>
        authenticatorService.embed(v, result)
      }
    }
  }
}
