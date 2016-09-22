package com.kubukoz.trellocat.domain

/**
  * Trello-specific domain.
  **/
object Trello {

  /**
    * Trello's board representation.
    **/
  case class Board(id: String, name: String)

  /**
    * Trello card list (column).
    **/
  case class Column(id: String, name: String, cards: List[Card]) {
    def toGithub: Github.Column = Github.Column(id, name, cards.map(_.toGithub))
  }

  /**
    * Trello card.
    **/
  case class Card(id: String, name: String) {
    def toGithub: Github.Card = Github.Card(id, name)
  }

}
