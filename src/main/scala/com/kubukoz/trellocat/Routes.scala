package com.kubukoz.trellocat

import akka.http.scaladsl.server._
import com.kubukoz.trellocat.domain.Github.{Card, Project, ProjectStub, User}
import com.kubukoz.trellocat.domain.{Github, JsonSupport, Trello}
import com.kubukoz.trellocat.service.{GithubService, TrelloService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Routes extends Directives with JsonSupport {
  val trelloService: TrelloService
  val githubService: GithubService

  val routes = path("boards") {
    get {
      complete {
        trelloService.allBoards
      }
    }
  } ~ path("transfer") {
    parameters('from, 'to) { (trelloBoardId, repoName) =>
      post {
        complete {
          transferBoardWithId(trelloBoardId, repoName)
        }
      }
    }
  }

  def transferBoardWithId(boardId: String, repoName: String): Future[Project] = {
    val userF = githubService.getUser()

    for {
      board <- trelloService.boardById(boardId)
      columns <- trelloService.columnsOnBoard(boardId)
      user <- userF
      project <- githubService.createProject(user, repoName, board.toGithubStub)
      transferService = new TransferService(user, repoName, project)(githubService)
      _ <- transferService.transferColumns(columns)
    } yield project
  }
}

class TransferService(user: User, repoName: String, project: Github.Project)
                     (githubService: GithubService) {

  def transferColumns(columns: List[Trello.Column]): Future[List[Github.Card]] =
    Future.sequence {
      columns.map { trelloColumn =>
        githubService.createColumn(user, project, repoName, trelloColumn.toGithubStub).flatMap {
          transferCards(trelloColumn.cards, _)
        }
      }
    }.map(_.flatten)

  def transferCards(cards: List[Trello.Card], ghColumn: Github.Column): Future[List[Card]] =
    Future.sequence {
      cards.map { trelloCard =>
        githubService.createCard(user, project, repoName, ghColumn, trelloCard.toGithubStub)
      }
    }
}