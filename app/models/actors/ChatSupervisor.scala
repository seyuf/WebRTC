package models.actors

/**
 * Created by Coulibaly Mamadou on 04/01/16.
 */

import akka.actor._
import com.google.common.collect.EvictingQueue
import play.api.Logger
import play.api.libs.json.{ JsArray, JsObject, JsString, JsValue, Json }

import scala.collection.mutable
import com.google.inject.Inject
import org.joda.time.DateTime
import play.api.libs.concurrent.Execution.Implicits._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.QueryOpts
import reactivemongo.api.commands.{ UpdateWriteResult, WriteResult }
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import scala.concurrent.duration._

import scala.concurrent.{ Await, Future }

case class JoinChat(username: String, event: String, userActor: ActorRef)
case class QuitChat(username: String, event: String, userActor: ActorRef)
case class TalkChat(username: String, text: JsValue, event: String)

class ChatSupervisor(reactiveMongoApi: ReactiveMongoApi) extends Actor {
  import play.modules.reactivemongo.json._

  var members = Set.empty[String]
  val log = Logger("WRTC." + this.getClass.getSimpleName);
  val actorRefsByevents: mutable.Map[String, mutable.Set[ActorRef]] = mutable.Map[String, mutable.Set[ActorRef]]()

  var isTextCollectionEmpty = true
  var textCollection: EvictingQueue[JsObject] = EvictingQueue.create(300)
  val chatBot = "LeBot"

  //def collection: Future[JSONCollection] = reactiveMongoApi.database.map(_.collection("livechat"))
  val tchatCol: JSONCollection = reactiveMongoApi.db.collection[JSONCollection]("livetchat")

  //context.setReceiveTimeout(1000 milliseconds)
  override def receive: Receive = {
    case JoinChat(username, event, userActor) => {
      actorRefsByevents(event) = actorRefsByevents.getOrElse(event, mutable.Set(userActor))
      actorRefsByevents(event) += userActor
      members = members + username
      log.debug(s"SUBSCRIBED TO TCHAT [ $username ]")
      broadcastMessage("LeBot", Json.toJson(s"${username} has joined this channel"), event, actorRefsByevents(event).size)
    }

    case TalkChat(username, text, event) => {
      broadcastMessage(username, text, event, actorRefsByevents(event).size)
    }

    case QuitChat(username, event, userActor) => {
      log.debug(s"HAS LEFT THE CHAT [$username]")
      log.debug(s"OLD NB USERS [ ${actorRefsByevents.size} ]")
      //if( actorRefsByevents.size > 0 ) {
      //actorRefsByevents(event) -= userActor

      members = members - username
      val remainingUsers = actorRefsByevents.get(event) match {
        case Some(users) => {
          users -= userActor
          users.size
        }
        case None => 0
      }
      //}
      if (remainingUsers > 0)
        broadcastMessage("LeBot", Json.toJson(s" $username has left the conversation"), event, remainingUsers)
      else {
        actorRefsByevents.remove(event)
        broadcastMessage("LeBot", Json.toJson(s" ${username} has left the conversation"), event, 0)
      }
      log.debug(s" NEW NB USERS [${actorRefsByevents.size}]\n\n")
      userActor ! PoisonPill
    }
  }

  def broadcastMessage(userID: String, text: JsValue, event: String, connected: Int): Unit = {

    /*
    val msg = Json.obj(
      "user" -> JsString(userID),
      "text" -> Json.toJson(text),
      "eventRoom" -> event,
      "connected" -> connected,
      "members" -> JsArray(members.toList.map(JsString)),
      "time" -> DateTime.now().getMillis
    )
    */
    log.debug(s"connected [$connected]")

    import play.api.libs.json._

    if (isTextCollectionEmpty) {

      import play.modules.reactivemongo.json._

      val resList: Future[List[JsObject]] = tchatCol
        .find(Json.obj())
        .sort(Json.obj("$natural" -> 1))
        .cursor[JsObject].collect[List]()

      // val curList = resList.map {
      //   curTexts =>

      // }

      val curTestList = Await.result(resList, 10 seconds)
      curTestList.take(1000).foreach(text => textCollection.add(text))

      isTextCollectionEmpty = false
      System.out.println(resList.toString)

    }

    val isEmptyMessage = Json.toJson(text).toString().isEmpty
    if (userID != "LeBot" && !isEmptyMessage) {
      val curJsMsg = Json.obj(
        "user" -> userID,
        "text" -> Json.toJson(text).toString(),
        "timeStamp" -> DateTime.now().getMillis
      )
      textCollection.add(
        curJsMsg
      )

      import play.modules.reactivemongo.json._

      tchatCol.insert(curJsMsg).map(lastError => log.debug(s"Insert logs: $lastError"))

    }
    var msg: JsObject = Json.obj(
      "user" -> JsString(userID),
      "message" -> Json.toJson(text).toString(),
      "members" -> JsArray(members.toList.map(JsString)),
      "eventRoom" -> event,
      "time" -> DateTime.now().getMillis
    )

    var curJsArr = Json.arr()
    textCollection.toArray().foreach {
      case elem: JsObject => curJsArr = curJsArr :+ elem
    }

    msg ++= Json.obj(
      "collection" -> curJsArr
    )

    if (connected != 0) {
      actorRefsByevents(event).foreach {
        member => member ! msg
      }
    }
  }

}
