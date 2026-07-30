import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.Serializable
import kotlin.collections.List

/**
 * @param b some Header
 */
@ScriptEntry
@Throws(Fault::class)
@JvmOverloads
fun typed(
    @Body(JsonFactory::class) body: List<D<Int>>,
    @Header("B") b: String?,
    ignored: String? = null,
): TypedOutput = error("")

class TypedOutput(
    @Body(JsonFactory::class) val b: List<C<Int>>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(val x: F)

@Serializable
data class D<F>(val x: Int, val y: List<Int>, val z: List<I>)

@Serializable
data class I(val ii: String)
