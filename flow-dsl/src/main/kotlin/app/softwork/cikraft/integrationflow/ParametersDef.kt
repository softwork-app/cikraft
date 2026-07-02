package app.softwork.cikraft.integrationflow

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@SerialName("parameters")
public data class ParametersDef(
    val parameters: List<Parameter>,
    @SerialName("param_references") val paramReferences: Unit = Unit,
) {
    @Serializable
    @SerialName("parameter")
    public data class Parameter(
        val key: Unit = Unit,
        @XmlElement
        val name: String,
        @XmlElement
        val type: String = "xsd:string",
        @XmlElement
        val isRequired: Boolean = false,
        val constraint: Unit = Unit,
        val description: Unit = Unit,
        val additionalMetadata: Unit = Unit,
    )
}
