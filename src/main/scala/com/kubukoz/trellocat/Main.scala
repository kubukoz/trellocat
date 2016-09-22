package com.kubukoz.trellocat

import com.kubukoz.trellocat.service.{GithubService, RealGithubService, RealTrelloService, TrelloService}

import scala.concurrent.ExecutionContext.Implicits.global

object Main extends Routes {
  override val trelloService: TrelloService = new RealTrelloService()
  override val githubService: GithubService = new RealGithubService()

  def main(args: Array[String]): Unit = {
    val server = http.bindAndHandle(routes, "localhost", 8080)

    io.StdIn.readLine("Press enter to stop")
    server.flatMap(_.unbind()).foreach(_ => system.terminate())
  }
}