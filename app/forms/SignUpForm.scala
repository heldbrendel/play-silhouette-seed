package forms

import play.api.data.Form
import play.api.data.Forms._

object SignUpForm {

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> email,
      "password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   userName: String,
                   email: String,
                   password: String)

}
