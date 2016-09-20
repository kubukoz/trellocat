package com.kubukoz.trellocat.domain

/**
  * Trello-specific domain.
  **/
object Trello {

  /**
    * Trello's board representation.
    **/
  case class Board(id: String, name: String, desc: String)

}
