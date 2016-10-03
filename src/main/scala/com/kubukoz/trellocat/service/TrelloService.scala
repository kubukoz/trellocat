package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.config.TrelloConfig
import com.kubukoz.trellocat.domain._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the Trello API.
  **/
trait TrelloService {
  def allBoards(implicit token: TrelloToken, ec: ExecutionContext): Future[List[Trello.Board]]

  def columnsOnBoard(boardId: String)(implicit token: TrelloToken, ec: ExecutionContext): Future[List[Trello.Column]]

  def boardById(boardId: String)(implicit token: TrelloToken, ec: ExecutionContext): Future[Trello.Board]
}

/**
  * Implements [[TrelloService]] with a HTTP client.
  **/
class RealTrelloService(config: TrelloConfig)(implicit api: ApiClient, mat: Materializer) extends TrelloService with JsonSupport {
  implicit val ap = AuthParams(Query("key" -> config.apiKey))

  val baseUrl = "https://api.trello.com/1"
  val boardsUrl = s"$baseUrl/members/me/boards"

  override def allBoards(implicit token: TrelloToken, ec: ExecutionContext): Future[List[Trello.Board]] =
    api[List[Trello.Board]](HttpRequest(uri = Uri(boardsUrl).withAuthQuery(Query.Empty)))

  override def columnsOnBoard(boardId: String)
                             (implicit token: TrelloToken, ec: ExecutionContext): Future[List[Trello.Column]] =
    api[List[Trello.Column]](HttpRequest(uri = Uri(s"$baseUrl/boards/$boardId/lists").withAuthQuery(Query("cards" -> "all"))))

  override def boardById(boardId: String)
                        (implicit token: TrelloToken, ec: ExecutionContext): Future[Trello.Board] =
    api[Trello.Board](HttpRequest(uri = Uri(s"$baseUrl/boards/$boardId").withAuthQuery(Query.Empty)))
}
