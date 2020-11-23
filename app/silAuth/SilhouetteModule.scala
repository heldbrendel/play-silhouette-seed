package silAuth

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions._
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{
  CookieAuthenticator,
  CookieAuthenticatorService,
  CookieAuthenticatorSettings
}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.Cookie.SameSite
import play.api.mvc.CookieHeaderEncoding

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class SilhouetteModule extends AbstractModule {

  override def configure(): Unit = {
    bind(classOf[SecuredErrorHandler]).to(classOf[CustomSecuredErrorHandler])
    bind(classOf[UnsecuredErrorHandler]).to(classOf[CustomUnsecuredErrorHandler])

    bind(classOf[IDGenerator]).toInstance(new SecureRandomIDGenerator())
    bind(classOf[FingerprintGenerator]).toInstance(new DefaultFingerprintGenerator(false))
    bind(classOf[EventBus]).toInstance(EventBus())
    bind(classOf[Clock]).toInstance(Clock())
  }

  @Provides
  def providesSilhouette(
      environment: Environment[DefaultEnv],
      securedAction: SecuredAction,
      unsecuredAction: UnsecuredAction,
      userAwareAction: UserAwareAction
  ): Silhouette[DefaultEnv] = {
    new SilhouetteProvider[DefaultEnv](environment, securedAction, unsecuredAction, userAwareAction)
  }

  @Provides
  def providesEnvironment(
      userService: UserService,
      authenticatorService: AuthenticatorService[CookieAuthenticator],
      eventBus: EventBus
  ): Environment[DefaultEnv] = {
    Environment[DefaultEnv](userService, authenticatorService, Seq(), eventBus)
  }

  @Provides
  def provideDelegableAuthInfoDAO(dbConfigProvider: DatabaseConfigProvider): DelegableAuthInfoDAO[PasswordInfo] = {
    new PasswordInfoDao(dbConfigProvider)
  }

  @Provides
  def providePasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  @Provides
  def providesAuthenticatorService(
      cookieAuthenticatorSettings: CookieAuthenticatorSettings,
      signer: Signer,
      crypter: Crypter,
      cookieHeaderEncoding: CookieHeaderEncoding,
      fingerprintGenerator: FingerprintGenerator,
      idGenerator: IDGenerator,
      clock: Clock
  ): AuthenticatorService[CookieAuthenticator] = {
    new CookieAuthenticatorService(
      cookieAuthenticatorSettings,
      None,
      signer,
      cookieHeaderEncoding,
      new CrypterAuthenticatorEncoder(crypter),
      fingerprintGenerator,
      idGenerator,
      clock
    )
  }

  @Provides
  def providesCrypter(jcaCrypterSettings: JcaCrypterSettings): Crypter = {
    new JcaCrypter(jcaCrypterSettings)
  }

  @Provides
  def providesSigner(jcaSignerSettings: JcaSignerSettings): Signer = {
    new JcaSigner(jcaSignerSettings)
  }

  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  @Provides
  def providesCookieAuthenticatorSettings(configuration: Configuration): CookieAuthenticatorSettings = {
    val cookieName         = configuration.get[String]("silhouette.authenticator.cookieName")
    val cookiePath         = configuration.get[String]("silhouette.authenticator.cookiePath")
    val secureCookie       = configuration.get[Boolean]("silhouette.authenticator.secureCookie")
    val httpOnlyCookie     = configuration.get[Boolean]("silhouette.authenticator.httpOnlyCookie")
    val sameSite           = configuration.get[String]("silhouette.authenticator.sameSite")
    val userFingerprinting = configuration.get[Boolean]("silhouette.authenticator.useFingerprinting")

    val cookieMaxAge = configuration.get[String]("silhouette.authenticator.rememberMe.cookieMaxAge")
    val authenticatorIdleTimeout =
      configuration.get[String]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout")
    val authenticatorExpiry = configuration.get[String]("silhouette.authenticator.rememberMe.authenticatorExpiry")

    CookieAuthenticatorSettings(
      cookieName,
      cookiePath,
      None,
      secureCookie,
      httpOnlyCookie,
      SameSite.parse(sameSite),
      userFingerprinting,
      Some(FiniteDuration.apply(cookieMaxAge.split(' ')(0).toLong, cookieMaxAge.split(' ')(1))),
      Some(FiniteDuration.apply(authenticatorIdleTimeout.split(' ')(0).toLong, authenticatorIdleTimeout.split(' ')(1))),
      FiniteDuration.apply(authenticatorExpiry.split(' ')(0).toLong, authenticatorExpiry.split(' ')(1))
    )
  }

  @Provides
  def providesJcaCrypterSettings(configuration: Configuration): JcaCrypterSettings = {
    JcaCrypterSettings(configuration.get[String]("silhouette.authenticator.crypter.key"))
  }

  @Provides
  def providesJcaSignerSettings(configuration: Configuration): JcaSignerSettings = {
    JcaSignerSettings(configuration.get[String]("silhouette.authenticator.signer.key"))
  }
}
