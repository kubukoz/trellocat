package com.kubukoz.trellocat.service

import com.kubukoz.trellocat.domain.Trello.{Board, Column}

import scala.concurrent.{ExecutionContext, Future}

//noinspection NotImplementedCode
class MockTrelloService extends TrelloService {
  override def allBoards(implicit ec: ExecutionContext): Future[List[Board]] = ???

  override def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Column]] = ???

  override def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Board] = ???
}
