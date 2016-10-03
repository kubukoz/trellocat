package com.kubukoz.trellocat

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import akka.http.scaladsl.model.{HttpHeader, StatusCodes}
import akka.http.scaladsl.server.MissingHeaderRejection
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain._
import com.kubukoz.trellocat.service._
import com.kubukoz.trellocat.util.StatusCodeException
import com.typesafe.config.ConfigFactory

import scala.concurrent.{ExecutionContext, Future}

class RouteTests extends BaseSpec with JsonSupport {
  "/boards" should "return a list of boards" in {
    val mockTrelloService = new MockTrelloService {
      override def allBoards(implicit trelloToken: TrelloToken, ec: ExecutionContext): Future[List[Trello.Board]] =
        Future.successful(List(
          Trello.Board("12345-12345", "First board"),
          Trello.Board("22222-22222", "Second board")
        ))
    }

    val routes = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = new MockGithubService
    }

    val trelloToken = HttpHeader.parse("TRELLO-TOKEN", "some-token").asInstanceOf[Ok].header

    Get("/boards").withHeaders(trelloToken) ~> routes.routes ~> check {
      responseAs[List[Trello.Board]] shouldBe List(
        Trello.Board("12345-12345", "First board"),
        Trello.Board("22222-22222", "Second board")
      )
    }
  }

  "/transfer" should "transfer a board to github" in {
    val transferredBoardId = "BOARD-ID"
    val transferredBoardName = "Transferred board 1"

    val repo = Repo("my-repo")
    val expectedProjectId = 100
    val user = User("user-name")

    val mockTrelloService = new MockTrelloService {
      override def boardById(boardId: String)(implicit trelloToken: TrelloToken, ec: ExecutionContext): Future[Trello.Board] = boardId match {
        case `transferredBoardId` =>
          Future.successful(Trello.Board(transferredBoardId, transferredBoardName))

      }

      override def columnsOnBoard(boardId: String)(implicit trelloToken: TrelloToken, ec: ExecutionContext): Future[List[Trello.Column]] = boardId match {
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
      override def createProject(rUser: User, rRepo: Repo, projectStub: ProjectStub)
                                (implicit githubToken: GithubToken, ec: ExecutionContext): Future[Github.Project] =
        (rUser, rRepo, projectStub) match {
          case (`user`, `repo`, ProjectStub(`transferredBoardName`)) => Future.successful(
            Github.Project(expectedProjectId, transferredBoardName, 1)
          )
        }

      var columnIndex = 0

      override def createColumn(user: User, project: Project, repo: Repo, column: Github.ColumnStub)
                               (implicit githubToken: GithubToken, ec: ExecutionContext): Future[Column] = project match {
        case Project(_, _, 1) =>
          columnIndex += 1
          Future.successful(Github.Column(column.name, columnIndex))
      }

      override def createCard(user: User, project: Project, repo: Repo, column: Column, card: Card)
                             (implicit githubToken: GithubToken, ec: ExecutionContext): Future[Card] =
        Future.successful(card)

      override def getUser()(implicit githubToken: GithubToken, ec: ExecutionContext): Future[User] = Future.successful(user)
    }

    val router = new Routes {
      override val trelloService: TrelloService = mockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    val trelloToken = HttpHeader.parse("TRELLO-TOKEN", "some-token").asInstanceOf[Ok].header
    val githubToken = HttpHeader.parse("GITHUB-TOKEN", "some-token").asInstanceOf[Ok].header

    Post(s"/transfer?from=$transferredBoardId&to=${repo.name}")
      .withHeaders(trelloToken, githubToken) ~> router.routes ~> check {
      responseAs[Github.ProjectWithColumns] shouldBe
        Github.ProjectWithColumns(
          Github.Project(expectedProjectId, transferredBoardName, 1),
          List(
            ColumnWithCards(Github.Column("Column 1", 1), List(
              Github.Card("Card 1"),
              Github.Card("Card 2")
            )),
            ColumnWithCards(Github.Column("Column 2", 2), List(
              Github.Card("Card 3"),
              Github.Card("Card 4")
            ))
          )
        )
    }
  }

  "/repos" should "return a list of repos" in {
    val mockGithubService = new MockGithubService {
      override def allRepos(implicit githubToken: GithubToken, ec: ExecutionContext): Future[List[Repo]] =
        Future.successful(List(
          Github.Repo("hello"),
          Github.Repo("world")
        ))
    }

    val routes = new Routes {
      override val trelloService: TrelloService = new MockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    val githubToken = HttpHeader.parse("GITHUB-TOKEN", "some-token").asInstanceOf[Ok].header

    Get("/repos").withHeaders(githubToken) ~> routes.routes ~> check {
      responseAs[List[Github.Repo]] shouldBe List(
        Github.Repo("hello"),
        Github.Repo("world")
      )
    }
  }

  "/repos" should "return Unauthorized if the token is wrong" in {
    val mockGithubService = new MockGithubService {
      override def allRepos(implicit githubToken: GithubToken, ec: ExecutionContext): Future[List[Repo]] =
        githubToken.value match {
          case "bad-token" => Future.failed(StatusCodeException(StatusCodes.Unauthorized))
        }
    }
    val routes = new Routes {
      override val trelloService: TrelloService = new MockTrelloService
      override val githubService: GithubService = mockGithubService
    }

    val githubToken = HttpHeader.parse("GITHUB-TOKEN", "bad-token").asInstanceOf[Ok].header

    Get("/repos").withHeaders(githubToken) ~> routes.routes ~> check {
      responseAs[String] shouldBe "Unauthorized"
    }
  }

  "/repos" should "return Bad Request if there is no token" in {
    val routes = new Routes {
      override val trelloService: TrelloService = new MockTrelloService
      override val githubService: GithubService = new MockGithubService
    }

    Get("/repos") ~> routes.routes ~> check {
      rejection shouldBe MissingHeaderRejection("GITHUB-TOKEN")
    }
  }

  override protected def createActorSystem(): ActorSystem =
    ActorSystem("customDirectivesTests", ConfigFactory.parseString(
      """
        |akka {
        | stdout-loglevel = "OFF"
        | loglevel = "OFF"
        |}
      """.stripMargin))
}
