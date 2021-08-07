package models.actors

/**
 * Created by COULIBALY Mamadou on 06/01/16.
 */

import akka.actor._
import play.api.Logger
import play.api.libs.json.{ JsString, Json }

import scala.collection.mutable

case class JoinVideoRoom(userID: String, event: String, userActor: ActorRef)
case class QuitVideoRoom(userID: String, event: String, userActor: ActorRef)
case class TalkVideoRoom(userID: String, text: String, event: String)

class VideoRoomSupervisor extends Actor {

  val actorRefsByUserID: mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()
  val log = Logger("WRTC." + this.getClass.getSimpleName);
  override def receive: Receive = {
    case JoinVideoRoom(userID, event, userActor) => {
      actorRefsByUserID(userID) = actorRefsByUserID.getOrElse(userID, userActor)
      log.debug(s"SUBSCRIBED TO VIDEOROOM [ $userID ]  NB USERS = [ ${actorRefsByUserID.size} ]")
      log.debug(s"SUBSCRIBED TO VIDEOROOM USERS = [ ${actorRefsByUserID} ]")
      //actorRefsByUserID.get /// += userActor
      broadcastMessage(userID, s"has joined this channel", event)
    }

    case TalkVideoRoom(userID, text, event) => {
      log.debug(s"VIDEOROOM [ $userID ]  --> $text")
      broadcastMessage(userID, text, event)
    }

    case QuitVideoRoom(userID, event, userActor) => {
      log.debug(s" [$userID] HAS LEFT THE VIDEOROOM \n")
      val tmpActor = actorRefsByUserID.remove(userID).getOrElse(null)
      if ((tmpActor != null) && tmpActor.equals(userActor)) {
        log.debug("VIDEOROOM POISONPILLER ACTORS ARE EQUAL")
        tmpActor ! PoisonPill
      } else {
        log.debug("VIDEOROOM POISONPILLER ACTORS ARE EQUAL")
        userActor ! PoisonPill
      }
    }
  }

  def broadcastMessage(userID: String, text: String, event: String): Unit = {
    val msg = Json.obj(
      "username" -> JsString(userID),
      "message" -> JsString(text),
      "event" -> event
    )
    actorRefsByUserID.get(userID) match {
      case Some(user) => {
        log.debug(s"\n msg $msg to $userID ref $user")
        user ! msg
      }
      case None => log.error(s"NO ACTOR REF [$userID] videoRoom")
    }
  }

}
