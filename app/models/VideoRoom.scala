package models

/**
 * Created by madalien on 08/07/15.
 */
import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{ JsArray, JsString, Json, JsValue }

/**
 * Created by madalien on 31/05/15.
 */

case class JoinVideo(username: String, eventRoom: String, data: JsValue)
case class QuitVideo(username: String, eventRoom: String, data: JsValue)
case class TalkVideo(username: String, text: String, eventRoom: String, data: JsValue)
class VideoRoom extends Actor {
  var members = Set.empty[String]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
  val chatBot = "LeBot"
  def receive = {
    case JoinVideo(username, eventRoom, data) => {
      members = members + username
      broadcastMessage(chatBot, s"$username has joined", eventRoom, data)
      sender ! chatEnumerator
    }
    case QuitVideo(username, eventRoom, data) => {
      broadcastMessage(chatBot, s"$username has left", eventRoom, data)
      members = members - username
    }
    case TalkVideo(username, text, eventRoom, data) => broadcastMessage(username, text, eventRoom, data)
  }

  def broadcastMessage(user: String, text: String, eventRoom: String, data: JsValue): Unit = {
    val msg = Json.obj(
      "user" -> JsString(user),
      "message" -> JsString(text),
      "members" -> JsArray(members.toList.map(JsString)),
      "eventRoom" -> eventRoom,
      "data" -> data
    )
    chatChannel.push(msg)
  }
}
