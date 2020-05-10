package controllers

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.ResetPasswordForm
import javax.inject.Inject
import play.api.mvc.{AnyContent, Request}
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

class ResetPasswordController @Inject()(
                                         scc: SilhouetteControllerComponents
                                       )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def view(authTokenId: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(authTokenId).map {
      case Some(_) => Ok(views.html.resetPassword(ResetPasswordForm.form, authTokenId))
      case None => Redirect(Calls.signIn).flashing("error" -> "Invalid reset link")
    }
  }

  def submit(authTokenId: UUID) = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    authTokenService.validate(authTokenId).flatMap {
      case Some(authToken) =>
        ResetPasswordForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.resetPassword(form, authTokenId))),
          data => userService.retrieve(authToken.userId).flatMap {
            case Some(user) =>
              val passwordInfo = passwordHasherRegistry.current.hash(data.password)
              val loginInfo = LoginInfo(CredentialsProvider.ID, user.userName)
              authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
                Redirect(Calls.signIn).flashing("success" -> "Password reset")
              }
            case _ => Future.successful(Redirect(Calls.signIn).flashing("error" -> "Invalid reset link"))
          }
        )

      case None => Future.successful(Redirect(Calls.signIn).flashing("error" -> "Invalid reset link"))
    }
  }
}
