package com.kubukoz.trellocat.service

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, RequestEntity, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.config.GithubConstants
import com.kubukoz.trellocat.domain.Github.{Column, Project, ProjectStub, User}
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the GitHub API.
  **/
trait GithubService {
  //todo doc
  def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit]

  /**
    * Creates a project with the given name in the given repo, owned by the current user.
    **/
  def createProject(user: User, repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Github.Project]

  /**
    * Returns information about the current user.
    **/
  def getUser()(implicit ec: ExecutionContext): Future[Github.User]
}

/**
  * Implements [[GithubService]] with a HTTP client.
  **/
class RealGithubService(ap: AuthParams)(implicit api: ApiClient, mat: Materializer)
  extends GithubService with JsonSupport with GithubConstants {

  implicit val app = ap

  /**
    * Creates a project with the given name in the given repo, owned by the current user.
    **/
  override def createProject(user: User, repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Project] = {
    for {
      entity <- Marshal(ProjectStub(projectName)).to[RequestEntity]
      project <- api[Project](projectRequest(user, repoName).withEntity(entity))
    } yield project
  }

  def projectsUrl(user: User, repoName: String): String = s"$baseUrl/repos/${user.login}/$repoName/projects"

  def projectRequest(user: User, repoName: String) = HttpRequest(
    method = HttpMethods.POST,
    uri = Uri(projectsUrl(user, repoName)).withAuthQuery(Query.Empty),
    headers = List(acceptInertia)
  )

  override def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit] = ???

  override def getUser()(implicit ec: ExecutionContext): Future[User] = api[User] {
    HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(userUrl).withAuthQuery(Query.Empty)
    )
  }
}

//todo move to test/
//noinspection NotImplementedCode
class MockGithubService extends GithubService {
  override def createProject(user: User, repoName: String, projectName: String)(implicit ec: ExecutionContext): Future[Project] = ???

  override def createColumn(projectId: Long, column: Column)(implicit ec: ExecutionContext): Future[Unit] = ???

  override def getUser()(implicit ec: ExecutionContext): Future[User] = ???
}