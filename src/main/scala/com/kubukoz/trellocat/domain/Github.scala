package com.kubukoz.trellocat.domain

/**
  * Github-specific domain.
  **/
object Github {

  /**
    * Representation of a project to be created.
    **/
  case class ProjectStub(name: String)

  /**
    * A project in a repo.
    **/
  case class Project(id: Int, name: String, number: Int)

  /**
    * A list of cards.
    **/
  case class Column(name: String)

  /**
    * A GitHub Project card
    **/
  case class Card(id: String, name: String)

  /**
    * A GitHub user.
    **/
  case class User(login: String)

}
