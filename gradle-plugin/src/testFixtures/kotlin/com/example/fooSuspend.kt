package com.example

import app.softwork.cikraft.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun fooSuspend(
    body: String,
    b: String,
    c: CharArray,
    d: CharArray,
    e: Int,
): FooOutput = error("")

class FooOutput(
    @Body(JsonFormat::class) val foo: String,
    @Header val bar: String,
    @Header("ASDF") val baz: String,
    @Header("DEFAULT") val default: String = "default",
    @DynamicHeaders val headers: Map<String, String>,
)

@ContentType("application/json", parameters = ["charset=utf-8"])
object JsonFormat : StringFormat by Json
