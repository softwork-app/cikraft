import app.softwork.cikraft.*
import com.example.core.Fault
import com.example.JsonFactory
import kotlinx.serialization.Serializable

/**
 * @param b some Header
 */
@ScriptEntry
@Throws(Fault::class)
fun bar(
    @Body(JsonFactory::class) body: Bar?,
    @Header("B") b: String?,
    ignored: String? = null,
): BarOutput = error("")

class BarOutput(
    @Body(JsonFactory::class) val bar: Bar,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
data class Bar(val f: String)
