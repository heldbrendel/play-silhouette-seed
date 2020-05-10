package controllers

import com.mohiva.play.silhouette.api.actions.SecuredRequest
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.{EventBus, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import javax.inject._
import models.User
import play.api.mvc._
import utils.auth.CookieEnv

@Singleton
class ApplicationController @Inject()(
                                       val controllerComponents: ControllerComponents,
                                       silhouette: Silhouette[CookieEnv],
                                       eventBus: EventBus,
                                       authenticatorService: AuthenticatorService[CookieAuthenticator]
                                     ) extends BaseController {

  def index = silhouette.SecuredAction { implicit request: SecuredRequest[CookieEnv, AnyContent] =>
    val user: User = request.identity
    Ok(views.html.index(user))
  }

  def signOut = silhouette.SecuredAction.async { implicit request: SecuredRequest[CookieEnv, AnyContent] =>
    val result = Ok(views.html.signOut())
    eventBus.publish(LogoutEvent(request.identity, request))
    authenticatorService.discard(request.authenticator, result)
  }
}
