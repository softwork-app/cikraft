package app.softwork.cikraft.proxy

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

@Serializable
public data class Flow(
    @XmlElement
    val name: String,
    @XmlElement
    val request: Request? = null,
    @XmlElement
    val response: Response? = null,
)
