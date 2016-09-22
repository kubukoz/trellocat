package com.kubukoz.trellocat.service

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.headers.Accept
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri, _}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.domain.Github.{Column, Project, ProjectStub, User}
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}
import com.kubukoz.trellocat.service.RealGithubService._
import spray.json.JsNumber

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the GitHub API.
  **/
trait GithubService {
  def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit]

  def createProject(repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Github.Project]

  def getUser()(implicit ec: ExecutionContext): Future[Github.User]
}

/**
  * Implements [[GithubService]] with a HTTP client.
  **/
class RealGithubService(ap: AuthParams)(implicit api: ApiClient, mat: Materializer) extends GithubService with JsonSupport {
  implicit val app = ap

  override def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit] = ???

  override def createProject(repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Project] =
    for {
      user <- getUser()
      entity <- Marshal(ProjectStub(projectName)).to[RequestEntity]
      project <- api[Project] {
        createProjectRequest(repoName, user).withEntity(entity)
      }
    } yield project


  protected def createProjectRequest(repoName: String, user: User): HttpRequest = {
    HttpRequest(
      method = HttpMethods.POST,
      uri = Uri(projectsUrl(user.login, repoName)).withAuthQuery(Query.Empty),
      headers = List(acceptInertia)
    )
  }

  override def getUser()(implicit ec: ExecutionContext): Future[User] = api[User] {
    HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(userUrl).withAuthQuery(Query.Empty)
    )
  }
}

object RealGithubService {
  val baseUrl = "https://api.github.com"
  val userUrl = s"$baseUrl/user"

  val acceptInertia = Accept.parseFromValueString("application/vnd.github.inertia-preview+json").right.get

  def projectsUrl(userName: String, repoName: String): String = s"$baseUrl/repos/$userName/$repoName/projects"
}

class MockGithubService extends GithubService {
  override def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit] =
    Future.failed(new Exception("Stub!"))

  override def createProject(repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Project] =
    Future.failed(new Exception("Stub!"))

  override def getUser()(implicit ec: ExecutionContext): Future[User] =
    Future.failed(new Exception("Stub!"))
}