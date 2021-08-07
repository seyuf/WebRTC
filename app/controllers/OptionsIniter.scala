package controllers

import play.api.mvc.{ Action, Controller }

/**
 * Created by madalien on 02/07/15.
 */

object OptionsIniter extends Controller {
  def options(path: String) = Action { Ok("") }
}
