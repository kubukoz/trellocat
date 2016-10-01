package com.kubukoz.trellocat.util

import akka.actor.ActorSystem
import akka.event.Logging
import akka.event.Logging.LogEvent
import akka.http.scaladsl.model.StatusCodes.{BadGateway, Unauthorized, UnprocessableEntity}
import akka.http.scaladsl.server.Directives._
import akka.testkit.EventFilter
import com.kubukoz.trellocat.BaseSpec
import com.kubukoz.trellocat.util.CustomDirectives.logAndHandleExternalExceptions
import com.typesafe.config.ConfigFactory

import scala.concurrent.Future

class CustomDirectivesTests extends BaseSpec {
  def errorContains(fragment: String): PartialFunction[LogEvent, Boolean] = {
    case event if event.level == Logging.ErrorLevel =>
      event.message.toString.contains(fragment)
  }

  override protected def createActorSystem(): ActorSystem =
    ActorSystem("customDirectivesTests", ConfigFactory.parseString(
      """
        |akka {
        | loggers = ["akka.testkit.TestEventListener"]
        |}
      """.stripMargin))


  "logAndHandleExternalExceptions" should "handle Unauthorized" in {
    val route = logAndHandleExternalExceptions {
      complete(Future.failed[String](StatusCodeException(Unauthorized)))
    }

    EventFilter.custom(errorContains("Invalid token"), occurrences = 1) intercept {
      Get("/") ~> route ~> check {
        responseAs[String] shouldBe "Unauthorized"
        response.status shouldBe Unauthorized
      }
    }
  }

  it should "handle 422 unprocessable entity" in {
    val route = logAndHandleExternalExceptions {
      complete(Future.failed[String](StatusCodeException(UnprocessableEntity)))
    }

    EventFilter.custom(errorContains("Exception in request"), occurrences = 1) intercept {
      Get("/") ~> route ~> check {
        responseAs[String] shouldBe "External service error"
        response.status shouldBe BadGateway
      }
    }
  }
}
