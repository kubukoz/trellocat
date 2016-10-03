package com.kubukoz.trellocat.api

import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.http.scaladsl.unmarshalling.{FromResponseUnmarshaller, Unmarshal}
import akka.http.scaladsl.util.FastFuture
import akka.stream.Materializer
import com.kubukoz.trellocat.domain.{AccessToken, AuthParams}
import com.kubukoz.trellocat.util.StatusCodeException

import scala.concurrent.{ExecutionContext, Future}

/**
  * Simplifies creating API clients.
  *
  * Provides a single method that takes a request and returns a future of chosen type.
  **/
trait ApiClient {
  def apply[T: FromResponseUnmarshaller](request: HttpRequest)
                                        (implicit ec: ExecutionContext): Future[T]
}

/**
  * Implements [[ApiClient]] in terms of `Http()`
  **/
class RealApiClient(implicit http: HttpExt, materializer: Materializer) extends ApiClient {
  override def apply[T: FromResponseUnmarshaller](request: HttpRequest)
                                                 (implicit ec: ExecutionContext): Future[T] =
    http.singleRequest(request).flatMap(unmarshalOrThrow[T])

  def unmarshalOrThrow[T: FromResponseUnmarshaller](result: HttpResponse)
                                                   (implicit ec: ExecutionContext): Future[T] =
    Unmarshal(result).to[T].recoverWith {
      case _ if result.status.isFailure =>
        FastFuture.failed(StatusCodeException(result.status))
    }
}

object ApiClient {

  /**
    * Simplifies adding the auth parameters to a URI's query string.
    **/
  implicit class AuthenticatedUri(uri: Uri)(implicit authParams: AuthParams, token: AccessToken) {
    def withAuthQuery(query: Query): Uri = uri.withQuery(Query(token.authQuery ++ authParams.authQuery ++ query: _*))
  }

}