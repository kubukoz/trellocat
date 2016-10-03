package com.kubukoz.trellocat.service

import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpMethods, HttpRequest, Uri}
import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.config.GithubConstants
import com.kubukoz.trellocat.domain.Github._
import com.kubukoz.trellocat.domain._
import com.kubukoz.trellocat.util.AkkaHttpUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Accesses the GitHub API.
  **/
trait GithubService {
  /**
    * Provides a list of all of the user's repos.
    **/
  def allRepos(implicit token: GithubToken, ec: ExecutionContext): Future[List[Repo]]

  /**
    * Creates a card within a column.
    **/
  def createCard(user: User, project: Project, repo: Repo, column: Column, card: Card)
                (implicit token: GithubToken, ec: ExecutionContext): Future[Card]

  /**
    * Creates a column within a project.
    **/
  def createColumn(user: User, project: Project, repo: Repo, columnStub: ColumnStub)
                  (implicit token: GithubToken, ec: ExecutionContext): Future[Column]

  /**
    * Creates a project with the given name in the given repo, owned by the current user.
    **/
  def createProject(user: User, repo: Repo, projectStub: ProjectStub)
                   (implicit token: GithubToken, ec: ExecutionContext): Future[Github.Project]

  /**
    * Returns information about the current user.
    **/
  def getUser()(implicit token: GithubToken, ec: ExecutionContext): Future[Github.User]
}

/**
  * Implements [[GithubService]] with a HTTP client.
  **/
class RealGithubService(implicit api: ApiClient, mat: Materializer)
  extends GithubService with JsonSupport with GithubConstants with AkkaHttpUtils {

  implicit val authParams = AuthParams.NoParams

  override def createProject(user: User, repo: Repo, projectStub: ProjectStub)
                            (implicit token: GithubToken, ec: ExecutionContext): Future[Project] = {
    for {
      entity <- toEntity(projectStub)
      project <- api[Project](inertiaRequest(projectsUrl(user, repo)).withEntity(entity))
    } yield project
  }

  override def allRepos(implicit token: GithubToken, ec: ExecutionContext): Future[List[Repo]] =
    api[List[Repo]](HttpRequest(uri = Uri(s"$baseUrl/user/repos").withAuthQuery(Query.Empty)))

  override def createCard(user: User, project: Project, repo: Repo, column: Column, card: Card)
                         (implicit token: GithubToken, ec: ExecutionContext): Future[Card] =
    for {
      entity <- toEntity(card)
      uri = cardsUri(user, repo, project, column)
      newCard <- api[Card](inertiaRequest(uri).withEntity(entity))
    } yield newCard

  override def createColumn(user: User, project: Project, repo: Repo, columnStub: ColumnStub)
                           (implicit token: GithubToken, ec: ExecutionContext): Future[Column] =
    for {
      entity <- toEntity(columnStub)
      uri = columnsUri(user, repo, project)
      column <- api[Column](inertiaRequest(uri).withEntity(entity))
    } yield column

  override def getUser()(implicit token: GithubToken, ec: ExecutionContext): Future[User] =
    api[User](HttpRequest(uri = Uri(userUrl).withAuthQuery(Query.Empty)))

  protected def inertiaRequest(uri: Uri, queryString: Query = Query.Empty)
                              (implicit token: GithubToken) =
    HttpRequest(
      method = HttpMethods.POST,
      uri = uri.withAuthQuery(queryString),
      headers = List(acceptInertia)
    )

  protected def projectsUrl(user: User, repo: Repo): String =
    s"$baseUrl/repos/${user.login}/${repo.name}/projects"

  protected def columnsUri(user: User, repo: Repo, project: Project): String =
    s"${projectsUrl(user, repo)}/${project.number}/columns"

  protected def cardsUri(user: User, repo: Repo, project: Project, column: Column): String =
    s"${columnsUri(user, repo, project)}/${column.number}/cards"
}
