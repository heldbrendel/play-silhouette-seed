package silAuth

import com.mohiva.play.silhouette.api.Identity

case class User(id: Option[Long], username: String, email: Option[String]) extends Identity
