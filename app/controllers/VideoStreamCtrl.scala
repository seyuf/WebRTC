package controllers

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.actors.{ ChatActor, ChatActorFactory, VideoRoomActorFactory }
import play.api.Logger
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._

import scala.concurrent.Future

class VideoStreamCtrl @Inject() (
    val messagesApi: MessagesApi,
    implicit val mat: Materializer,
    implicit val actorSystem: ActorSystem,
    val videoRoomActorFactory: VideoRoomActorFactory
) extends Controller with I18nSupport {

  val log = Logger("WTRC." + this.getClass.getSimpleName);

  def stream(eventName: String, username: String, token: Option[String]) = WebSocket.accept[JsValue, JsValue] {
    request =>
      {
        //log.debug(s"new query string:  ${request.getQueryString("X-Auth-Token")}")
        //val tmpRequest = request.copy(headers = new Headers(Seq("X-Auth-Token" -> token)))
        //implicit val req = Request(tmpRequest, AnyContentAsEmpty)
        ActorFlow.actorRef(videoRoomActorFactory.props(username, eventName, _))
      }
  }
}

