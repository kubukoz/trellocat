package com.kubukoz.trellocat

import akka.http.scaladsl.server.Directives._
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain.{Github, JsonSupport}
import com.kubukoz.trellocat.service.{GithubService, TransferService, TrelloService}
import com.kubukoz.trellocat.util.CustomDirectives._

import scala.concurrent.{ExecutionContext, Future}

trait Routes extends JsonSupport {
  val trelloService: TrelloService
  val githubService: GithubService

  def routes(implicit ec: ExecutionContext) = logAndHandleExternalExceptions {
    path("boards") {
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
  }

  def transferBoardWithId(boardId: String, repo: Github.Repo)
                         (implicit ec: ExecutionContext): Future[Github.ProjectWithColumns] = {
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