package controllers

import play.api.mvc._

class Application extends Controller {

  def index = Action { request =>
    Redirect("/ws/send", request.queryString, MOVED_PERMANENTLY)
  }

  def help = Action {
    Ok(views.html.index("Your new application is ready."))
  }

}