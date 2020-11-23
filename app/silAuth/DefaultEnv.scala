package silAuth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator

trait DefaultEnv extends Env {
  // Identity
  type I = User
  // Authenticator
  type A = CookieAuthenticator
}
