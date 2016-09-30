package com.kubukoz.trellocat

import akka.http.scaladsl.server.Directives._
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain.{Github, JsonSupport, Trello}
import com.kubukoz.trellocat.service.{GithubService, TrelloService}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Routes extends JsonSupport {
  val trelloService: TrelloService
  val githubService: GithubService

  val routes = path("boards") {
    get {
      complete {
        trelloService.allBoards
      }
    }
  } ~ path("repos") {
    complete {
      githubService.allRepos
    }
  } ~ path("transfer") {
    parameters('from, 'to) { (trelloBoardId, repo) =>
      post {
        complete {
          transferBoardWithId(trelloBoardId, Repo(repo))
        }
      }
    }
  }

  def transferBoardWithId(boardId: String, repo: Repo): Future[ProjectWithColumns] = {
    val userF = githubService.getUser()

    for {
      board <- trelloService.boardById(boardId)
      columns <- trelloService.columnsOnBoard(boardId)
      user <- userF
      project <- githubService.createProject(user, repo, board.toGithubStub)
      transferService = new TransferService(user, repo, project)(githubService)
      columns <- transferService.transferColumns(columns)
    } yield ProjectWithColumns(project, columns)
  }
}

class TransferService(user: User, repo: Repo, project: Github.Project)
                     (githubService: GithubService) {

  def transferColumns(columns: List[Trello.Column]): Future[List[Github.ColumnWithCards]] =
    Future.sequence {
      columns.map { trelloColumn =>
        for {
          createdColumn <- githubService.createColumn(user, project, repo, trelloColumn.toGithubStub)
          createdCards <- transferCards(trelloColumn.cards, createdColumn)
        } yield ColumnWithCards(createdColumn, createdCards)
      }
    }

  def transferCards(cards: List[Trello.Card], ghColumn: Github.Column): Future[List[Card]] =
    Future.sequence {
      cards.map { trelloCard =>
        githubService.createCard(user, project, repo, ghColumn, trelloCard.toGithubStub)
      }
    }
}