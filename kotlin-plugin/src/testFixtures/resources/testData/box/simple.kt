import app.softwork.cikraft.Body
import app.softwork.cikraft.DynamicHeaders
import app.softwork.cikraft.Header
import app.softwork.cikraft.Password
import app.softwork.cikraft.Property
import app.softwork.cikraft.Parameter
import app.softwork.cikraft.STATUS_CODE_HEADER
import app.softwork.cikraft.ScriptEntry
import kotlinx.serialization.Serializable
import app.softwork.cikraft.ContentType
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule

/**
 * Some Fault
 * @param input a message
 */
@Serializable
public data class Fault(
    override val message: String,
    val input: String?,
    val statusCode: Int?,
    @Header("CamelHttpResponseCode") public val httpReturnCode: Int,
    val sapMessageProcessingLogID: String,
) : Exception(message) {

    @Body(ErrorJsonFactory::class)
    val jsonError: Fault get() = this

    @ContentType("application/json")
    companion object ErrorJsonFactory : StringFormat {
        override fun <T> decodeFromString(
            deserializer: DeserializationStrategy<T>,
            string: String,
        ): T {
            TODO("Not yet implemented")
        }

        override fun <T> encodeToString(
            serializer: SerializationStrategy<T>,
            value: T,
        ): String {
            TODO("Not yet implemented")
        }

        override val serializersModule: SerializersModule
            get() = TODO("Not yet implemented")
    }
}

@ContentType("application/json", parameters = ["foo=value", "bar=value"])
data object JsonFactory : StringFormat by Json(builderAction = {
    prettyPrint = true
})

@ScriptEntry
@Throws(Fault::class)
fun foo(
    @Body(JsonFactory::class) body: FooInput,
    @Header("B") b: String,
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    km: javax.net.ssl.KeyManager,
    ignored: String? = null,
): FooOutput = error("")

/**
 * Foo input sample
 * @param s asdf
 */
@Serializable
data class FooInput(
    val s: String,
)

public class FooOutput(
    @Body(JsonFactory::class) val body: String,
    @Property("FOO") val foo: String,
    @Header(STATUS_CODE_HEADER) val fooHeader: Int,
    @DynamicHeaders val headers: Map<String, String>,
)

fun box() = "OK"
