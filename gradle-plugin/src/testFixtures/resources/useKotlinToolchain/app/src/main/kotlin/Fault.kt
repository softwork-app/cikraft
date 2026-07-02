package com.example.core

import app.softwork.cikraft.*
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.StringFormat
import kotlinx.serialization.modules.SerializersModule

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

    @Body(ErrorJsonFactory::class)
    val jsonError: Fault get() = this

    @ContentType("application/json", parameters = ["charset=utf-8"])
    companion object ErrorJsonFactory : StringFormat {
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
