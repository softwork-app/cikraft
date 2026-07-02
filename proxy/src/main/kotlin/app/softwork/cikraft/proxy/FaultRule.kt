package app.softwork.cikraft.proxy

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
public data class FaultRule(
    @XmlElement
    val name: String,
    @XmlElement
    val condition: String? = null,
    @XmlElement
    val alwaysEnforce: Boolean? = null,
    val steps: Steps,
)
