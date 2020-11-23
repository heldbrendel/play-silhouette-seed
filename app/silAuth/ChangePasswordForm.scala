package silAuth

import play.api.data.Form
import play.api.data.Forms._

object ChangePasswordForm {

  val form = Form(
    mapping(
      "old-password" -> nonEmptyText,
      "new-password" -> nonEmptyText,
      "confirm-new-password" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(oldPassword: String,
                  newPassword: String,
                  confirmNewPassword: String)

}
