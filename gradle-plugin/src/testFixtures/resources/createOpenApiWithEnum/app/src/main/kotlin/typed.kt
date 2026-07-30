import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@ScriptEntry
fun injectBoolean() = true

/**
 * @param b some Header
 */
@ScriptEntry
@Throws(Fault::class)
fun typed(
    @Header("B") b: String?,
    injected: Boolean,
    ignored: String? = null,
): TypedOutput = error("")

class TypedOutput(
    @Body(JsonFactory::class) val b: D<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(val x: F)

/**
 * @param e Some Enum description at D
 */
@Serializable
data class D<F>(val x: F, val nested: E, val e: ENUM)

@Serializable
data class E(val f: String)

@Serializable
enum class ENUM {
    @SerialName("foo") FOO,
    Bar
}
