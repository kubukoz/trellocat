package com.kubukoz.trellocat

import akka.http.scaladsl.server._
import com.kubukoz.trellocat.domain.Github.{Project, User}
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
      project <- githubService.createProject(user, repoName, board.name)
      _ <- transferColumns(columns, project, user, repoName)
    } yield project
  }

  def transferColumns(columns: List[Trello.Column], project: Github.Project,
                      user: User, repoName: String): Future[List[Github.Column]] = {
    Future.sequence {
      columns
        .map(_.toGithub)
        .map(githubService.createColumn(user, project.number, repoName, _))
    }
  }
}