import filters.CORSFilter
import play.api._
import play.api.mvc.WithFilters


import play.api.Play.current
import play.modules.reactivemongo._


// Reactive Mongo imports
import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection


import scala.concurrent.{Await, Future}
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by madalien on 02/07/15.
 */

object Global extends WithFilters(CORSFilter()) with GlobalSettings  {
  //...if you need some init


  val logger = Logger("WBS." + this.getClass.getSimpleName)
  import play.modules.reactivemongo.json.collection.JSONCollection
  import play.modules.reactivemongo.json.BSONFormats._
  import play.modules.reactivemongo.json._

  override def onStart(app: Application) {
    Logger.info("Application has started")


    import scala.concurrent.duration._

    //val collection = db.collection[JSONCollection]("tchatter")

    def db = ReactiveMongoPlugin.db

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

}