package com.kubukoz

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kubukoz.trellocat.Routes
import com.kubukoz.trellocat.domain.Trello.Board
import com.kubukoz.trellocat.domain.{AuthParams, JsonSupport}
import com.kubukoz.trellocat.service.{MockTrelloService, TrelloService}

import scala.concurrent.{ExecutionContext, Future}

class RouteTests extends BaseSpec with ScalatestRouteTest with JsonSupport {
  "/boards" should "return a list of boards" in {
    val mockTrelloService = new MockTrelloService {
      override def allBoards(implicit ap: AuthParams, ec: ExecutionContext): Future[List[Board]] =
        Future.successful(List(
          Board("12345-12345", "First board"),
          Board("22222-22222", "Second board")
        ))
    }

    val routes = new Routes {
      override val trelloService: TrelloService = mockTrelloService
    }

    Get("/boards") ~> routes.routes ~> check {
      responseAs[List[Board]] shouldBe List(
        Board("12345-12345", "First board"),
        Board("22222-22222", "Second board")
      )
    }
  }
}
