import filters.CORSFilter
import play.api.GlobalSettings
import play.api.mvc.WithFilters

/**
 * Created by madalien on 02/07/15.
 */

object Global extends WithFilters(CORSFilter()) with GlobalSettings {
  //...if you need some init
}
