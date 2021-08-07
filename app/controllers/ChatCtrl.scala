package controllers

import javax.inject.Inject
import akka.actor.ActorSystem
import akka.stream.Materializer
import models.actors.{ ChatActor, ChatActorFactory }
import play.api.Logger
import play.api.i18n.{ I18nSupport, MessagesApi }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.libs.streams.ActorFlow
import play.api.mvc._
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }

import scala.concurrent.Future

import javax.inject.Inject

import scala.concurrent.{ ExecutionContext, Future }

import play.api.mvc.{ Action, Controller, WebSocket }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ Json, JsObject, JsValue }
import play.api.libs.iteratee.{ Enumerator, Iteratee }

import reactivemongo.api.{ Cursor, QueryOpts }
import play.modules.reactivemongo.{
  MongoController,
  ReactiveMongoApi,
  ReactiveMongoComponents
}
import play.modules.reactivemongo.json.collection.JSONCollection

class ChatCtrl @Inject() (
    val messagesApi: MessagesApi,
    implicit val mat: Materializer,
    implicit val actorSystem: ActorSystem,
    val chatActorFactory: ChatActorFactory,
    val reactiveMongoApi: ReactiveMongoApi
) extends Controller with I18nSupport with MongoController with ReactiveMongoComponents {

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
  val log = Logger("WTRC." + this.getClass.getSimpleName);

  // BSON-JSON conversions
  import play.modules.reactivemongo.json._, ImplicitBSONHandlers._

  // let's be sure that the collections exists and is capped
  val futureCollection: Future[JSONCollection] = {
    val db = reactiveMongoApi.db
    val collection = db.collection[JSONCollection]("acappedcollection")

    collection.stats().flatMap {
      case stats if !stats.capped =>
        // the collection is not capped, so we convert it
        println("converting to capped")
        collection.convertToCapped(1024 * 1024, None)
      case _ => Future(collection)
    }.recover {
      // the collection does not exist, so we create it
      case _ =>
        println("creating capped collection...")
        collection.createCapped(1024 * 1024, None)
    }.map { _ =>
      println("the capped collection is available")
      collection
    }
  }

  def chat(eventName: String, username: String, token: Option[String]) = WebSocket.accept[JsValue, JsValue] {
    request =>
      {
        //log.debug(s"new query string:  ${request.getQueryString("X-Auth-Token")}")
        //val tmpRequest = request.copy(headers = new Headers(Seq("X-Auth-Token" -> token)))
        //implicit val req = Request(tmpRequest, AnyContentAsEmpty)
        ActorFlow.actorRef(chatActorFactory.props(username, eventName, _))
      }
  }
}
