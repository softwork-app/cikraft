import app.softwork.cikraft.*
import com.example.core.*
import com.example.JsonFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlin.collections.List

/**
 * @param b some Header
 */
@ScriptEntry
@Throws(Fault::class)
@JvmOverloads
fun sealed(
    @Body(JsonFactory::class) body: List<Base>,
    @Header("B") b: String,
    ignored: String? = null,
): SealedOutput = error("")

class SealedOutput(
    @Body(JsonFactory::class) val b: List<Int>,
    @Header("CamelHttpResponseCode") val foo: Int,
)

@Serializable
sealed interface Base {
    val base: String
}

@Serializable
data class Sub(val name: String, override val base: String) : Base

@Serializable
@SerialName("serial")
data class SubSerialName(val foo: Int, override val base: String) : Base
