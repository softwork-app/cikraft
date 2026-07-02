package app.softwork.cikraft.proxy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("request")
public data class Request(
    @XmlElement
    val isRequest: Boolean,
    val steps: Steps,
)
