package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("BasicAuthentication", "http://www.sap.com/apimgmt")
public data class BasicAuthentication(
    val operation: Operation,
    override val async: Boolean = false,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    @SerialName("IgnoreUnresolvedVariables")
    @XmlElement
    val ignoreUnresolvedVariables: Boolean = false,
    @XmlSerialName("User")
    val user: RefOrValue,
    @XmlSerialName("Password")
    val password: RefOrValue,
    @XmlSerialName("AssignTo") val assignTo: AssignTo? = null,
    @XmlElement
    @XmlSerialName("Source") val source: String? = null,
) : Policy {
    @Serializable
    public enum class Operation {
        Encode,
        Decode,
    }
}
