package forms

import play.api.data.Form
import play.api.data.Forms._

object ForgotPasswordForm {

  val form = Form(
    mapping(
      "username" -> nonEmptyText,
      "email" -> email
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   userName: String,
                   email: String)

}
