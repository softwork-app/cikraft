import com.example.JsonFactory
import app.softwork.cikraft.*
import com.example.core.Fault
import kotlinx.serialization.*

@ScriptEntry
fun injectedBoolean() = true

@ScriptEntry
@Throws(Fault::class)
fun typed(
    @Body(JsonFactory::class) body: C<Int>?,
    @Header("B") b: String,
    injected: Boolean,
    ignored: String? = null,
): TypedOutput = error("")

data class TypedOutput(
    @Body(JsonFactory::class) val b: D<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@ScriptEntry
@Throws(Fault::class)
fun star(
    @Body(JsonFactory::class) body: C<Int>?,
    @Header("B") b: String,
    injected: Boolean,
    ignored: String? = null,
): StarOutput = error("")

data class StarOutput(
    @Body(JsonFactory::class) val b: D<*>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(val x: F)

@Serializable
data class D<F>(val x: F, val nested: E)

@Serializable
data class E(val f: String)
