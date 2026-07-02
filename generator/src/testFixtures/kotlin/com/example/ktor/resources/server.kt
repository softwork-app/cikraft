@file:Suppress("detekt.Indentation", "detekt.ImportOrdering")

package com.example.ktor.resources

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
import com.example.FooInput
import com.example.JsonFactory
import com.example.core.Fault
import io.ktor.http.ContentType
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.ContentType.Companion.Any
import io.ktor.http.Cookie
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.Forbidden
import io.ktor.http.HttpStatusCode.Companion.NotAcceptable
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.http.HttpStatusCode.Companion.UnsupportedMediaType
import io.ktor.server.request.receiveText
import io.ktor.server.resources.head
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import javax.net.ssl.KeyManager
import javax.sql.DataSource
import kotlin.Boolean
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid
import kotlinx.serialization.builtins.serializer
import app.softwork.cikraft.ktor.server.runtime.contentType as runtimeContentType
import io.ktor.server.request.`header` as requestHeader
import io.ktor.server.request.accept as requestAccept
import io.ktor.server.request.contentType as requestContentType
import io.ktor.server.response.`header` as responseHeader
import io.ktor.server.routing.`header` as routingHeader
import io.ktor.server.routing.accept as routingAccept

@ExperimentalUuidApi
public fun Route.BazA(
  c: CharArray,
  d: CharArray,
  e: Int? = BazAConfig.e,
  km: KeyManager,
  ds: DataSource?,
  injected: Boolean,
  ignored: String?,
) {
  val csrfToken = "csrfTokenBazA" + Uuid.random()
  val csrfServerSessionCookie = "csrfSessionCookieBazA" + Uuid.random()
  val csrfServerVCAPCookie = "csrfVCAPCookieBazA" + Uuid.random()
  routingHeader("X-CSRF-Token", "FETCH") {
    head<BazA> {
      call.response.responseHeader("X-CSRF-Token", csrfToken)
      call.response.cookies.append(Cookie(name = "JSESSIONID", value = csrfServerSessionCookie))
      call.response.cookies.append(Cookie(name = "__VCAP_ID__", value = csrfServerVCAPCookie))
      call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
      call.respond(OK)
    }
  }
  routingHeader("X-CSRF-Token", csrfToken) {
    runtimeContentType(Json) {
      routingAccept(Json) {
        post<BazA> {
          val csrfRequestSessionCookie = call.request.cookies["JSESSIONID"]
          val csrfRequestVCAPCookie = call.request.cookies["__VCAP_ID__"]
          if (csrfRequestSessionCookie != csrfServerSessionCookie || csrfRequestVCAPCookie != csrfServerVCAPCookie) {
            call.respond(Forbidden)
            return@post
          }
          call.response.responseHeader(SAP_MESSAGE_PROCESSING_LOG_ID_HEADER, Uuid.random().toString())
          val acceptContentTypes = call.request.requestAccept()?.let { it.split(",").map { ContentType.parse(it.trim()) }} ?: listOf(Any)
          val (responseFactory, responseContentType) = when {
            acceptContentTypes.any { it == Any } ||
            acceptContentTypes.any { Json.withParameter("charset", "utf-8").match(it) } -> JsonFactory to "application/json; charset=utf-8"
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
          val requestContentType = call.request.requestContentType()
          val requestFactory = when {
            requestContentType.match(Any) ||
            Json.withParameter("charset", "utf-8").match(requestContentType) -> JsonFactory
            else -> {
              call.response.responseHeader("Accept-Post", "application/json")
              call.respond(UnsupportedMediaType)
              return@post
            }
          }
          try {
            val result = BazAFunction(body = requestFactory.decodeFromString(FooInput.serializer(), call.receiveText()),b = call.request.requestHeader("B")!!,c = c,d = d,e = e,km = km,ds = ds,injected = injected,ignored = ignored,)
            call.response.status(HttpStatusCode.fromValue(result.fooHeader))
            if (result.optionalHeader != null) {
              call.response.responseHeader("X-FOO", result.optionalHeader)
            }
            for ((key, value) in result.headers) {
              call.response.responseHeader(key, value)
            }
            call.response.responseHeader(name = io.ktor.http.HttpHeaders.ContentType, value = responseContentType)
            call.respondText(text = responseFactory.encodeToString(String.serializer(), result.body))
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
