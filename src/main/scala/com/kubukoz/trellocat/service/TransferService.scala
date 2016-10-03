package com.kubukoz.trellocat.service

import com.kubukoz.trellocat.domain.Github.{Card, ColumnWithCards, User}
import com.kubukoz.trellocat.domain.{Github, GithubToken, Trello, TrelloToken}

import scala.concurrent.{ExecutionContext, Future}

class TransferService(user: User, repo: Github.Repo, project: Github.Project)
                     (githubService: GithubService) {

  def transferColumns(columns: List[Trello.Column])
                     (implicit ghToken: GithubToken, trelloToken: TrelloToken,
                      ec: ExecutionContext): Future[List[Github.ColumnWithCards]] =
    Future.sequence {
      columns.map { trelloColumn =>
        for {
          createdColumn <- githubService.createColumn(user, project, repo, trelloColumn.toGithubStub)
          createdCards <- transferCards(trelloColumn.cards, createdColumn)
        } yield ColumnWithCards(createdColumn, createdCards)
      }
    }

  def transferCards(cards: List[Trello.Card], ghColumn: Github.Column)
                   (implicit token: GithubToken, ec: ExecutionContext): Future[List[Card]] =
    Future.sequence {
      cards.map { trelloCard =>
        githubService.createCard(user, project, repo, ghColumn, trelloCard.toGithubStub)
      }
    }
}
