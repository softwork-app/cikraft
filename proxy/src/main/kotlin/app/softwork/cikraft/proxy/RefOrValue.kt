package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
public data class RefOrValue(
    val ref: String? = null,
    @XmlValue
    val value: String? = null,
)
