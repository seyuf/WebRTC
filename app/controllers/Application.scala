package controllers
import java.io.FileOutputStream
import java.lang.ProcessBuilder.Redirect
import java.nio.file.{ Files, Paths }
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsArray, JsObject, JsValue, Json }
import play.api.mvc._
import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import javax.inject._
import com.typesafe.config.ConfigFactory
import org.apache.commons.io.FileUtils
import org.joda.time.DateTime
import play.api._
import play.api.data.{ Form, Forms }
import play.api.http.DefaultHttpRequestHandler
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.json.{ JsArray, JsObject, JsValue, Json }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.ws.{ StreamedResponse, WSClient }
import net.ceedubs.ficus.Ficus._

import scala.collection.{ SortedMap, mutable }
import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }

@Singleton
class Application @Inject() (
    val messagesApi: MessagesApi
) extends Controller with I18nSupport {

  def index = Action.async {
    implicit request =>
      {
        Future.successful(Ok(views.html.index()))
      }
  }
}

