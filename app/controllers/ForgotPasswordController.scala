package controllers

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.ForgotPasswordForm
import javax.inject.Inject
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request}
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

class ForgotPasswordController @Inject()(
                                          scc: SilhouetteControllerComponents
                                        )(implicit ex: ExecutionContext) extends SilhouetteController(scc) {

  def view = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>
    Future.successful(Ok(views.html.forgotPassword(ForgotPasswordForm.form)))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request: Request[AnyContent] =>

    ForgotPasswordForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.forgotPassword(form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.userName)
        val result = Redirect(Calls.signIn).flashing("info" -> "Reset email sent")
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            authTokenService.create(user.userId.get).map { authToken =>
              val url = routes.ResetPasswordController.view(authToken.authTokenId).absoluteURL()
              mailerClient.send(Email(
                subject = "Reset password",
                from = "foobar@example.com",
                to = Seq(user.email),
                bodyText = Some(s"Reset password url: $url")
              ))
              result
            }
          case None => Future.successful(result)
        }
      }
    )
  }
}
