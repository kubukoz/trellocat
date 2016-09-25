package com.kubukoz.trellocat.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * JSON formats for the domain.
  **/
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val trelloBoardFormat = jsonFormat2(Trello.Board.apply)
  implicit val trelloCardFormat = jsonFormat2(Trello.Card.apply)
  implicit val trelloColumnFormat = jsonFormat2(Trello.Column.apply)

  implicit val githubProjectStubFormat = jsonFormat1(Github.ProjectStub.apply)
  implicit val githubProjectFormat = jsonFormat3(Github.Project.apply)
  implicit val githubUserFormat = jsonFormat1(Github.User.apply)
  implicit val githubCardFormat = jsonFormat1(Github.Card.apply)
  implicit val githubColumnStubFormat = jsonFormat1(Github.ColumnStub.apply)
  implicit val githubColumnFormat = jsonFormat2(Github.Column.apply)
}