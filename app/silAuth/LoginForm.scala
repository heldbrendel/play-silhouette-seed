package silAuth

import play.api.data.Form
import play.api.data.Forms._

object LoginForm {

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "remember-me" -> boolean
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String,
                  password: String,
                  rememberMe: Boolean)

}
