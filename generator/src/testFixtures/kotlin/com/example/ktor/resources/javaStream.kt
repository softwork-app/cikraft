@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
import com.example.StreamFactory
import com.example.core.Fault
import com.sap.it.api.msglog.MessageLog
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Application.OctetStream
import io.ktor.http.ContentType.Companion.Any
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotAcceptable
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnsupportedMediaType
import io.ktor.server.request.receive
import io.ktor.server.resources.handle
import io.ktor.server.resources.head
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import kotlin.uuid.Uuid
import io.ktor.server.request.`header` as requestHeader
import io.ktor.server.request.accept as requestAccept
import io.ktor.server.request.contentType as requestContentType
import io.ktor.server.response.`header` as responseHeader
import io.ktor.server.routing.`header` as routingHeader
import io.ktor.server.routing.accept as routingAccept
import io.ktor.server.routing.contentType as routingContentType

public fun Route.BazStream(getMessageLog: () -> MessageLog) {
  val csrfToken = "csrfTokenBazStream" + Uuid.random()
  val csrfServerSessionCookie = "csrfSessionCookieBazStream" + Uuid.random()
  val csrfServerVCAPCookie = "csrfVCAPCookieBazStream" + Uuid.random()
  routingHeader("X-CSRF-Token", "FETCH") {
    head<BazStream> {
      call.response.responseHeader("X-CSRF-Token", csrfToken)
      call.response.cookies.append(Cookie(name = "JSESSIONID", value = csrfServerSessionCookie))
      call.response.cookies.append(Cookie(name = "__VCAP_ID__", value = csrfServerVCAPCookie))
      call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
      call.respond(OK)
    }
  }
  routingHeader("X-CSRF-Token", csrfToken) {
    routingContentType(OctetStream) {
      routingAccept(OctetStream) {
        handle<BazStream> {
          val csrfRequestSessionCookie = call.request.cookies["JSESSIONID"]
          val csrfRequestVCAPCookie = call.request.cookies["__VCAP_ID__"]
          if (csrfRequestSessionCookie != csrfServerSessionCookie || csrfRequestVCAPCookie != csrfServerVCAPCookie) {
            call.respond(Forbidden)
            return@handle
          }
          call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
          val acceptContentTypes = call.request.requestAccept()?.let { it.split(",").map { ContentType.parse(it.trim()) }} ?: listOf(Any)
          val (responseFactory, responseContentType) = when {
            acceptContentTypes.any { it == Any } ||
            acceptContentTypes.any { OctetStream.match(it) } -> StreamFactory to "application/octet-stream"
            else -> {
              call.respond(NotAcceptable)
              return@handle
            }
          }
          val (errorResponseFactory, errorContentType) = when {
            acceptContentTypes.any { it == Any } ||
            acceptContentTypes.any { Json.match(it) } -> Fault.ErrorJsonFactory to "application/json"
            else -> {
              call.respond(NotAcceptable)
              return@handle
            }
          }
          val requestContentType = call.request.requestContentType()
          val requestFactory = when {
            requestContentType.match(Any) ||
            OctetStream.match(requestContentType) -> StreamFactory
            else -> {
              call.response.responseHeader("Accept-Post", "application/octet-stream")
              call.respond(UnsupportedMediaType)
              return@handle
            }
          }
          try {
            val result = context(getMessageLog()) {
              BazStreamFunction(body = call.receive(),b = call.request.requestHeader("B"),)
            }
            call.response.responseHeader(name = io.ktor.http.HttpHeaders.ContentType, value = responseContentType)
            call.respond(result.body)
          } catch (exception: Fault) {
            call.response.status(HttpStatusCode.fromValue(exception.httpReturnCode))
            call.response.responseHeader(name = io.ktor.http.HttpHeaders.ContentType, value = errorContentType)
            call.respondText(text = errorResponseFactory.encodeToString(Fault.serializer(), exception.jsonError))
          }
        }
      }
    }
  }
}
