package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.util.{Credentials, PasswordInfo}
import forms.ChangePasswordForm
import javax.inject.Inject
import play.api.mvc.AnyContent
import utils.auth.CookieEnv

import scala.concurrent.{ExecutionContext, Future}

class ChangePasswordController @Inject()(
                                          scc: SilhouetteControllerComponents
                                        )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def view = silhouette.SecuredAction { implicit request: SecuredRequest[CookieEnv, AnyContent] =>
    Ok(views.html.changePassword(ChangePasswordForm.form))
  }

  def submit = silhouette.SecuredAction.async { implicit request: SecuredRequest[CookieEnv, AnyContent] =>
    ChangePasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.changePassword(form))),
      data => {
        val credentials = Credentials(request.identity.userName, data.currentPassword)
        credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
          val passwordInfo = passwordHasherRegistry.current.hash(data.newPassword)
          authInfoRepository.update[PasswordInfo](loginInfo, passwordInfo).map { _ =>
            Redirect(routes.ChangePasswordController.view()).flashing("success" -> "Password changed")
          }
        }.recover {
          case _: ProviderException =>
            Redirect(routes.ChangePasswordController.view()).flashing("error" -> "Current password invalid")
        }
      }
    )
  }
}
