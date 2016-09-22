package com.kubukoz.trellocat

import akka.http.scaladsl.model.Uri.Query
import com.kubukoz.trellocat.config.{GithubConfig, TrelloConfig}
import com.kubukoz.trellocat.domain.AuthParams
import com.kubukoz.trellocat.service.{GithubService, RealGithubService, RealTrelloService, TrelloService}
import com.typesafe.config.ConfigFactory
import configs.Configs

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends Routes {
  val config = ConfigFactory.load()
  val trelloConfig = Configs[TrelloConfig].get(config, "trello").value
  val githubConfig = Configs[GithubConfig].get(config, "github").value

  val trelloAuthParams = AuthParams(Query("key" -> trelloConfig.apiKey, "token" -> trelloConfig.apiToken))
  val githubAuthParams = AuthParams(Query("access_token" -> githubConfig.apiToken))

  override val trelloService: TrelloService = new RealTrelloService(trelloAuthParams)
  override val githubService: GithubService = new RealGithubService(githubAuthParams)

  def main(args: Array[String]): Unit = {
    val server = http.bindAndHandle(routes, "localhost", 8080)

    githubService.createProject("trellocat", "testtest").map { proj =>
      println(s"created $proj")
    }.recover{
      case thr => thr.printStackTrace()
    }
    io.StdIn.readLine("Press enter to stop")
    server.flatMap(_.unbind()).foreach(_ => system.terminate())
  }
}