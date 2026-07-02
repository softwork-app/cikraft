package com.example

import app.softwork.cikraft.Body
import app.softwork.cikraft.Header
import app.softwork.cikraft.STATUS_CODE_HEADER
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule

fun binaryRedirect(): BinaryRedictResult = error("")

data class BinaryRedictResult(
    @param:Header("Location") val location: String,
    @param:Header(STATUS_CODE_HEADER) val status: Int = 302,
    @param:Body(OctectStream::class) val nothing: Nothing? = null,
)

data object OctectStream: StringFormat {
    override fun <T> encodeToString(
        serializer: SerializationStrategy<T>,
        value: T
    ): String = TODO("Not yet implemented")

    override fun <T> decodeFromString(
        deserializer: DeserializationStrategy<T>,
        string: String
    ): T {
        TODO("Not yet implemented")
    }

    override val serializersModule: SerializersModule
        get() = TODO("Not yet implemented")
}
