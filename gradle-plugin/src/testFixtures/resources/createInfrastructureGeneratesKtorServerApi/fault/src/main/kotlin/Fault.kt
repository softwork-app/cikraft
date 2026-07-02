package com.example.core

import app.softwork.cikraft.*
import kotlinx.serialization.*
import kotlinx.serialization.modules.*

/**
 * Some Fault
 * @param input a message
 */
@Serializable
public data class Fault(
    override val message: String,
    val input: String?,
    val statusCode: Int?,
    @Header("CamelHttpResponseCode") public val httpReturnCode: Int,
    val sapMessageProcessingLogID: String,
) : Exception(message) {

    @Body(FaultFactory::class)
    val jsonError: Fault get() = this

    @ContentType("application/json")
    companion object FaultFactory: StringFormat {
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
}
