package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("SpikeArrest", "http://www.sap.com/apimgmt")
public data class SpikeArrest(
    override val async: Boolean = true,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    @SerialName("Identifier")
    val identifier: Identifier? = null,
    @SerialName("Rate")
    @XmlElement
    val rate: String,
    @SerialName("UseEffectiveCount")
    @XmlElement
    val useEffectiveCount: Boolean?,
    @SerialName("MessageWeight")
    @XmlElement
    val messageWeight: MessageWeight? = null,
) : Policy {
    @Serializable
    public data class Identifier(val ref: String)

    @Serializable
    public data class MessageWeight(val ref: String)
}
