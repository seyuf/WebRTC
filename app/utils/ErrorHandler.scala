
package utils

import javax.inject.Inject

import controllers.routes
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.Messages
import play.api.libs.json.Json
import play.api.mvc.Results._
import play.api.mvc.{ RequestHeader, Result }
import play.api.routing.Router
import play.api.{ UsefulException, Configuration, OptionalSourceMapper }

import scala.concurrent.Future

/**
 * A secured error handler.
 */
class ErrorHandler @Inject() (
  env: play.api.Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: javax.inject.Provider[Router]
)
    extends DefaultHttpErrorHandler(env, config, sourceMapper, router) {

  override def onProdServerError(request: RequestHeader, exception: UsefulException) = {
    Future.successful(
      InternalServerError("A Mad prod server error occurred: " + exception.getMessage)
    )
  }

  override def onServerError(request: RequestHeader, exception: Throwable) = {
    Future.successful(
      InternalServerError("A Mad server error occurred: " + exception.getMessage)
    )
  }

}
