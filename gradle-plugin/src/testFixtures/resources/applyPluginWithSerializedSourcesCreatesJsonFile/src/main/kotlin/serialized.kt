import com.example.JsonFactory
import app.softwork.cikraft.*
import com.example.core.Fault
import kotlinx.serialization.*

/**
 * @param body some Body
 * @param b some Header
 * @throws Fault if true
 */
@ScriptEntry
@Throws(Fault::class)
fun serialized(
    @Body(JsonFactory::class) body: B,
    @Header("B") b: String,
    ignored: String? = null,
): SerializedOutput = error("")

/**
 * @param b some Output body
 */
data class SerializedOutput(
    @Body(JsonFactory::class) val b: B,
)

@Serializable
data class B(val x: Int)
