package com.kubukoz.trellocat

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.kubukoz.trellocat.api.RealApiClient
import com.kubukoz.trellocat.config.{GithubConfig, TrelloConfig}
import com.kubukoz.trellocat.service._
import com.typesafe.config.ConfigFactory
import configs.Configs

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends Routes {
  implicit val system = ActorSystem("trellocat")
  implicit val materializer = ActorMaterializer()

  implicit val http = Http()
  implicit val api = new RealApiClient

  val config = ConfigFactory.load()
  val trelloConfig = Configs[TrelloConfig].get(config, "trello").value
  val githubConfig = Configs[GithubConfig].get(config, "github").value

  val trelloService = new RealTrelloService(trelloConfig)

  val githubService = new RealGithubService()

  def main(args: Array[String]): Unit = {
    val server = http.bindAndHandle(routes, "localhost", 8080)

    println("Press enter to stop")
    io.StdIn.readLine()
    server.flatMap(_.unbind()).foreach(_ => system.terminate())
  }
}