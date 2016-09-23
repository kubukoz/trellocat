package com.kubukoz.trellocat.domain

object Github {

  case class ProjectStub(name: String)

  case class Project(id: Int, name: String)

  case class Column(id: String, name: String, cards: List[Card])

  case class Card(id: String, name: String)

  case class User(login: String)

}
