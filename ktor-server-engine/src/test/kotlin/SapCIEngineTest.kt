import app.softwork.cikraft.*
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.jsonIo
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import kotlin.test.*

class SapCIEngineTest {
    private fun Application.testing() {
        install(ContentNegotiation) {
            jsonIo()
        }

        routing {
            route("foo") {
                post {
                    val body = call.receiveText()
                    call.response.header("TEST", "ff")
                    call.respondText(body, status = HttpStatusCode.Created)
                }
            }
            route("json") {
                post {
                    val body = call.receive<Foo>()
                    call.respond(HttpStatusCode.Created, body.copy(bar = body.bar + body.bar))
                }
            }
        }
    }

    @Serializable
    data class Foo(val bar: String)

    @Test
    fun testing() {
        testApplication {
            application {
                testing()
            }
            val client = createClient {
                install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                    jsonIo()
                }
            }
            val response = client.post("foo") {
                setBody("bar")
            }
            assertEquals("bar", response.bodyAsText())
            assertEquals("ff", response.headers["TEST"])
            assertEquals(HttpStatusCode.Created, response.status)

            val jsonResponse = client.post("json") {
                contentType(ContentType.Application.Json)
                setBody(Foo("bar"))
            }
            assertEquals(Foo("barbar"), jsonResponse.body<Foo>())
            assertEquals("application/json", jsonResponse.headers["Content-Type"])
            assertEquals(HttpStatusCode.Created, jsonResponse.status)
        }

        val s = "bar".byteInputStream()

        runBlocking {
            val response = sapCIServer(
                MessageImpl(
                    body = s,
                    properties = mapOf(
                        "CamelHttpUrl" to "http://foo:443/foo",
                        "CamelHttpMethod" to "POST",
                        "CamelHttpQuery" to "foo=bar",
                    ),
                ),
            ) {
                testing()
            }
            assertEquals("bar", (response.body as ByteArrayOutputStream).toString())
            assertEquals("ff", response.getHeader("TEST", String::class.java))
            assertEquals(201, response.getHeader("CamelHttpResponseCode", Int::class.java))

            val jsonResponse = sapCIServer(
                MessageImpl(
                    // language=json
                    body = """{"bar": "bar"}""".byteInputStream(),
                    properties = mapOf(
                        "CamelHttpUrl" to "http://foo:443/json",
                        "CamelHttpMethod" to "POST",
                        "CamelHttpQuery" to "foo=bar",
                    ),
                    headers = mapOf(
                        "Content-Type" to "application/json",
                    ),
                ),
            ) {
                testing()
            }
            // language=json
            assertEquals("""{"bar":"barbar"}""", (jsonResponse.body as ByteArrayOutputStream).toString())
            assertEquals("application/json", jsonResponse.headers["Content-Type"])
            assertEquals(201, jsonResponse.getHeader("CamelHttpResponseCode", Int::class.java))
        }
    }
}
