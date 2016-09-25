package com.kubukoz

import com.kubukoz.trellocat.Routes
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain.{Github, JsonSupport, Trello}
import com.kubukoz.trellocat.service._

import scala.concurrent.{ExecutionContext, Future}

class RouteTests extends BaseSpec with JsonSupport {
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

    val repoName = "my-repo"
    val expectedProjectId = 100
    val user = User("user-name")

    val mockTrelloService = new MockTrelloService {
      override def boardById(boardId: String)(implicit ec: ExecutionContext): Future[Trello.Board] = boardId match {
        case `transferredBoardId` =>
          Future.successful(Trello.Board(transferredBoardId, transferredBoardName))

      }

      override def columnsOnBoard(boardId: String)(implicit ec: ExecutionContext): Future[List[Trello.Column]] = boardId match {
        case `transferredBoardId` => Future.successful(List(
          Trello.Column("Column 1", List(
            Trello.Card("card-1", "Card 1"),
            Trello.Card("card-2", "Card 2")
          )),
          Trello.Column("Column 2", List(
            Trello.Card("card-3", "Card 3"),
            Trello.Card("card-4", "Card 4")
          ))
        ))
      }
    }

    val mockGithubService = new MockGithubService {
      override def createProject(rUser: User, rRepoName: String, projectStub: ProjectStub)
                                (implicit ec: ExecutionContext): Future[Github.Project] =
        (rUser, rRepoName, projectStub) match {
          case (`user`, `repoName`, ProjectStub(`transferredBoardName`)) => Future.successful(
            Github.Project(expectedProjectId, transferredBoardName, 1)
          )
        }

      override def createColumn(user: User, project: Project, repoName: String, column: Github.ColumnStub)
                               (implicit ec: ExecutionContext): Future[Column] = project match {
        case Project(_, _, 1) => Future.successful(null)
      }

      override def createCard(user: User, project: Project, repoName: String, column: Column, card: Card)
                             (implicit ec: ExecutionContext): Future[Card] =
        Future.successful(card)

      override def getUser()(implicit ec: ExecutionContext): Future[User] = Future.successful(user)
    }

    val router = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    Post(s"/transfer?from=$transferredBoardId&to=$repoName") ~> router.routes ~> check {
      responseAs[Github.Project] shouldBe Github.Project(expectedProjectId, transferredBoardName, 1)
    }
  }
}
