package models

import java.sql.Timestamp

import com.mohiva.play.silhouette.api.Identity

case class User(
                 userId: Option[Long],
                 userName: String,
                 email: String,
                 firstName: Option[String],
                 lastName: Option[String],
                 activated: Boolean,
                 created: Timestamp,
                 modified: Timestamp)
  extends Identity
