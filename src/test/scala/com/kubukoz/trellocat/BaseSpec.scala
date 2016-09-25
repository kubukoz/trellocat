package com.kubukoz.trellocat

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.http.scaladsl.unmarshalling.{Unmarshal, _}
import com.kubukoz.trellocat.api.ApiClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.{ExecutionContext, Future}

trait BaseSpec extends FlatSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  class MockApiClient(mockingRoute: Route) extends ApiClient {
    override def apply[T: FromResponseUnmarshaller](request: HttpRequest)
                                                   (implicit ec: ExecutionContext): Future[T] = {
      routeThrough(mockingRoute)(request)
    }

    def routeThrough[T: FromResponseUnmarshaller](route: Route)(request: HttpRequest): Future[T] =
      request ~> route ~> (result => Unmarshal(result.response).to[T])
  }

}