package ip.foo

import app.softwork.cikraft.SAP_MESSAGE_PROCESSING_LOG_ID_HEADER
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
import kotlin.CharArray
import kotlin.Int
import kotlin.String
import kotlin.uuid.Uuid
import kotlinx.serialization.builtins.serializer
import app.softwork.cikraft.ktor.server.runtime.contentType as runtimeContentType
import io.ktor.server.request.`header` as requestHeader
import io.ktor.server.request.accept as requestAccept
import io.ktor.server.request.contentType as requestContentType
import io.ktor.server.response.`header` as responseHeader
import io.ktor.server.routing.`header` as routingHeader
import io.ktor.server.routing.accept as routingAccept

public fun Route.IFBa(
  a: String = IFBaConfig.a,
  b: Int = IFBaConfig.b,
  d: CharArray,
  e: CharArray,
  other: String,
) {
  val csrfToken = "csrfTokenIFBa" + Uuid.random()
  val csrfServerSessionCookie = "csrfSessionCookieIFBa" + Uuid.random()
  val csrfServerVCAPCookie = "csrfVCAPCookieIFBa" + Uuid.random()
  routingHeader("X-CSRF-Token", "FETCH") {
    head<IFBa> {
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
        post<IFBa> {
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
            acceptContentTypes.any { Json.match(it) } -> JsonFactory to "application/json"
            else -> {
              call.respond(NotAcceptable)
              return@post
            }
          }
          val (errorResponseFactory, errorContentType) = when {
            acceptContentTypes.any { it == Any } ||
            acceptContentTypes.any { Json.match(it) } -> Fault.FaultFactory to "application/json"
            else -> {
              call.respond(NotAcceptable)
              return@post
            }
          }
          val requestContentType = call.request.requestContentType()
          val requestFactory = when {
            requestContentType.match(Any) ||
            Json.match(requestContentType) -> JsonFactory
            else -> {
              call.response.responseHeader("Accept-Post", "application/json")
              call.respond(UnsupportedMediaType)
              return@post
            }
          }
          try {
            val result = IFBaFunction(input = requestFactory.decodeFromString(String.serializer(), call.receiveText()),a = a,b = b,c = call.request.requestHeader("CCC"),d = d,e = e,other = other,)
            call.response.responseHeader("bar", result.bar)
            call.response.responseHeader("ASDF", result.baz)
            call.response.responseHeader("DEFAULT", result.default)
            for ((key, value) in result.headers) {
              call.response.responseHeader(key, value)
            }
            call.response.responseHeader(name = io.ktor.http.HttpHeaders.ContentType, value = responseContentType)
            call.respondText(text = responseFactory.encodeToString(String.serializer(), result.foo))
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
