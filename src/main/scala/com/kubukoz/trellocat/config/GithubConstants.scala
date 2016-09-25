package com.kubukoz.trellocat.config

import akka.http.scaladsl.model.headers.Accept

trait GithubConstants {
  val baseUrl = "https://api.github.com"
  val userUrl = s"$baseUrl/user"

  val acceptInertia = Accept.parseFromValueString("application/vnd.github.inertia-preview+json, application/json").right.get
}
