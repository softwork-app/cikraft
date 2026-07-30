package com.example

import com.example.core.*
import app.softwork.cikraft.*

/**
 * @throws Fault if true
 */
@ScriptEntry
@Throws(Fault::class)
suspend fun fooSuspend(
    @Body(JsonFactory::class) body: String,
    @Header("B") b: String?,
    @Password c: CharArray,
    @Password d: CharArray,
    @Property("E") e: Int,
    ignored: String? = null,
): FooOutput = error("")
