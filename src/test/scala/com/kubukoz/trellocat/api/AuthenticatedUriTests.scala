package com.kubukoz.trellocat.api

import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query
import com.kubukoz.trellocat.BaseSpec
import com.kubukoz.trellocat.api.ApiClient.AuthenticatedUri
import com.kubukoz.trellocat.domain.AuthParams

class AuthenticatedUriTests extends BaseSpec {
  "withAuthQuery" should "merge parameters" in {
    val uri = Uri("sample")

    implicit val authParams = AuthParams(Query("a" -> "aValue", "b" -> "bValue"))
    val newUri = uri.withAuthQuery(Query("c" -> "cValue", "d" -> "dValue"))

    newUri.rawQueryString shouldBe Some("a=aValue&b=bValue&c=cValue&d=dValue")
  }

  it should "not fail for an empty query parameter" in {
    val uri = Uri("sample")

    implicit val authParams = AuthParams(Query("a" -> "aValue", "b" -> "bValue"))
    val newUri = uri.withAuthQuery(Query.Empty)

    newUri.rawQueryString shouldBe Some("a=aValue&b=bValue")
  }
}