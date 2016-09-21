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
  case class Column(id: String, name: String, cards: List[Card])

  /**
    * Trello card.
    **/
  case class Card(id: String, name: String)

}
