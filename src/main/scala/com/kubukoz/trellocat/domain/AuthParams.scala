package com.kubukoz.trellocat.domain

import akka.http.scaladsl.model.Uri.Query

/**
  * Holds information that'll be added to any authenticated request's URI's query string.
  **/
case class AuthParams(authQuery: Query)

object NoParams extends AuthParams(Query.Empty)