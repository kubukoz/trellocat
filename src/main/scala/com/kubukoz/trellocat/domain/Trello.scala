package com.kubukoz.trellocat.domain

/**
  * Trello-specific domain.
  **/
object Trello {

  /**
    * Trello's board representation.
    **/
  case class Board(id: String, name: String, desc: String)

  case class Column(id: String, name: String, cards: List[Card])

  case class Card(id: String, name: String)

}
