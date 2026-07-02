package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("ExtractVariables", namespace = "http://www.sap.com/apimgmt")
public data class ExtractVariables(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    @XmlElement @XmlSerialName("Source") val source: String,
    @XmlElement @XmlSerialName("VariablePrefix") val variablePrefix: String,
    @XmlElement @XmlSerialName("JSONPayload") val jsonPayload: JsonPayload,
) : Policy {
    @Serializable
    public data class JsonPayload(val variables: List<Variable>)

    @Serializable
    public data class Variable(
        val name: String,
        @XmlElement
        @SerialName("JSONPath")
        val jsonPath: String,
    )
}
