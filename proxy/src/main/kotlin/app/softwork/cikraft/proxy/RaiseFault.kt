package app.softwork.cikraft.proxy

import app.softwork.cikraft.proxy.AssignMessage.*
import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("RaiseFault", "http://www.sap.com/apimgmt")
public data class RaiseFault(
    override val async: Boolean = false,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    @SerialName("FaultResponse")
    val faultResponse: FaultResponse,
    @SerialName("IgnoreUnresolvedVariables")
    @XmlElement
    val ignoreUnresolvedVariables: Boolean = true,
) : Policy {
    @Serializable
    public data class FaultResponse(
        @XmlSerialName("Set")
        val set: SetBuilder? = null,
        @XmlSerialName("Remove")
        val remove: RemoveBuilder? = null,
    )
}
