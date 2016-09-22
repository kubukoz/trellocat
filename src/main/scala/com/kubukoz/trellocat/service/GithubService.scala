package com.kubukoz.trellocat.service

import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.domain.Github.Column
import com.kubukoz.trellocat.domain.{Github, JsonSupport}

import scala.concurrent.Future

/**
  * Accesses the GitHub API.
  **/
trait GithubService {
  def createColumn(projectId: String, column: Column): Future[Unit] = ???

  def createProject(repoId: String, projectName: String): Future[Github.Project] = ???
}

/**
  * Implements [[GithubService]] with a HTTP client.
  * */
class RealGithubService(implicit api: ApiClient, mat: Materializer) extends GithubService with JsonSupport{

}

class MockGithubService extends GithubService {

}