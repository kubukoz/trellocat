package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.server.Directives
import com.kubukoz.trellocat.BaseSpec
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import scala.concurrent.duration._

class RealGithubServiceTests extends BaseSpec with JsonSupport {
  "createProject" should "create a project" in {

    val projectId = 100
    val boardName = "Project 1"
    val repoName = "some-repo"
    val user = User("some-user")

    val ap = AuthParams(Query("access_token" -> "some-token"))

    val githubApiRoutes = {
      import Directives._

      path("repos" / user.login / repoName / "projects") {
        post {
          parameter("access_token") {
            case "some-token" =>
              entity(as[ProjectStub]) {
                case ProjectStub(`boardName`) =>
                  complete(Github.Project(projectId, boardName, 1))
              }
          }
        }
      }
    }

    implicit val client = new MockApiClient(githubApiRoutes)

    val ghService = new RealGithubService(ap)

    implicit val timeout = Timeout(3.seconds)
    ghService.createProject(user, repoName, ProjectStub(boardName)).futureValue(timeout) shouldBe Github.Project(projectId, boardName, 1)
  }

  it should "handle trying to create a project in a nonexistent repo" in {
    val boardName = "Project 2"
    val repoName = "nope-nope"
    val userName = "some-user"
    val user = User(userName)

    implicit val ap = AuthParams(Query("access_token" -> "token"))

    val githubApiRoutes = {
      import Directives._

      path("repos" / userName / repoName / "projects") {
        post {
          parameter("access_token") {
            case "token" => reject
          }
        }
      }
    }

    implicit val client = new MockApiClient(githubApiRoutes)

    val ghService = new RealGithubService(ap)

    implicit val timeout = Timeout(1.second)

    ghService.createProject(user, repoName, ProjectStub(boardName)).failed.futureValue.getMessage shouldBe "Request was rejected"
  }

  "createColumn" should "create a column" in {
    val columnName = "Some column"
    val repoName = "some-repo"
    val user = User("some-user")
    val column = Column(columnName, 1)

    val ap = AuthParams(Query("access_token" -> "some-token"))

    val githubApiRoutes = {
      import Directives._
      path("repos" / user.login / repoName / "projects" / "1" / "columns") {
        post {
          parameter("access_token") {
            case "some-token" =>
              entity(as[ColumnStub]) {
                case ColumnStub(`columnName`) =>
                  complete(column)
              }
          }
        }
      }
    }

    implicit val client = new MockApiClient(githubApiRoutes)

    val ghService = new RealGithubService(ap)

    ghService.createColumn(user, Project(1, "my-project", 1), repoName, ColumnStub(columnName)).futureValue shouldBe column
  }

  it should "not create a column if the related project doesn't exist" in {
    val columnName = "Some column"
    val repoName = "some-repo"
    val user = User("some-user")

    val ap = AuthParams(Query("access_token" -> "some-token"))

    val githubApiRoutes = {
      import Directives._
      path("repos" / user.login / repoName / "projects" / "2" / "columns") {
        reject
      }
    }

    implicit val client = new MockApiClient(githubApiRoutes)

    val ghService = new RealGithubService(ap)

    ghService.createColumn(user, Project(2, "my-project", 2), repoName, ColumnStub(columnName)).failed.futureValue.getMessage shouldBe "Request was rejected"
  }
}