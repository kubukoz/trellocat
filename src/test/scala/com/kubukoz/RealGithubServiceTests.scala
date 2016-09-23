package com.kubukoz

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.server.Directives
import akka.stream.Materializer
import com.kubukoz.trellocat.domain.Github.ProjectStub
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}
import com.kubukoz.trellocat.service.RealGithubService
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import scala.concurrent.duration._

class RealGithubServiceTests extends BaseSpec with JsonSupport {
  "createProject" should "create a project" in {

    val projectId = 100
    val boardName = "Project 1"
    val repoName = "some-repo"
    val userName = "some-user"

    implicit val ap = AuthParams(Query("access_token" -> "some-token"))
    implicit val mat: Materializer = null

    val githubApiRoutes = {
      import Directives._

      path("user") {
        get {
          parameter("access_token") {
            case "some-token" =>
              complete(Github.User(userName))
          }
        }
      } ~ path("repos" / "some-user" / "some-repo" / "projects") {
        post {
          entity(as[ProjectStub]) {
            case ProjectStub(`boardName`) =>
              complete(Github.Project(projectId, boardName))
          }
        }
      }
    }

    implicit val client = new MockApiClient(githubApiRoutes)

    val ghService = new RealGithubService(ap)

    implicit val timeout = Timeout(1.second)
    ghService.createProject(repoName, boardName).futureValue shouldBe Github.Project(projectId, boardName)
  }
}