package controllers

import java.util.concurrent.TimeUnit

import akka.actor.Props
import akka.pattern.ask
import models.{Quit, Talk, Join, ChatRoom}
import play.api.libs.concurrent.Akka
import play.api.libs.iteratee.{Enumeratee, Iteratee, Enumerator}
import play.api.libs.json.JsValue
import play.api.mvc.{WebSocket, Controller}
import akka.util.Timeout

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import scala.concurrent.Future

// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection



import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits._
/**
 * Created by madalien on 31/05/15.
 */
object ChatCtrl extends Controller with MongoController{




  def collection: JSONCollection = db.collection[JSONCollection]("livetchat")




  def filter(eventRoom: String) = Enumeratee.filter[JsValue]{  json: JsValue =>
    eventRoom == (json\"eventRoom").as[String]
  }

  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  lazy val chatroomActor = Akka.system.actorOf(Props(new ChatRoom(collection)))
  def chat(eventRoom: String, username: String) = WebSocket.async[JsValue] {
    request => {
      (chatroomActor ? Join(username, eventRoom)) map {
        // grab the Enumerator from ChatRoom:
        case out: Enumerator[JsValue] =>
          val in = Iteratee.foreach[JsValue] {
            event => chatroomActor ! Talk(username, (event \ "text").as[String], eventRoom)
          }.map { _ =>
            chatroomActor ! Quit(username, eventRoom)
          }
          (in, out &> filter(eventRoom))
      }
    }
  }
}