package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class AssignTo(
    val createNew: Boolean = false,
    val type: String? = null,
    @XmlValue
    val value: String,
)
