package controllers;

import org.joda.time.DateTime;
import play.*;
import play.mvc.*;

import reactivemongo.api.Collection;
import reactivemongo.bson.BSONObjectID;
import scala.Option;
import views.html.*;

public class Application extends Controller  {
    public static Result index() {
        return ok(index.render());
    }
}
