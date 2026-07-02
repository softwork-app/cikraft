package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("DecodeJWT", namespace = "http://www.sap.com/apimgmt")
public data class DecodeJWT(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    @XmlElement
    @XmlSerialName("Source")
    val source: String,
) : Policy
