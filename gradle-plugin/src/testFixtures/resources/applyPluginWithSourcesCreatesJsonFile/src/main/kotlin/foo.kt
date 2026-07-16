package com.example

import com.example.core.*
import app.softwork.cikraft.*
import kotlinx.serialization.Serializable

/**
 * @param b some Header
 * @param c Foo
 * @throws Fault if true
 */
@ScriptEntry
@Throws(Fault::class)
fun foo(
    @Body(JsonFactory::class) body: FooInput,
    @Header("B") b: String?,
    @Password c: CharArray,
    @Password d: CharArray,
    @Parameter e: Int?,
    km: javax.net.ssl.KeyManager,
    @Property ds: javax.sql.DataSource?,
    injected: Boolean,
    ignored: String? = null,
): FooOutput = error("")

/**
 * Foo input sample
 * @param s asdf
 */
@Serializable
data class FooInput(
    val s: String,
)
