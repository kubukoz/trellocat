package com.kubukoz.trellocat.domain

import akka.http.scaladsl.model.Uri.Query

/**
  * Access token for a Service.
  * */
class AccessToken(val authQuery: Query)

case class GithubToken(value: String) extends AccessToken(Query("access_token" -> value))

case class TrelloToken(value: String) extends AccessToken(Query("token" -> value))