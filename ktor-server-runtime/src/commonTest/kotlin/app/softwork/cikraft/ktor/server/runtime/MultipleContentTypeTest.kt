package app.softwork.cikraft.ktor.server.runtime

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.request.contentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class MultipleContentTypeTest {
    @Test
    fun success() = testApplication {
        application {
            routing {
                contentType(ContentType.Application.Json, ContentType.Application.Soap) {
                    get {
                        val contentType = call.request.contentType()
                        call.respondText(contentType.toString())
                    }
                }
            }
        }

        val client = createClient {
            expectSuccess = true
        }

        val responseSoapContentType = client.get {
            contentType(ContentType.Application.Soap)
        }.bodyAsText()
        assertEquals(ContentType.Application.Soap, ContentType.parse(responseSoapContentType))

        val responseJsonContentType = client.get {
            contentType(ContentType.Application.Json)
        }.bodyAsText()
        assertEquals(ContentType.Application.Json, ContentType.parse(responseJsonContentType))
    }

    @Test
    fun unsupported() = testApplication {
        application {
            routing {
                contentType(ContentType.Application.Json, ContentType.Application.Soap) {
                    get {
                        val contentType = call.request.contentType()
                        call.respondText(contentType.toString())
                    }
                }
            }
        }

        val responseStatusCode = client.get {
            contentType(ContentType.Text.CSV)
        }.status
        assertEquals(HttpStatusCode.UnsupportedMediaType, responseStatusCode)
    }

    @Test
    fun noContentTypeHeader() = testApplication {
        application {
            routing {
                contentType(ContentType.Application.Json, ContentType.Application.Soap) {
                    get {
                        val contentType = call.request.contentType()
                        call.respondText(contentType.toString())
                    }
                }
            }
        }

        val responseStatusCode = client.get {}.status
        assertEquals(HttpStatusCode.UnsupportedMediaType, responseStatusCode)
    }
}
