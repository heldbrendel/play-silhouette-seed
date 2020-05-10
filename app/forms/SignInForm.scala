package forms

import play.api.data.Form
import play.api.data.Forms._

object SignInForm {

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "rememberMe" -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   userName: String,
                   password: String,
                   rememberMe: Boolean)

}
