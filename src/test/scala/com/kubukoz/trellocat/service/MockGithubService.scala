package com.kubukoz.trellocat.service

import com.kubukoz.trellocat.domain.Github._

import scala.concurrent.{ExecutionContext, Future}

//noinspection NotImplementedCode
class MockGithubService extends GithubService {
  override def createProject(user: User, repoName: String, projectStub: ProjectStub)(implicit ec: ExecutionContext): Future[Project] = ???

  override def createCard(user: User, project: Project, repoName: String, column: Column, card: Card)(implicit ec: ExecutionContext): Future[Card] = ???

  override def createColumn(user: User, project: Project, repoName: String, columnStub: ColumnStub)(implicit ec: ExecutionContext): Future[Column] = ???

  override def getUser()(implicit ec: ExecutionContext): Future[User] = ???
}
