package models.actors

/**
 * Created by Mamadou Coulibaly on 09/12/15.
 */
import akka.actor.{ Actor, ActorRef, ActorSystem, Props }
import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json.JsValue

class VideoRoomActor(userID: String, event: String, outChannel: ActorRef, videoRoomSupervisor: ActorRef) extends Actor {

  val log = Logger("WRTC." + this.getClass.getSimpleName);

  override def preStart() = {
    videoRoomSupervisor ! JoinVideoRoom(userID, event, outChannel)
  }

  override def postStop() = {
    log.debug(s"KILL videoRoom actor -> [ $userID ]")
    videoRoomSupervisor ! QuitVideoRoom(userID, event, outChannel)
  }

  def receive = {
    case json: JsValue => {
      val sendTo = (json \ "userID").asOpt[String].getOrElse("")
      val msg = (json \ "text").asOpt[String].getOrElse("")
      videoRoomSupervisor ! TalkVideoRoom(sendTo, msg, event)
    }
  }

}

class VideoRoomActorFactory @Inject() (actorSystem: ActorSystem) {
  lazy val videoRoomSupervisor = actorSystem.actorOf(Props[VideoRoomSupervisor])

  def props(userID: String, event: String, outChannel: ActorRef) = {
    Props(new VideoRoomActor(userID, event, outChannel, videoRoomSupervisor))
  }
}
