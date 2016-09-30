package com.kubukoz.trellocat.service

import com.kubukoz.trellocat.domain.Github.{Card, User}
import com.kubukoz.trellocat.domain.{Github, Trello}

import scala.concurrent.{ExecutionContext, Future}

class TransferService(user: User, repo: Github.Repo, project: Github.Project)
                     (githubService: GithubService) {

  def transferColumns(columns: List[Trello.Column])(implicit ec: ExecutionContext): Future[List[Github.Card]] =
    Future.sequence {
      columns.map { trelloColumn =>
        githubService.createColumn(user, project, repo, trelloColumn.toGithubStub).flatMap {
          transferCards(trelloColumn.cards, _)
        }
      }
    }.map(_.flatten)

  def transferCards(cards: List[Trello.Card], toColumn: Github.Column)(implicit ec: ExecutionContext): Future[List[Card]] =
    Future.sequence {
      cards.map { trelloCard =>
        githubService.createCard(user, project, repo, toColumn, trelloCard.toGithubStub)
      }
    }
}
