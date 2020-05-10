package controllers

import java.sql.Timestamp

import com.mohiva.play.silhouette.api.{LoginInfo, SignUpEvent}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import forms.SignUpForm
import javax.inject.Inject
import models.User
import play.api.libs.mailer.Email
import play.api.mvc.{AnyContent, Request}
import utils.route.Calls

import scala.concurrent.{ExecutionContext, Future}

class SignUpController @Inject()(scc: SilhouetteControllerComponents
                                )(implicit ec: ExecutionContext)
  extends SilhouetteController(scc) {

  def view = silhouette.UnsecuredAction { implicit request: Request[AnyContent] =>
    Ok(views.html.signUp(SignUpForm.form))
  }

  def submit = silhouette.UnsecuredAction.async { implicit request =>

    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {

        val result = Redirect(routes.SignUpController.view()).flashing("info" -> "Sign up email sent")
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.userName)
        userService.retrieve(loginInfo).flatMap {
          case Some(_) =>

            val url = Calls.signIn.absoluteURL()
            mailerClient.send(Email(
              subject = "Already signed up",
              from = "foobar@example.com",
              to = Seq(data.email),
              bodyText = Some(s"Already signed up: $url")
            ))

            Future.successful(result)

          case None =>

            val authInfo = passwordHasherRegistry.current.hash(data.password)
            val now = new Timestamp(System.currentTimeMillis())
            val user = User(
              userId = None,
              userName = data.userName,
              email = data.email,
              firstName = None,
              lastName = None,
              activated = false,
              created = now,
              modified = now)

            for {
              user <- userService.insert(user)
              _ <- authInfoRepository.add(loginInfo, authInfo)
              authToken <- authTokenService.create(user.userId.get)

            } yield {

              val url = routes.ActivateAccountController.activate(authToken.authTokenId).absoluteURL()
              mailerClient.send(Email(
                subject = "Sign up",
                from = "foobar@example.com",
                to = Seq(data.email),
                bodyText = Some(s"Sign up: $url")
              ))

              eventBus.publish(SignUpEvent(user, request))
              result
            }
        }
      }
    )
  }
}
