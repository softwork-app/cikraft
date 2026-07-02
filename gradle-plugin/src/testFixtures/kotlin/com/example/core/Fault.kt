package com.example.core

import kotlinx.serialization.KSerializer
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

/**
 * Some Fault
 * @param input a message
 */
public data class Fault(
    override val message: String,
    val input: String?,
    val statusCode: Int?,

    public val httpReturnCode: Int,
    val sapMessageProcessingLogID: String,
) : Exception(message) {

    val jsonError: Fault get() = this

    companion object FaultFactory : StringFormat by Json {
        fun serializer(): KSerializer<Fault> = TODO()
    }
}
