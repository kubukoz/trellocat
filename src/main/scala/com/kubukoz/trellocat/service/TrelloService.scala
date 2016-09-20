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
  def allBoards(implicit ap: AuthParams, ec: ExecutionContext): Future[List[Trello.Board]]

  def columnsOnBoard(board: Trello.Board)(implicit ap: AuthParams, ec: ExecutionContext): Future[List[Trello.Column]]
}

/**
  * Implements [[TrelloService]] with a HTTP client.
  **/
class RealTrelloService(implicit api: ApiClient, mat: Materializer) extends TrelloService with JsonSupport {
  override def allBoards(implicit auth: AuthParams, ec: ExecutionContext): Future[List[Trello.Board]] = {
    api[List[Trello.Board]](HttpRequest(uri = Uri(boardsUrl).withAuthQuery(Query.Empty)))
  }

  override def columnsOnBoard(board: Trello.Board)(implicit ap: AuthParams, ec: ExecutionContext): Future[List[Trello.Column]] =
    api[List[Trello.Column]](HttpRequest(uri = Uri(s"$baseUrl/boards/${board.id}/lists").withAuthQuery(Query("cards" -> "all"))))
}

object RealTrelloService {
  val baseUrl = "https://api.trello.com/1/"
  val boardsUrl = s"$baseUrl/members/me/boards"
}