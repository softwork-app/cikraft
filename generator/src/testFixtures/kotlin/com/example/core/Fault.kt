package com.example.core

import app.softwork.cikraft.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json

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
    companion object ErrorJsonFactory : StringFormat by Json
}
