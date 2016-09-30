package com.kubukoz.trellocat.util

import akka.http.scaladsl.model.StatusCode

/**
  * An exception that holds the status code of the result which caused it.
  **/
case class StatusCodeException(statusCode: StatusCode) extends Exception {
  override def toString: String = super.toString + s"($statusCode)"
}
