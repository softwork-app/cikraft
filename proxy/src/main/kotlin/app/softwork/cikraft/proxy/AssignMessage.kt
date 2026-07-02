package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("AssignMessage", "http://www.sap.com/apimgmt")
public data class AssignMessage(
    override val async: Boolean = false,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    @XmlSerialName("Set")
    val set: SetBuilder? = null,
    @XmlSerialName("Remove")
    val remove: RemoveBuilder? = null,
    @XmlSerialName("AssignVariable")
    val assignVariable: AssignVariable? = null,
    @SerialName("IgnoreUnresolvedVariables")
    @XmlElement
    val ignoreUnresolvedVariables: Boolean = false,
    val assignTo: AssignTo,
) : Policy {
    @Serializable
    public data class SetBuilder(
        val headers: Headers,
        @SerialName("Payload")
        @XmlElement
        val payload: String? = null,
        @SerialName("StatusCode")
        @XmlElement
        val statusCode: String? = null,
        @SerialName("ReasonPhrase")
        @XmlElement
        val reasonPhrase: String? = null,
    )

    @Serializable
    public data class Headers(val headers: List<Header>)

    @Serializable
    public data class Header(val name: String, @XmlValue val value: String? = null)

    @Serializable
    public data class RemoveBuilder(val headers: Headers)

    @Serializable
    public data class AssignVariable(
        @SerialName("Name")
        @XmlElement
        val name: String,
        @SerialName("Ref")
        @XmlElement
        val ref: String,
    )
}
