package com.kubukoz.trellocat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.server.Directives
import akka.stream.ActorMaterializer
import com.kubukoz.trellocat.api.RealApiClient
import com.kubukoz.trellocat.domain.Trello.Column
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}
import com.kubukoz.trellocat.service.{GithubService, TrelloService}
import com.typesafe.config.ConfigFactory
import configs.Configs
import spray.json.pimpAny

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Routes extends Directives with JsonSupport {

  implicit val system = ActorSystem("trellocat")
  implicit val materializer = ActorMaterializer()

  implicit val http = Http()
  implicit val api = new RealApiClient

  val config = ConfigFactory.load()
  val trelloConfig = Configs[TrelloConfig].get(config, "trello").value

  implicit val authParams = AuthParams(Query("key" -> trelloConfig.apiKey, "token" -> trelloConfig.apiToken))

  val trelloService: TrelloService
  val githubService: GithubService

  val routes = path("boards") {
    get {
      complete {
        trelloService.allBoards.map(_.toJson)
      }
    }
  } ~ path("transfer") {
    parameters('from, 'to) { (trelloBoardId, repoId) =>
      post {
        complete {
          transferBoardWithId(trelloBoardId, repoId)
        }
      }
    }
  }

  def transferBoardWithId(boardId: String, repoId: String) = {
    for {
      board <- trelloService.boardById(boardId)
      columns <- trelloService.columnsOnBoard(boardId)
      project <- githubService.createProject(repoId, board.name)
      _ <- transferColumns(columns, project)
    } yield project.id
  }

  def transferColumns(columns: List[Column], project: Github.Project) = {
    Future.sequence {
      columns
        .map(_.toGithub)
        .map(githubService.createColumn(project.id, _))
    }
  }
}