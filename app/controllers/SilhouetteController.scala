package controllers

import silAuth._
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.{AuthenticatorResult, AuthenticatorService}
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{AbstractController, ControllerComponents, RequestHeader}

import scala.concurrent.{ExecutionContext, Future}

class SilhouetteController @Inject() (
    cc: ControllerComponents,
    userService: UserService,
    passwordHasherRegistry: PasswordHasherRegistry,
    credentialsProvider: CredentialsProvider,
    authInfoRepository: AuthInfoRepository,
    clock: Clock,
    authenticatorService: AuthenticatorService[CookieAuthenticator],
    cookieAuthenticatorSettings: CookieAuthenticatorSettings,
    eventBus: EventBus,
    silhouette: Silhouette[DefaultEnv]
)(implicit executionContext: ExecutionContext, assetsFinder: AssetsFinder)
    extends AbstractController(cc)
    with I18nSupport {

  def showRegister = silhouette.UnsecuredAction { implicit request =>
    Ok(views.html.register(RegisterForm.form))
  }

  def register = silhouette.UnsecuredAction.async { implicit request =>
    RegisterForm.form
      .bindFromRequest()
      .fold(
        hasErrors => Future.successful(BadRequest(views.html.register(hasErrors))),
        success => {
          val loginInfo = LoginInfo(CredentialsProvider.ID, success.username)
          userService.retrieve(loginInfo).flatMap {
            case Some(_) =>
              Future.successful(
                BadRequest(views.html.register(RegisterForm.form.withGlobalError(Messages("warning.username-taken"))))
              )

            case None =>
              if (success.password != success.confirmPassword) {
                Future.successful(
                  BadRequest(
                    views.html.register(
                      RegisterForm.form.fill(success).withGlobalError(Messages("warning.passwords-not-equal"))
                    )
                  )
                )
              } else {
                val passwordInfo = passwordHasherRegistry.current.hash(success.password)
                val user         = User(None, success.username, None)

                for {
                  user <- userService.save(user)
                  _    <- authInfoRepository.add(loginInfo, passwordInfo)
                } yield {
                  eventBus.publish(SignUpEvent(user, request))
                  Redirect(routes.SilhouetteController.showLogin())
                    .flashing("info" -> Messages("registration.success"))
                }
              }
          }
        }
      )
  }

  def showLogin = silhouette.UnsecuredAction { implicit request =>
    Ok(views.html.login(LoginForm.form))
  }

  def login = silhouette.UnsecuredAction.async { implicit request =>
    LoginForm.form
      .bindFromRequest()
      .fold(
        hasErrors => Future.successful(BadRequest(views.html.login(hasErrors))),
        success =>
          {
            val credentials = Credentials(success.username, success.password)
            credentialsProvider.authenticate(credentials).flatMap { loginInfo =>
              userService.retrieve(loginInfo).flatMap {
                case Some(user) => authenticateUser(user, success.rememberMe)
                case None       => Future.failed(new IdentityNotFoundException("Could not find user"))
              }
            }
          }.recover { case _: ProviderException =>
            BadRequest(views.html.login(LoginForm.form.withGlobalError(Messages("warning.credentials-invalid"))))
          }
      )
  }

  protected def authenticateUser(user: User, rememberMe: Boolean)(implicit
      request: RequestHeader
  ): Future[AuthenticatorResult] = {
    val result = Redirect(controllers.routes.Application.profile()).flashing("success" -> Messages("login.success"))

    val loginInfo = LoginInfo(CredentialsProvider.ID, user.username)
    authenticatorService
      .create(loginInfo)
      .map {
        case authenticator if rememberMe =>
          authenticator.copy(
            expirationDateTime = clock.now.plus(cookieAuthenticatorSettings.authenticatorExpiry.toMillis),
            idleTimeout = cookieAuthenticatorSettings.authenticatorIdleTimeout,
            cookieMaxAge = cookieAuthenticatorSettings.cookieMaxAge
          )

        case authenticator => authenticator
      }
      .flatMap { authenticator =>
        eventBus.publish(LoginEvent(user, request))
        authenticatorService.init(authenticator).flatMap { v =>
          authenticatorService.embed(v, result)
        }
      }
  }

  def logout = silhouette.SecuredAction.async { implicit request =>
    val result = Redirect(routes.SilhouetteController.login()).flashing("success" -> Messages("logout.success"))
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }

  def showChangePassword = silhouette.SecuredAction { implicit request =>
    Ok(views.html.changePassword(request.identity, ChangePasswordForm.form))
  }

  def changePassword = silhouette.SecuredAction.async { implicit request =>
    val username = request.identity.username
    ChangePasswordForm.form.bindFromRequest.fold(
      hasErrors => Future.successful(BadRequest(views.html.changePassword(request.identity, hasErrors))),
      success => {
        val credentials = Credentials(username, success.oldPassword)
        credentialsProvider
          .authenticate(credentials)
          .flatMap { loginInfo =>
            if (success.newPassword != success.confirmNewPassword) {
              Future.successful(
                BadRequest(
                  views.html
                    .changePassword(
                      request.identity,
                      ChangePasswordForm.form.fill(success).withGlobalError(Messages("warning.passwords-not-equal"))
                    )
                )
              )
            } else {
              val passwordInfo = passwordHasherRegistry.current.hash(success.newPassword)
              authInfoRepository.update(loginInfo, passwordInfo).map { _ =>
                Redirect(routes.SilhouetteController.showChangePassword())
                  .flashing("success" -> Messages("success.password-change"))
              }
            }
          }
          .recover { case _: ProviderException =>
            BadRequest(
              views.html.changePassword(
                request.identity,
                ChangePasswordForm.form.withGlobalError(Messages("warning.credentials"))
              )
            )
          }
      }
    )
  }
}
