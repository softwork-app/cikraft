import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @param b some Header
 */
@ScriptEntry
@Throws(Fault::class)
fun typed(
    @Body(JsonFactory::class) body: C<Int>?,
    @Header("B") b: String?,
    ignored: String? = null,
): TypedOutput = error("")

class TypedOutput(
    @Body(JsonFactory::class) val b: D<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(@SerialName("x") val f: F)

@Serializable
data class D<F>(val x: F, val nested: E)

@Serializable
data class E(val f: String)
