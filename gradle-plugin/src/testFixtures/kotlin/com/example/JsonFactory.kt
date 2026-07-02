package com.example

import kotlinx.serialization.*
import kotlinx.serialization.modules.*

object JsonFactory : StringFormat {
    override val serializersModule: SerializersModule
        get() = TODO("Not yet implemented")

    override fun <T> decodeFromString(deserializer: DeserializationStrategy<T>, string: String): T {
        TODO("Not yet implemented")
    }

    override fun <T> encodeToString(serializer: SerializationStrategy<T>, value: T): String {
        TODO("Not yet implemented")
    }
}
