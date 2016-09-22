package com.kubukoz.trellocat.domain

object Github {

  case class Project(id: String, name: String)

  case class Column(id: String, name: String, cards: List[Card])

  case class Card(id: String, name: String)

}
