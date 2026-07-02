package com.example

import com.sap.gateway.ip.core.customdev.util.*
import com.sap.it.api.msglog.*
import app.softwork.cikraft.*
import com.example.core.Fault
import kotlinx.io.*
import kotlinx.serialization.Serializable
import java.io.*
import javax.net.ssl.KeyManager
import javax.sql.*

@ScriptEntry
@Throws(Fault::class)
fun foo(
    @Body(JsonFactory::class) body: FooInput,
    @Header("B") b: String,
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    km: KeyManager,
    @Property ds: DataSource?,
    injected: Boolean,
    ignored: String? = null,
): FooOutput = FooOutput(
    body = "Some",
    foo = "some property",
    fooHeader = 42,
    headers = mapOf("ff" to "Some Header"),
    optionalHeader = null,
)

/**
 * Foo input sample
 * @param s asdf
 */
@Serializable
data class FooInput(
    val s: String,
)

@ScriptEntry
@Throws(Fault::class)
suspend fun fooSuspend(
    @Body(JsonFactory::class) body: String,
    @Header("B") b: String,
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    ignored: String? = null,
): FooOutput = error("")

@ScriptEntry
fun noError(
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    ignored: String? = null,
): FooOutput = error("")

@ScriptEntry
@Throws(Fault::class)
fun noOutputs(
    @Header("B") bb: String,
    @Password cc: CharArray,
    @Password dd: CharArray,
    @Parameter ee: Int?,
    ignored: String? = null,
): Unit = error("")

@ScriptEntry
fun raw(rawMessage: Message, rawMessageLog: MessageLog, rawNullableMessageLog: MessageLog? = null) {}

@ScriptEntry
suspend fun rawSuspend(rawMessage: Message, rawMessageLog: MessageLog) {}

@ScriptEntry
fun setup(): Boolean = true

@ScriptEntry
@Throws(Fault::class)
suspend fun twoPart1(
    @Body(JsonFactory::class) body: FooInput,
    @Header("B") b: String,
    ignored: String? = null,
    injected: Boolean,
): TwoPart1Output = throw Fault("", "", 418, 418, "")

class TwoPart1Output(
    @Body(Nothing::class) val body: FooOutput2,
)

@ScriptEntry
@Throws(Fault::class)
suspend fun dummy(
    @Body(Nothing::class) body: FooOutput2,
    @Header("B") b: String,
    ignored2: String? = null,
) {}

@ScriptEntry
suspend fun dummyWithOutput(
    @Body(Nothing::class) body: FooOutput2,
    @Header("B") b: String,
    ignored2: String? = null,
): DummyOutput = error("")

class DummyOutput(
    @Header("D") val d: String,
)

@ScriptEntry
@Throws(Fault::class)
suspend fun twoPart2(
    @Body(Nothing::class) body: FooOutput2,
    @Header("B") b: String,
    ignored2: String? = null,
): FooOutput = throw Fault("", "", 418, 418, "")

@ScriptEntry
@Throws(Fault::class)
fun javaStreams(
    @Body(JsonFactory::class) body: InputStream,
    @Header("B") b: String,
): StreamOutput = error("")

class StreamOutput(
    @Body(JsonFactory::class) val body: InputStream,
)

@ScriptEntry
@Throws(Fault::class)
fun kotlinxIO(
    @Body(JsonFactory::class) body: Source,
    @Header("B") b: String,
    rawNullableMessageLog: MessageLog? = null,
): SourceOutput = error("")

class SourceOutput(
    @Body(JsonFactory::class) val body: Source,
)

@ScriptEntry
@Throws(Fault::class)
fun fooDataStore(
    @Body(XmlFactory::class) entries: DataStoreMessages<String>,
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    km: KeyManager,
    @Property ds: DataSource?,
    injected: Boolean,
    ignored: String? = null,
): FooDataStoreOutput = FooDataStoreOutput(
    body = "Some",
    foo = "some property",
    fooHeader = 42,
    headers = mapOf("ff" to "Some Header"),
    optionalHeader = null,
)

public class FooDataStoreOutput(
    @Body(JsonFactory::class) val body: String,
    @Property val foo: String,
    @Header(STATUS_CODE_HEADER) val fooHeader: Int,
    @Header("X-FOO") val optionalHeader: String?,
    @DynamicHeaders val headers: Map<String, String>,
)
