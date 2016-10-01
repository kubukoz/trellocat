package com.kubukoz.trellocat.util

import akka.http.scaladsl.model.StatusCodes.{BadGateway, Unauthorized, UnprocessableEntity}
import com.kubukoz.trellocat.BaseSpec
import com.kubukoz.trellocat.util.CustomDirectives.logAndHandleExternalExceptions

import scala.concurrent.Future

class CustomDirectivesTests extends BaseSpec {

  import akka.http.scaladsl.server.Directives._


  "logAndHandleExternalExceptions" should "handle Unauthorized" in {
    val route = logAndHandleExternalExceptions {
      complete(Future.failed[String](StatusCodeException(Unauthorized)))
    }

    Get("/") ~> route ~> check {
      responseAs[String] shouldBe "Unauthorized"
      response.status shouldBe Unauthorized
    }
  }

  it should "log errors" in pending

  it should "handle 422 unprocessable entity" in {
    val route = logAndHandleExternalExceptions {
      complete(Future.failed[String](StatusCodeException(UnprocessableEntity)))
    }

    Get("/") ~> route ~> check {
      responseAs[String] shouldBe "External service error"
      response.status shouldBe BadGateway
    }
  }
}
