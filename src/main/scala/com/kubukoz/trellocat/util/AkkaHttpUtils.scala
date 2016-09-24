package com.kubukoz.trellocat.util

import akka.http.scaladsl.marshalling.{Marshal, Marshaller}
import akka.http.scaladsl.model.RequestEntity

import scala.concurrent.ExecutionContext

trait AkkaHttpUtils {
  type ToEntityMarshaller[T] = Marshaller[T, RequestEntity]

  protected def toEntity[T: ToEntityMarshaller](value: T)(implicit ec: ExecutionContext) = Marshal(value).to[RequestEntity]
}
