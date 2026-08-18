@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
import com.example.StreamFactory
import com.example.core.Fault
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Application.OctetStream
import io.ktor.http.ContentType.Companion.Any
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.NotAcceptable
import io.ktor.server.resources.post
import io.ktor.server.response.`header`
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import kotlin.uuid.Uuid
import io.ktor.server.request.accept as requestAccept
import io.ktor.server.routing.accept as routingAccept

public fun Route.BazNoBody() {
  routingAccept(OctetStream) {
    post<BazNoBody> {
      call.response.`header`(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
      val acceptContentTypes = call.request.requestAccept()?.let { it.split(",").map { ContentType.parse(it.trim()) }} ?: listOf(Any)
      val (responseFactory, responseContentType) = when {
        acceptContentTypes.any { it == Any } ||
        acceptContentTypes.any { OctetStream.match(it) } -> StreamFactory to "application/octet-stream"
        else -> {
          call.respond(NotAcceptable)
          return@post
        }
      }
      val (errorResponseFactory, errorContentType) = when {
        acceptContentTypes.any { it == Any } ||
        acceptContentTypes.any { Json.match(it) } -> Fault.ErrorJsonFactory to "application/json"
        else -> {
          call.respond(NotAcceptable)
          return@post
        }
      }
      try {
        val result = BazNoBodyFunction()
        call.respond(HttpStatusCode.OK)
      } catch (exception: Fault) {
        call.response.status(HttpStatusCode.fromValue(exception.httpReturnCode))
        call.response.`header`(name = io.ktor.http.HttpHeaders.ContentType, value = errorContentType)
        call.respondText(text = errorResponseFactory.encodeToString(Fault.serializer(), exception.jsonError))
      }
    }
  }
}
