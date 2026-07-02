package app.softwork.cikraft.proxy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
@SerialName("response")
public data class Response(
    @XmlElement
    val isRequest: Boolean,
    val steps: Steps,
)
