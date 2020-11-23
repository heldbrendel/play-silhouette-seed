package controllers

import com.mohiva.play.silhouette.api.Silhouette
import javax.inject.Inject
import play.api.i18n.{I18nSupport, Lang}
import play.api.mvc.{AbstractController, ControllerComponents}
import silAuth.DefaultEnv

import scala.concurrent.ExecutionContext

class Application @Inject() (
    cc: ControllerComponents,
    silhouette: Silhouette[DefaultEnv]
)(implicit executionContext: ExecutionContext, assetsFinder: AssetsFinder)
    extends AbstractController(cc)
    with I18nSupport {

  def index = silhouette.UnsecuredAction { implicit request =>
    Redirect(routes.SilhouetteController.showLogin())
  }

  def profile = silhouette.SecuredAction { implicit request =>
    Ok(views.html.profile(request.identity))
  }

  def setLanguage(lang: String, previous: Option[String]) = Action { implicit request =>
    Redirect(previous.getOrElse("/")).withLang(Lang(lang))
  }
}
