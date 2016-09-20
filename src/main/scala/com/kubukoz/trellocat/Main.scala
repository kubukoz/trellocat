package com.kubukoz.trellocat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.stream.ActorMaterializer
import com.kubukoz.trellocat.api.RealApiClient
import com.kubukoz.trellocat.domain.AuthParams
import com.kubukoz.trellocat.domain.Trello.Board
import com.kubukoz.trellocat.service.{RealTrelloService, TrelloService}
import com.typesafe.config.ConfigFactory
import configs.Configs

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object Main {

  implicit val system = ActorSystem("trellocat")
  implicit val materializer = ActorMaterializer()

  implicit val http = Http(system)
  implicit val api = new RealApiClient

  val config = ConfigFactory.load()
  val trelloConfig = Configs[TrelloConfig].get(config, "trello").value

  implicit val authParams = AuthParams(Query("key" -> trelloConfig.apiKey, "token" -> trelloConfig.apiToken))

  val service: TrelloService = new RealTrelloService()

  def main(args: Array[String]): Unit = {
    val futureBoards: Future[List[Board]] = service.allBoards

    futureBoards foreach println
  }
}