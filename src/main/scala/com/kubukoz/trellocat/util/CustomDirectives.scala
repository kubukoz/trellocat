package com.kubukoz.trellocat.util

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, _}

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
}
