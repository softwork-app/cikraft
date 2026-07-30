import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.Serializable

@ScriptEntry
@Throws(Fault::class)
fun typed(
    @Body(JsonFactory::class) body: C<Int>?,
    @Header("B") b: String?,
    ignored: String? = null,
): TypedOutput = error("")

class TypedOutput(
    @Body(JsonFactory::class) val b: C<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class C<F>(val x: F)
