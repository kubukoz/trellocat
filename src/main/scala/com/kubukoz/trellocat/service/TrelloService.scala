package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.domain.Trello.{Board, Column}
import com.kubukoz.trellocat.domain.{AuthParams, JsonSupport, Trello}
import com.kubukoz.trellocat.service.RealTrelloService._

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the Trello API.
  **/
trait TrelloService {
  def allBoards(implicit ec: ExecutionContext): Future[List[Trello.Board]]

  def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Trello.Column]]

  def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Trello.Board]
}

/**
  * Implements [[TrelloService]] with a HTTP client.
  **/
class RealTrelloService(ap: AuthParams)(implicit api: ApiClient, mat: Materializer) extends TrelloService with JsonSupport {
  implicit val app = ap
  override def allBoards(implicit ec: ExecutionContext): Future[List[Trello.Board]] =
    api[List[Trello.Board]](HttpRequest(uri = Uri(boardsUrl).withAuthQuery(Query.Empty)))

  override def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Trello.Column]] =
    api[List[Trello.Column]](HttpRequest(uri = Uri(s"$baseUrl/boards/$boardId/lists").withAuthQuery(Query("cards" -> "all"))))

  override def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Trello.Board] =
    api[Trello.Board](HttpRequest(uri = Uri(s"$baseUrl/boards/$boardId").withAuthQuery(Query.Empty)))
}

object RealTrelloService {
  val baseUrl = "https://api.trello.com/1"
  val boardsUrl = s"$baseUrl/members/me/boards"
}

trait MockTrelloService extends TrelloService {
  override def allBoards(implicit ec: ExecutionContext): Future[List[Board]] =
    Future.failed(new Exception("Stub!"))

  override def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Column]] =
    Future.failed(new Exception("Stub!"))

  override def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Board] =
    Future.failed(new Exception("Stub!"))
}