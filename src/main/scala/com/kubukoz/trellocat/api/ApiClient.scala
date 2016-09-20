package com.kubukoz.trellocat.api

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import akka.stream.Materializer
import com.kubukoz.trellocat.domain.AuthParams

import scala.concurrent.{ExecutionContext, Future}

/**
  * Simplifies creating API clients.
  *
  * Provides a single method that takes a request and returns a future of chosen type.
  **/
trait ApiClient {
  def apply[T](request: HttpRequest)
              (implicit unmarshaller: FromResponseUnmarshaller[T], mat: Materializer, ec: ExecutionContext): Future[T]
}

/**
  * Implements [[ApiClient]] in terms of `Http()`
  **/
class RealApiClient(implicit http: HttpExt) extends ApiClient {
  override def apply[T](request: HttpRequest)
                       (implicit unmarshaller: FromResponseUnmarshaller[T], mat: Materializer, ec: ExecutionContext): Future[T] =
    http.singleRequest(request).flatMap(Unmarshal(_).to[T])
}

object ApiClient {

  /**
    * Simplifies adding the auth parameters to a URI's query string.
    **/
  implicit class AuthenticatedUri(uri: Uri)(implicit authParams: AuthParams) {
    //todo TESTS
    def withAuthQuery(query: Query): Uri = uri.withQuery(Query(authParams.authQuery ++ query: _*))
  }

}