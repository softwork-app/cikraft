@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
import com.example.core.Fault
import com.sap.it.api.msglog.MessageLog
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Companion.Any
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotAcceptable
import io.ktor.server.request.accept
import io.ktor.server.resources.handle
import io.ktor.server.response.`header`
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import kotlin.uuid.Uuid

public fun Route.BazNoBody(getMessageLog: () -> MessageLog) {
  handle<BazNoBody> {
    call.response.`header`(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
    val acceptContentTypes = call.request.accept()?.let { it.split(",").map { ContentType.parse(it.trim()) }} ?: listOf(Any)
    val (errorResponseFactory, errorContentType) = when {
      acceptContentTypes.any { it == Any } ||
      acceptContentTypes.any { Json.match(it) } -> Fault.ErrorJsonFactory to "application/json"
      else -> {
        call.respond(NotAcceptable)
        return@handle
      }
    }
    try {
      val result = context(getMessageLog()) {
        BazNoBodyFunction()
      }
      call.respond(NoContent)
    } catch (exception: Fault) {
      call.response.status(HttpStatusCode.fromValue(exception.httpReturnCode))
      call.response.`header`(name = io.ktor.http.HttpHeaders.ContentType, value = errorContentType)
      call.respondText(text = errorResponseFactory.encodeToString(Fault.serializer(), exception.jsonError))
    }
  }
}
