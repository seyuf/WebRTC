package models.actors

/**
 * Created by Mamadou Coulibaly on 10/12/15.
 */
import akka.actor._
import com.google.inject.Inject
import play.api.Logger
import play.api.libs.json.JsValue
import play.modules.reactivemongo.ReactiveMongoApi

/*
case class JoinChat(username: String, event: String)
case class QuitChat(username: String, event: String)
case class TalkChat(username: String, text: String, event: String)

case class JoinChat(username:String, event: String, userActor: ActorRef)
case class QuitChat(username: String, event: String, userActor: ActorRef)
case class TalkChat(username: String, text: String, event: String)
*/

class ChatActor(username: String, event: String, outChannel: ActorRef, roomSupervisor: ActorRef) extends Actor {

  val log = Logger("WRTC." + this.getClass.getSimpleName);

  override def preStart() = {
    roomSupervisor ! JoinChat(username, event, outChannel)
  }

  override def postStop() = {
    log.debug(s"KILL chat actor -> [ $username ]")
    roomSupervisor ! QuitChat(username, event, outChannel)
  }

  def receive = {
    case json: JsValue => {
      roomSupervisor ! TalkChat(username, (json \ "text").get, event)
    }
  }

}

class ChatActorFactory @Inject() (actorSystem: ActorSystem, reactiveMongoApi: ReactiveMongoApi) {
  lazy val roomSupervisor = actorSystem.actorOf(Props(new ChatSupervisor(reactiveMongoApi)))

  def props(username: String, event: String, outChannel: ActorRef) = {
    Props(new ChatActor(username, event, outChannel, roomSupervisor))
  }
}

