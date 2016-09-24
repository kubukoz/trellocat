package com.kubukoz.trellocat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.kubukoz.trellocat.api.RealApiClient
import com.kubukoz.trellocat.domain.Github.Project
import com.kubukoz.trellocat.domain.Trello.Column
import com.kubukoz.trellocat.domain.{Github, JsonSupport}
import com.kubukoz.trellocat.service.{GithubService, TrelloService}
import spray.json.pimpAny

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Routes extends Directives with JsonSupport {

  implicit val system = ActorSystem("trellocat")
  implicit val materializer = ActorMaterializer()

  implicit val http = Http()
  implicit val api = new RealApiClient

  val trelloService: TrelloService
  val githubService: GithubService

  val routes = path("boards") {
    get {
      complete {
        trelloService.allBoards.map(_.toJson)
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
    for {
      board <- trelloService.boardById(boardId)
      columns <- trelloService.columnsOnBoard(boardId)
      project <- githubService.createProject(repoName, board.name)
      _ <- transferColumns(columns, project)
    } yield project
  }

  def transferColumns(columns: List[Column], project: Github.Project): Future[List[Unit]] = {
    Future.sequence {
      columns
        .map(_.toGithub)
        .map(githubService.createColumn(project.id, _))
    }
  }
}