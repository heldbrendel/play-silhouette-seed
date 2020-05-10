package utils.route

import play.api.mvc.Call

object Calls {

  def home: Call = controllers.routes.ApplicationController.index()

  def signIn: Call = controllers.routes.SignInController.view()

}
