package com.kubukoz

import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.kubukoz.trellocat.Routes
import com.kubukoz.trellocat.domain.{Github, JsonSupport, Trello}
import com.kubukoz.trellocat.service.{GithubService, MockGithubService, MockTrelloService, TrelloService}

import scala.concurrent.{ExecutionContext, Future}

class RouteTests extends BaseSpec with ScalatestRouteTest with JsonSupport {
  "/boards" should "return a list of boards" in {
    val mockTrelloService = new MockTrelloService {
      override def allBoards(implicit ec: ExecutionContext): Future[List[Trello.Board]] =
        Future.successful(List(
          Trello.Board("12345-12345", "First board"),
          Trello.Board("22222-22222", "Second board")
        ))
    }

    val routes = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = new MockGithubService {}
    }

    Get("/boards") ~> routes.routes ~> check {
      responseAs[List[Trello.Board]] shouldBe List(
        Trello.Board("12345-12345", "First board"),
        Trello.Board("22222-22222", "Second board")
      )
    }
  }

  "/transfer" should "transfer a board to github" in {
    val transferredBoardId = "BOARD-ID"
    val transferredBoardName = "Transferred board 1"
    val myRepoId = "my-repo"

    val mockTrelloService = new MockTrelloService {
      override def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Trello.Board] = boardId match {
        case `transferredBoardId` =>
          Future.successful(Trello.Board(transferredBoardId, transferredBoardName))

      }

      override def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Trello.Column]] = boardId match {
        case `transferredBoardId` => Future.successful(List(
          Trello.Column("column-1", "Column 1", List(
            Trello.Card("card-1", "Card 1"),
            Trello.Card("card-2", "Card 2")
          )),
          Trello.Column("column-2", "Column 2", List(
            Trello.Card("card-3", "Card 3"),
            Trello.Card("card-4", "Card 4")
          ))
        ))
      }
    }

    val mockGithubService = new MockGithubService {
      override def createProject(repoId: String, projectName: String): Future[Github.Project] = (repoId, projectName) match {
        case (`myRepoId`, `transferredBoardName`) => Future.successful(
          Github.Project("PROJECT-ID", transferredBoardName)
        )
      }

      override def createColumn(projectId: String, column: Github.Column): Future[Unit] = projectId match {
        case "PROJECT-ID" => Future.successful(())
      }
    }

    val router = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    Post(s"/transfer?from=$transferredBoardId&to=$myRepoId") ~> router.routes ~> check {
      responseAs[String] shouldBe "PROJECT-ID"
    }
  }
}
