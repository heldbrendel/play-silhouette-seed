package controllers

import com.mohiva.play.silhouette.api.actions.{SecuredActionBuilder, UnsecuredActionBuilder}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util.{Clock, PasswordHasherRegistry}
import com.mohiva.play.silhouette.api.{EventBus, Silhouette}
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import javax.inject.Inject
import play.api.Logging
import play.api.http.FileMimeTypes
import play.api.i18n.{Langs, MessagesApi}
import play.api.libs.mailer.MailerClient
import play.api.mvc._
import services.{AuthTokenService, UserService}
import utils.auth.CookieEnv

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

abstract class SilhouetteController(override protected val controllerComponents: SilhouetteControllerComponents)
  extends AbstractController(controllerComponents) with SilhouetteComponents with Logging {

  def SecuredAction: SecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.SecuredAction

  def unsecuredAction: UnsecuredActionBuilder[EnvType, AnyContent] = controllerComponents.silhouette.UnsecuredAction


  def userService: UserService = controllerComponents.userService

  def authInfoRepository: AuthInfoRepository = controllerComponents.authInfoRepository

  def passwordHasherRegistry: PasswordHasherRegistry = controllerComponents.passwordHasherRegistry

  def authTokenService: AuthTokenService = controllerComponents.authTokenService

  def mailerClient: MailerClient = controllerComponents.mailerClient

  def rememberMeConfig: RememberMeConfig = controllerComponents.rememberMeConfig

  def clock: Clock = controllerComponents.clock

  def credentialsProvider: CredentialsProvider = controllerComponents.credentialsProvider


  def silhouette: Silhouette[EnvType] = controllerComponents.silhouette

  def authenticatorService: AuthenticatorService[AuthType] = silhouette.env.authenticatorService

  def eventBus: EventBus = silhouette.env.eventBus

}

trait SilhouetteComponents {
  type EnvType = CookieEnv
  type AuthType = EnvType#A
  type IdentityType = EnvType#I

  def userService: UserService

  def authInfoRepository: AuthInfoRepository

  def passwordHasherRegistry: PasswordHasherRegistry

  def authTokenService: AuthTokenService

  def mailerClient: MailerClient

  def rememberMeConfig: RememberMeConfig

  def clock: Clock

  def credentialsProvider: CredentialsProvider

  def silhouette: Silhouette[EnvType]
}

trait SilhouetteControllerComponents extends ControllerComponents with SilhouetteComponents

final case class DefaultSilhouetteControllerComponents @Inject()(
                                                                  silhouette: Silhouette[CookieEnv],
                                                                  userService: UserService,
                                                                  authInfoRepository: AuthInfoRepository,
                                                                  passwordHasherRegistry: PasswordHasherRegistry,
                                                                  authTokenService: AuthTokenService,
                                                                  mailerClient: MailerClient,
                                                                  rememberMeConfig: RememberMeConfig,
                                                                  clock: Clock,
                                                                  credentialsProvider: CredentialsProvider,

                                                                  actionBuilder: DefaultActionBuilder,
                                                                  parsers: PlayBodyParsers,
                                                                  messagesApi: MessagesApi,
                                                                  langs: Langs,
                                                                  fileMimeTypes: FileMimeTypes,
                                                                  executionContext: ExecutionContext
                                                                ) extends SilhouetteControllerComponents


trait RememberMeConfig {
  def expiry: FiniteDuration

  def idleTimeout: Option[FiniteDuration]

  def cookieMaxAge: Option[FiniteDuration]
}

final case class DefaultRememberMeConfig(
                                          expiry: FiniteDuration,
                                          idleTimeout: Option[FiniteDuration],
                                          cookieMaxAge: Option[FiniteDuration]
                                        ) extends RememberMeConfig
