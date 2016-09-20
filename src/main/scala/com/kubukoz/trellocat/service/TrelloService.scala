package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.domain.{AuthParams, JsonSupport, Trello}
import com.kubukoz.trellocat.service.RealTrelloService._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the Trello API.
  **/
trait TrelloService {
  def allBoards(implicit ap: AuthParams): Future[List[Trello.Board]]
}

/**
  * Implements [[TrelloService]] with a HTTP client.
  **/
class RealTrelloService(implicit api: ApiClient, mat: Materializer, ec: ExecutionContext) extends TrelloService with JsonSupport {
  override def allBoards(implicit auth: AuthParams): Future[List[Trello.Board]] = {
    api[List[Trello.Board]](HttpRequest(uri = Uri(boardsUrl).withAuthQuery(Query.Empty)))
  }
}

object RealTrelloService {
  val baseUrl = "https://api.trello.com/1/"
  val boardsUrl = s"$baseUrl/members/me/boards"
}