package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.config.GithubConstants
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain.{AuthParams, Github, JsonSupport}
import com.kubukoz.trellocat.util.AkkaHttpUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the GitHub API.
  **/
trait GithubService {
  //todo doc
  def createCard(user: User, project: Project, repoName: String, column: Column, card: Card)
                (implicit ec: ExecutionContext): Future[Card]

  /**
    * Creates a column within a project.
    **/
  def createColumn(user: User, projectNumber: Int, repoName: String, columnStub: ColumnStub)(implicit ec: ExecutionContext): Future[Column]

  /**
    * Creates a project with the given name in the given repo, owned by the current user.
    **/
  def createProject(user: User, repoName: String, projectStub: ProjectStub)(implicit ec: ExecutionContext): Future[Github.Project]

  /**
    * Returns information about the current user.
    **/
  def getUser()(implicit ec: ExecutionContext): Future[Github.User]
}

/**
  * Implements [[GithubService]] with a HTTP client.
  **/
class RealGithubService(ap: AuthParams)(implicit api: ApiClient, mat: Materializer)
  extends GithubService with JsonSupport with GithubConstants with AkkaHttpUtils {

  implicit val app = ap

  override def createProject(user: User, repoName: String, projectStub: ProjectStub)(implicit ec: ExecutionContext): Future[Project] = {
    for {
      entity <- toEntity(projectStub)
      project <- api[Project](inertiaRequest(projectsUrl(user, repoName)).withEntity(entity))
    } yield project
  }

  override def createCard(user: User, project: Project, repoName: String, column: Column, card: Card)
                         (implicit ec: ExecutionContext): Future[Card] = ???

  override def createColumn(user: User, projectNumber: Int, repoName: String, columnStub: ColumnStub)
                           (implicit ec: ExecutionContext): Future[Column] =
    for {
      entity <- toEntity(columnStub)
      uri = columnsUri(user, repoName, projectNumber)
      column <- api[Column](inertiaRequest(uri).withEntity(entity))
    } yield column

  override def getUser()(implicit ec: ExecutionContext): Future[User] = api[User] {
    HttpRequest(
      method = HttpMethods.GET,
      uri = Uri(userUrl).withAuthQuery(Query.Empty)
    )
  }

  protected def inertiaRequest(uri: Uri, queryString: Query = Query.Empty) = HttpRequest(
    method = HttpMethods.POST,
    uri = uri.withAuthQuery(queryString),
    headers = List(acceptInertia)
  )

  protected def projectsUrl(user: User, repoName: String): String =
    s"$baseUrl/repos/${user.login}/$repoName/projects"

  protected def columnsUri(user: User, repoName: String, projectNumber: Int): String =
    s"${projectsUrl(user, repoName)}/$projectNumber/columns"
}

//noinspection NotImplementedCode
//todo move to test/
class MockGithubService extends GithubService {
  override def createProject(user: User, repoName: String, projectStub: ProjectStub)(implicit ec: ExecutionContext): Future[Project] = ???

  override def createCard(user: User, project: Project, repoName: String, column: Column, card: Card)(implicit ec: ExecutionContext): Future[Card] = ???

  override def createColumn(user: User, projectNumber: Int, repoName: String, columnStub: ColumnStub)(implicit ec: ExecutionContext): Future[Column] = ???

  override def getUser()(implicit ec: ExecutionContext): Future[User] = ???
}