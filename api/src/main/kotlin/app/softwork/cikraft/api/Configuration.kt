package app.softwork.cikraft.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class Configuration(
    @SerialName("ParameterKey") val parameterKey: String,
    @SerialName("ParameterValue") val parameterValue: String,
    @SerialName("DataType") val dataType: String,
)
