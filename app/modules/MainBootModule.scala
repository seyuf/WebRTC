package modules

import javax.inject.Inject
import scala.concurrent.{ ExecutionContext, Future }
import play.api.mvc.{ Action, Controller, WebSocket }
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{ JsObject, JsValue, Json }
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import reactivemongo.api.{ Cursor, QueryOpts }
import play.modules.reactivemongo.{ MongoController, ReactiveMongoApi, ReactiveMongoComponents }

import java.util.UUID
import com.google.inject.AbstractModule
import models.actors.VideoRoomActorFactory
import models.actors.ChatActorFactory
import net.codingwell.scalaguice.ScalaModule
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.api._
import reactivemongo.bson.BSONInteger
import reactivemongo.play.json.collection.JSONCollection
import play.api.libs.concurrent.Execution.Implicits._
import net.codingwell.scalaguice.ScalaModule
import org.joda.time.DateTime
import play.api.Logger
import play.api.libs.concurrent.AkkaGuiceSupport
import reactivemongo.api.indexes.{ Index, IndexType }
import reactivemongo.api._
import reactivemongo.bson.BSONInteger

import java.util.UUID
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ Await, Future }

/**
 * Provides Guice bindings for the persistence module.
 */

class MainBootModule extends AbstractModule with ScalaModule with AkkaGuiceSupport {

  val logger = Logger("WRTC." + this.getClass.getSimpleName);
  /**
   * Configures the module.
   */
  def configure() {

    bind(classOf[ChatActorFactory]).asEagerSingleton()
    bind(classOf[VideoRoomActorFactory]).asEagerSingleton()

    applicationBootstrap()
  }

  def applicationBootstrap(): Unit = {
    import com.typesafe.config.ConfigFactory

    val config = ConfigFactory.load
    val driver = new MongoDriver
    val uri = MongoConnection.parseURI(config.getString("mongodb.uri")).get
    val connection = driver.connection(uri)

    /*val config = ConfigFactory.load
    val driver = new MongoDriver
    val connection = driver.connection(
      config.getStringList("mongodb.servers"),
      MongoConnectionOptions(),
      Seq()
    )*/
    connection.database(config.getString("mongodb.db"), FailoverStrategy.default).map(
      db => {

        // BSON-JSON conversions
        import play.modules.reactivemongo.json._, ImplicitBSONHandlers._
        db.collection[JSONCollection]("livetchat")

        def curCollection: JSONCollection = db.collection[JSONCollection]("livetchat")
        val futureCollection: Future[JSONCollection] = curCollection.stats().flatMap {
          case stats if !stats.capped =>
            // the collection is not capped, so we convert it
            logger.info("converting to capped")
            curCollection.convertToCapped(5242880, Some(5000))
          case _ => Future(curCollection)
        }.recover {
          // the collection does not exist, so we create it
          case _ =>
            logger.info("creating capped collection...")
            curCollection.createCapped(5242880, Some(5000))
        }.map { _ =>
          logger.info("the capped collection is available")
          curCollection
        }

        Await.result(futureCollection, 5 seconds)

      }
    )

  }

}
