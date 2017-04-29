package models

import akka.actor.Actor
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{JsArray, JsString, Json, JsValue}

/**
 * Created by madalien on 31/05/15.
 */


case class Join(username: String, eventRoom: String)
case class Quit(username: String, eventRoom: String)
case class Talk(username: String, text: String, eventRoom: String)
class ChatRoom extends Actor {
  var members = Set.empty[String]
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
  val chatBot = "LeBot"
  def receive = {
    case Join(username, eventRoom) => { members = members + username
      broadcastMessage(chatBot, s"$username has joined", eventRoom)
      sender ! chatEnumerator
    }
    case Quit(username, eventRoom) => {
      broadcastMessage(chatBot, s"$username has left", eventRoom)
      members = members - username
    }
    case Talk(username, text, eventRoom) => broadcastMessage(username, text, eventRoom)
  }

  def broadcastMessage(user: String, text: String, eventRoom: String): Unit = {
    val msg = Json.obj("user" -> JsString(user),
      "message" -> JsString(text),
      "members" -> JsArray(members.toList.map(JsString)),
      "eventRoom" -> eventRoom
    )
    chatChannel.push(msg)
  }
}