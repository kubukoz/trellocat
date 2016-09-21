package com.kubukoz

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kubukoz.trellocat.Routes
import com.kubukoz.trellocat.domain.JsonSupport
import com.kubukoz.trellocat.domain.Trello.Board
import com.kubukoz.trellocat.service.{GithubService, MockGithubService, MockTrelloService, TrelloService}

import scala.concurrent.{ExecutionContext, Future}

class RouteTests extends BaseSpec with ScalatestRouteTest with JsonSupport {
  "/boards" should "return a list of boards" in {
    val mockTrelloService = new MockTrelloService {
      override def allBoards(implicit ec: ExecutionContext): Future[List[Board]] =
        Future.successful(List(
          Board("12345-12345", "First board"),
          Board("22222-22222", "Second board")
        ))
    }

    val routes = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = new MockGithubService {}
    }

    Get("/boards") ~> routes.routes ~> check {
      responseAs[List[Board]] shouldBe List(
        Board("12345-12345", "First board"),
        Board("22222-22222", "Second board")
      )
    }
  }

  "/transfer" should "transfer a board to github" in {
    val transferredBoardId = "BOARD-ID"
    val mockTrelloService = new MockTrelloService {

    }

    val mockGithubService = new MockGithubService {

    }

    val router = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    Post(s"/transfer/$transferredBoardId") ~> router.routes ~> check {
      responseAs[String] /*Github board id*/ shouldBe "NEW-BOARD-ID"
    }
  }
}
