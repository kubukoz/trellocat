package com.kubukoz.trellocat.domain

/**
  * Trello-specific domain.
  **/
object Trello {

  /**
    * Trello's board representation.
    **/
  case class Board(id: String, name: String) {
    def toGithubStub: Github.ProjectStub = Github.ProjectStub(name)
  }

  /**
    * Trello card list (column).
    **/
  case class Column(name: String, cards: List[Card]) {
    def toGithubStub: Github.ColumnStub = Github.ColumnStub(name)
  }

  /**
    * Trello card.
    **/
  case class Card(id: String, name: String) {
    def toGithubStub: Github.Card = Github.Card(name)
  }

}
