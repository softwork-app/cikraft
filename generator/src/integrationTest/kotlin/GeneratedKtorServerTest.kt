import com.example.ktor.resources.BazTwo
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.ContentType.Application.Json
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlin.uuid.*

@ExperimentalUuidApi
class GeneratedKtorServerTest {

    @Test
    fun requestCSRFToken() = testApplication {
        application {
            install(io.ktor.server.resources.Resources)
            routing {
                BazTwo(
                    ignored = "",
                    ignored2 = null,
                    injected = false,
                )
            }
        }

        val client = createClient {
            install(HttpCookies)
            install(Resources)
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }

        val token = client.head(BazTwo) {
            header("X-CSRF-Token", "FETCH")
            expectSuccess = true
        }.headers["X-CSRF-Token"]
        assertNotNull(token)
    }

    @Test
    fun testContentTypeHandling() {
        assertTrue(Json.match(ContentType.Application.Any))
        assertTrue(Json.withParameter("foo", "bar").match(Json))
    }

    @Test
    fun useCorrectContentTypeNegotiationHeaders() = testApplication {
        application {
            install(io.ktor.server.resources.Resources)
            routing {
                BazTwo(
                    ignored = "",
                    ignored2 = null,
                    injected = false,
                )
            }
        }

        val client = createClient {
            install(HttpCookies)
            install(Resources)
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }

        val token = client.head(BazTwo) {
            header("X-CSRF-Token", "FETCH")
            expectSuccess = true
        }.headers["X-CSRF-Token"]
        assertNotNull(token)

        val result = client.post(BazTwo) {
            header("X-CSRF-Token", token)
            header("B", "f")
            contentType(Json.withParameter("foo", "bar"))
            setBody("""{ "s": "42" }""")
            accept(Json)
        }
        assertEquals(418, result.status.value)
    }

    @Test
    fun acceptAll() = testApplication {
        application {
            install(io.ktor.server.resources.Resources)
            routing {
                BazTwo(
                    ignored = "",
                    ignored2 = null,
                    injected = false,
                )
            }
        }

        val client = createClient {
            install(HttpCookies)
            install(Resources)
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }
        }

        val token = client.head(BazTwo) {
            header("X-CSRF-Token", "FETCH")
            expectSuccess = true
        }.headers["X-CSRF-Token"]
        assertNotNull(token)

        val resultAcceptAll = client.post(BazTwo) {
            header("X-CSRF-Token", token)
            header("B", "f")
            contentType(Json.withParameter("foo", "bar"))
            setBody("""{ "s": "42" }""")
        }
        assertEquals(418, resultAcceptAll.status.value)
    }
}
