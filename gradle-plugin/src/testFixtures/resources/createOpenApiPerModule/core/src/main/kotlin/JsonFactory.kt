package com.example

import app.softwork.cikraft.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

@ContentType("application/json", parameters = ["charset=utf-8"])
object JsonFactory: StringFormat {
    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String
    ): T {
        TODO("Not yet implemented")
    }

    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T
    ): String {
        TODO("Not yet implemented")
    }

    override val serializersModule: SerializersModule
        get() = TODO("Not yet implemented")
}
