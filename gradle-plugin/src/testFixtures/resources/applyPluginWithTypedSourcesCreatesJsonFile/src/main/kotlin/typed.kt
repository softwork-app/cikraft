import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.Serializable

/**
 * @param b some Header
 * @throws Fault if true
 */
@ScriptEntry
@Throws(Fault::class)
fun typed(
    @Body(JsonFactory::class) body: C<Int>?,
    @Header("B") b: String,
    injected: Boolean,
    ignored: String? = null,
): TypedOutput = error("")

class TypedOutput(
    @Body(JsonFactory::class) val b: D<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(val x: F)

@Serializable
data class D<F>(val x: F, val nested: E)

@Serializable
data class E(val f: String)
