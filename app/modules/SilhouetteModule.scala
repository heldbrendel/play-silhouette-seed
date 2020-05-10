package modules

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.api.actions.{SecuredErrorHandler, UnsecuredErrorHandler}
import com.mohiva.play.silhouette.api.crypto.{Crypter, CrypterAuthenticatorEncoder, Signer}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AuthenticatorService
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{Environment, EventBus, Silhouette, SilhouetteProvider}
import com.mohiva.play.silhouette.crypto.{JcaCrypter, JcaCrypterSettings, JcaSigner, JcaSignerSettings}
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, CookieAuthenticatorService, CookieAuthenticatorSettings}
import com.mohiva.play.silhouette.impl.util.{DefaultFingerprintGenerator, SecureRandomIDGenerator}
import com.mohiva.play.silhouette.password.{BCryptPasswordHasher, BCryptSha256PasswordHasher}
import com.mohiva.play.silhouette.persistence.daos.{DelegableAuthInfoDAO, InMemoryAuthInfoDAO}
import com.mohiva.play.silhouette.persistence.repositories.DelegableAuthInfoRepository
import com.typesafe.config.Config
import controllers.{DefaultRememberMeConfig, DefaultSilhouetteControllerComponents, RememberMeConfig, SilhouetteControllerComponents}
import dao.PasswordInfoDAO
import net.ceedubs.ficus.Ficus._
import net.ceedubs.ficus.readers.ArbitraryTypeReader._
import net.ceedubs.ficus.readers.ValueReader
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import play.api.db.slick.DatabaseConfigProvider
import play.api.mvc.{Cookie, CookieHeaderEncoding}
import services.UserService
import utils.auth.{CookieEnv, CustomSecuredErrorHandler, CustomUnsecuredErrorHandler}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration

class SilhouetteModule extends AbstractModule with ScalaModule {

  implicit val sameSiteReader: ValueReader[Option[Option[Cookie.SameSite]]] =
    (config: Config, path: String) => {
      if (config.hasPathOrNull(path)) {
        if (config.getIsNull(path)) {
          Some(None)
        } else {
          Some(Cookie.SameSite.parse(config.getString(path)))
        }
      } else {
        None
      }
    }

  override def configure(): Unit = {

    bind[Silhouette[CookieEnv]].to[SilhouetteProvider[CookieEnv]]

    bind[UnsecuredErrorHandler].to[CustomUnsecuredErrorHandler]
    bind[SecuredErrorHandler].to[CustomSecuredErrorHandler]

    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[Clock].toInstance(Clock())

    bind[DelegableAuthInfoDAO[PasswordInfo]].to[PasswordInfoDAO]
  }

  @Provides
  def providesPasswordInfoDAO(dbConfigProvider: DatabaseConfigProvider): PasswordInfoDAO = {
    new PasswordInfoDAO(dbConfigProvider)
  }

  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[CookieAuthenticator],
                          eventBus: EventBus
                        ): Environment[CookieEnv] = {

    Environment[CookieEnv](
      userService,
      authenticatorService,
      Seq(),
      eventBus
    )
  }

  @Provides
  def provideAuthInfoRepository(passwordInfoDAO: DelegableAuthInfoDAO[PasswordInfo]): AuthInfoRepository = {
    new DelegableAuthInfoRepository(passwordInfoDAO)
  }

  @Provides
  def provideAuthenticatorService(
                                   signer: Signer,
                                   crypter: Crypter,
                                   cookieHeaderEncoding: CookieHeaderEncoding,
                                   fingerprintGenerator: FingerprintGenerator,
                                   idGenerator: IDGenerator,
                                   config: Configuration,
                                   clock: Clock
                                 ): AuthenticatorService[CookieAuthenticator] = {

    val settings = config.underlying.as[CookieAuthenticatorSettings]("silhouette.authenticator")
    val authenticatorEncoder = new CrypterAuthenticatorEncoder(crypter)

    new CookieAuthenticatorService(
      settings,
      None,
      signer,
      cookieHeaderEncoding,
      authenticatorEncoder,
      fingerprintGenerator,
      idGenerator,
      clock)
  }

  @Provides
  def provideCrypter(
                      config: Configuration
                    ): Crypter = {
    val settings = config.underlying.as[JcaCrypterSettings]("silhouette.authenticator.crypter")
    new JcaCrypter(settings)
  }

  @Provides
  def provideSigner(
                     config: Configuration
                   ): Signer = {
    val settings = config.underlying.as[JcaSignerSettings]("silhouette.authenticator.signer")
    new JcaSigner(settings)
  }

  @Provides
  def providesPasswordHasherRegistry(): PasswordHasherRegistry = {
    PasswordHasherRegistry(new BCryptSha256PasswordHasher(), Seq(new BCryptPasswordHasher()))
  }

  @Provides
  def rememberMeConfig(config: Configuration): RememberMeConfig = {
    val c = config.underlying
    DefaultRememberMeConfig(
      expiry = c.as[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorExpiry"),
      idleTimeout = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.authenticatorIdleTimeout"),
      cookieMaxAge = c.getAs[FiniteDuration]("silhouette.authenticator.rememberMe.cookieMaxAge")
    )
  }

  @Provides
  def providesSilhouetteComponents(components: DefaultSilhouetteControllerComponents): SilhouetteControllerComponents = {
    components
  }
}
