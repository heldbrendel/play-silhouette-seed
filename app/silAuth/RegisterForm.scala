package silAuth

import play.api.data.Form
import play.api.data.Forms._

object RegisterForm {

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "password" -> nonEmptyText,
      "confirm-password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(username: String,
                  password: String,
                  confirmPassword: String)

}
