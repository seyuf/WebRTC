package models
/*
import akka.actor.{ Actor, ReceiveTimeout }
import akka.actor._
import com.google.common.collect.{ EvictingQueue, ImmutableList }
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.iteratee.Concurrent
import play.api.libs.json.{ JsArray, JsObject, JsString, JsValue, Json }
import play.api.libs.json.Reads._

import scala.collection.mutable
import scala.concurrent.duration._
import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._

import javax.annotation.concurrent.Immutable
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.json.collection.JSONCollection

/**
 * Created by madalien on 31/05/15.
 */

case class Join(username: String, eventRoom: String)
case class Quit(username: String, eventRoom: String)
case class Talk(username: String, text: String, eventRoom: String)
class ChatRoom(tchatCol: JSONCollection) extends Actor {
  var members = Set.empty[String]

  val logger = Logger("WBS." + this.getClass.getSimpleName)
  var isTextCollectionEmpty = true
  var textCollection: EvictingQueue[JsObject] = EvictingQueue.create(300)
  val (chatEnumerator, chatChannel) = Concurrent.broadcast[JsValue]
  val chatBot = "LeBot"
  context.setReceiveTimeout(600 seconds)

  def receive = {
    case Join(username, eventRoom) => {
      members = members + username
      broadcastMessage(chatBot, s"$username has joined", eventRoom)
      sender ! chatEnumerator
    }
    case Quit(username, eventRoom) => {
      members = members - username
      broadcastMessage(chatBot, s"$username has left", eventRoom)
      //members = members - username
    }
    case Talk(username, text, eventRoom) => broadcastMessage(username, text, eventRoom)

    /*
  case ReceiveTimeout =>{
    log.debug(s"TIMEOUT tchat of [ $username ]")
    context.setReceiveTimeout(Duration.Undefined)
    roomSupervisor ! QuitChat(username,  event, outChannel)
  }
    */

  }

  def broadcastMessage(user: String, text: String, eventRoom: String): Unit = {

    import reactivemongo.bson.BSONObjectID
    import play.modules.reactivemongo.json.BSONFormats._
    import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

    import play.api.libs.json._
    if (isTextCollectionEmpty) {
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

    if (user != "LeBot" && text.nonEmpty) {
      val curJsMsg = Json.obj(
        "user" -> user,
        "text" -> text,
        "timeStamp" -> DateTime.now().getMillis
      )
      textCollection.add(
        curJsMsg
      )

      tchatCol.insert(curJsMsg).map(lastError => logger.debug(s"Insert logs: $lastError"))

    }
    var msg: JsObject = Json.obj(
      "user" -> JsString(user),
      "message" -> JsString(text),
      "members" -> JsArray(members.toList.map(JsString)),
      "eventRoom" -> eventRoom,
      "time" -> DateTime.now().getMillis
    )

    var curJsArr = Json.arr()
    textCollection.toArray().foreach {
      case elem: JsObject => curJsArr = curJsArr :+ elem
    }

    msg ++= Json.obj(
      "collection" -> curJsArr
    )

    chatChannel.push(msg)
  }
}
*/
