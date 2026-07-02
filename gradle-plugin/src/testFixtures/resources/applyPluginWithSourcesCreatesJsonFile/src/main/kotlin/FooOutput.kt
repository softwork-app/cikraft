package com.example

import app.softwork.cikraft.*

public class FooOutput(
    @Body(JsonFactory::class) val body: String,
    @Property("FOO") val foo: String,
    @Header("CamelHttpResponseCode") val fooHeader: Int,
    @Header("X-FOO") val optionalHeader: String?,
    @DynamicHeaders val headers: Map<String, String>,
)
