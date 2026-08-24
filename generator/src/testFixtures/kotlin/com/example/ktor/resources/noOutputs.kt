@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
import com.example.core.Fault
import com.sap.it.api.msglog.MessageLog
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Companion.Any
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NoContent
import io.ktor.http.HttpStatusCode.Companion.NotAcceptable
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.request.accept
import io.ktor.server.resources.handle
import io.ktor.server.resources.head
import io.ktor.server.resources.resource
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.uuid.Uuid
import io.ktor.server.request.`header` as requestHeader
import io.ktor.server.response.`header` as responseHeader
import io.ktor.server.routing.`header` as routingHeader

public fun Route.BazNoOutputs(
  cc: CharArray,
  dd: CharArray,
  ee: Int? = BazNoOutputsConfig.ee,
  ignored: String?,
  getMessageLog: () -> MessageLog,
) {
  val csrfToken = "csrfTokenBazNoOutputs" + Uuid.random()
  val csrfServerSessionCookie = "csrfSessionCookieBazNoOutputs" + Uuid.random()
  val csrfServerVCAPCookie = "csrfVCAPCookieBazNoOutputs" + Uuid.random()
  routingHeader("X-CSRF-Token", "FETCH") {
    head<BazNoOutputs> {
      call.response.responseHeader("X-CSRF-Token", csrfToken)
      call.response.cookies.append(Cookie(name = "JSESSIONID", value = csrfServerSessionCookie))
      call.response.cookies.append(Cookie(name = "__VCAP_ID__", value = csrfServerVCAPCookie))
      call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
      call.respond(OK)
    }
  }
  routingHeader("X-CSRF-Token", csrfToken) {
    resource<BazNoOutputs> {
      handle<BazNoOutputs> {
        val csrfRequestSessionCookie = call.request.cookies["JSESSIONID"]
        val csrfRequestVCAPCookie = call.request.cookies["__VCAP_ID__"]
        if (csrfRequestSessionCookie != csrfServerSessionCookie || csrfRequestVCAPCookie != csrfServerVCAPCookie) {
          call.respond(Forbidden)
          return@handle
        }
        call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
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
            BazNoOutputsFunction(bb = call.request.requestHeader("B"),cc = cc,dd = dd,ee = ee,ignored = ignored,)
          }
          call.respond(NoContent)
        } catch (exception: Fault) {
          call.response.status(HttpStatusCode.fromValue(exception.httpReturnCode))
          call.response.responseHeader(name = io.ktor.http.HttpHeaders.ContentType, value = errorContentType)
          call.respondText(text = errorResponseFactory.encodeToString(Fault.serializer(), exception.jsonError))
        }
      }
    }
  }
}
