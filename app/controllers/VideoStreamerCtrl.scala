package controllers

/**
 * Created by madalien on 07/07/15.
 */

import java.util.concurrent.TimeUnit
import akka.actor.Props
import akka.pattern.ask
import models.{QuitVideo, TalkVideo, JoinVideo, VideoRoom}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import play.api.libs.json.{JsString, JsValue}
import play.api.mvc.{WebSocket, Controller}
import akka.util.Timeout
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

object VideoStreamerCtrl extends Controller{

  implicit val timeout = Timeout(1, TimeUnit.SECONDS)
  def filter(eventRoom: String) = Enumeratee.filter[JsValue]{  json: JsValue =>
    eventRoom == (json\"eventRoom").as[String]
  }

  lazy val chatroomActor = Akka.system.actorOf(Props[VideoRoom])
  def chat(eventRoom: String, username: String) = WebSocket.async[JsValue] {
     request => (chatroomActor ? JoinVideo(username, eventRoom, JsString("joinning"))) map {
      // grab the Enumerator from ChatRoom:
      case out: Enumerator[JsValue] =>
        val in = Iteratee.foreach[JsValue] {
          event => chatroomActor ! TalkVideo(username , (event \ "text").as[String], eventRoom, event)
        }.map { _ =>
          chatroomActor ! QuitVideo(username, eventRoom, JsString("leaving"))
        }
        (in, out &> filter(eventRoom))
    }
  }
}
