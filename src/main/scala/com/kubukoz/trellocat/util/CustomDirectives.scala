package com.kubukoz.trellocat.util

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, _}
import com.kubukoz.trellocat.domain.{GithubToken, TrelloToken}

object CustomDirectives {
  /**
    * Logs and handles external services' exceptions.
    **/
  def logAndHandleExternalExceptions: Directive0 = extractLog.flatMap { log =>
    handleExceptions(
      ExceptionHandler {
        case ex@StatusCodeException(Unauthorized) =>
          log.error(ex, "Invalid token")
          complete(Unauthorized, "Unauthorized")
        case thr =>
          log.error(thr, "Exception in request")
          complete(BadGateway, "External service error")
      }
    )
  }

  def withGithubToken: Directive1[GithubToken] =
    headerValueByName("GITHUB-TOKEN").map(GithubToken)

  def withTrelloToken: Directive1[TrelloToken] =
    headerValueByName("TRELLO-TOKEN").map(TrelloToken)
}
