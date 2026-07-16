import app.softwork.cikraft.*
import com.example.core.Fault
import com.example.JsonFactory

@ScriptEntry
@Throws(Fault::class)
fun test(
    @Body(JsonFactory::class) input: String,
    @Parameter a: String,
    @Parameter b: Int,
    @Header("CCC") c: String?,
    @Password d: CharArray,
    @Password e: CharArray,
    @Suppress("unused") other: String = "",
): TestResult {
    return TestResult(
        "foo",
        "bar",
        "baz",
        headers = mapOf("a" to "b"),
    )
}

class TestResult(
    @Body(JsonFactory::class) val foo: String,
    @Header val bar: String,
    @Header("ASDF") val baz: String,
    @Header("DEFAULT") val default: String = "default",
    @DynamicHeaders val headers: Map<String, String>,
)
