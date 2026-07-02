package app.softwork.cikraft.proxy

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
public data class CacheKey(
    @XmlElement
    @XmlSerialName("KeyFragment")
    val keyFragment: String,
)
