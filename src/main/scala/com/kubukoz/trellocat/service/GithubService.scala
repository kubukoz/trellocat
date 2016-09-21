package com.kubukoz.trellocat.service

import akka.stream.Materializer
import com.kubukoz.trellocat.api.ApiClient
import com.kubukoz.trellocat.domain.JsonSupport
import com.kubukoz.trellocat.domain.Trello.Column

/**
  * Accesses the GitHub API.
  **/
trait GithubService {

}

/**
  * Implements [[GithubService]] with a HTTP client.
  * */
class RealGithubService(implicit api: ApiClient, mat: Materializer) extends GithubService with JsonSupport{

}

class MockGithubService extends GithubService {

}