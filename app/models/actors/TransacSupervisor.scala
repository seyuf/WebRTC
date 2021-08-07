package models.actors

/**
 * Created by COULIBALY Mamadou on 06/01/16.
 */

import akka.actor._
import play.api.Logger
import play.api.libs.json.{ JsString, Json }

import scala.collection.mutable

case class Join(userID: String, event: String, userActor: ActorRef)
case class Quit(userID: String, event: String, userActor: ActorRef)
case class Talk(userID: String, text: String, event: String)

class TransacSupervisor extends Actor {

  val actorRefsByUserID: mutable.Map[String, ActorRef] = mutable.Map[String, ActorRef]()
  val log = Logger("WRTC." + this.getClass.getSimpleName);
  override def receive: Receive = {
    case Join(userID, event, userActor) => {
      actorRefsByUserID(userID) = actorRefsByUserID.getOrElse(userID, userActor)
      log.debug(s"SUBSCRIBED TO TRANSAC [ $userID ]  NB USERS = [ ${actorRefsByUserID.size} ]")
      log.debug(s"SUBSCRIBED TO TRANSAC USERS = [ ${actorRefsByUserID} ]")
      //actorRefsByUserID.get /// += userActor
      broadcastMessage(userID, s"has joined this channel", event)
    }

    case Talk(userID, text, event) => {
      log.debug(s"TRANSAC [ $userID ]  --> $text")
      broadcastMessage(userID, text, event)
    }

    case Quit(userID, event, userActor) => {
      log.debug(s" [$userID] HAS LEFT THE TRANSAC \n")
      val tmpActor = actorRefsByUserID.remove(userID).getOrElse(null)
      if ((tmpActor != null) && tmpActor.equals(userActor)) {
        log.debug("TRANSAC POISONPILLER ACTORS ARE EQUAL")
        tmpActor ! PoisonPill
      } else {
        log.debug("TRANSAC POISONPILLER ACTORS ARE EQUAL")
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
      case None => log.error(s"NO ACTOR REF [$userID] transac")
    }
  }

}
