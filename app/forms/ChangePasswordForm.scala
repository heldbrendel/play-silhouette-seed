package forms

import play.api.data.Form
import play.api.data.Forms._

object ChangePasswordForm {

  val form = Form(
    mapping(
      "currentPassword" -> nonEmptyText,
      "newPassword" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(
                   currentPassword: String,
                   newPassword: String)

}
