package com.kubukoz

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.FromResponseUnmarshaller
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.domain.{AuthParams, Github}
import com.kubukoz.trellocat.service.RealGithubService
import org.scalatest.concurrent.PatienceConfiguration.Timeout

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class RealGithubServiceTests extends BaseSpec {
  "createProject" should "create a project" in {

    val projectId = 100
    val boardName = "Project 1"
    val repoName = "some-repo"

    implicit val ap = AuthParams(Query("access_token" -> "some-token"))
    implicit val mat: Materializer = null

    implicit val client = new ApiClient {
      override def apply[T](request: HttpRequest)(implicit unmarshaller: FromResponseUnmarshaller[T],
                                                  mat: Materializer, ec: ExecutionContext): Future[T] = {
        request match {
          case _ if request.uri == Uri(RealGithubService.userUrl).withAuthQuery(Query.Empty) =>
            Future.successful(
              Github.User("some-user").asInstanceOf[T]
            )
          case _ =>
            Future.successful {
              Github.Project(projectId, boardName).asInstanceOf[T]
            }
        }
      }
    }
    val ghService = new RealGithubService(ap)

    implicit val timeout = Timeout(1.second)
    ghService.createProject(repoName, boardName).futureValue shouldBe Github.Project(projectId, boardName)
  }
}