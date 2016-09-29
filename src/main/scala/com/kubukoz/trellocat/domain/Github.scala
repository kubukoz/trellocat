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
    * Representation of a column to be created.
    **/
  case class ColumnStub(name: String)

  /**
    * A list of cards.
    **/
  case class Column(name: String, number: Int)

  /**
    * A GitHub Project card
    **/
  case class Card(name: String)

  /**
    * A GitHub user.
    **/
  case class User(login: String)

  /**
    * A repository.
    **/
  case class Repo(name: String)

}
