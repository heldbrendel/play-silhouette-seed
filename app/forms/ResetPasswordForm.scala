package forms

import play.api.data.Form
import play.api.data.Forms._

object ResetPasswordForm {

  val form = Form(
    mapping(
      "password" -> nonEmptyText,
    )(Data.apply)(Data.unapply)
  )

  case class Data(password: String)

}
