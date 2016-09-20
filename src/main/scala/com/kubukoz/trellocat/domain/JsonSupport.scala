package com.kubukoz.trellocat.domain

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

/**
  * JSON formats for the domain.
  **/
trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val trelloBoardFormat = jsonFormat3(Trello.Board.apply)
  implicit val trelloCardFormat = jsonFormat2(Trello.Card.apply)
  implicit val trelloColumnFormat = jsonFormat3(Trello.Column.apply)
}