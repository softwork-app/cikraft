package app.softwork.cikraft.integrationflow

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.*

@Serializable
public data class Property(
    @XmlSerialName("key", namespace = "")
    @XmlElement
    val key: String,
    @XmlSerialName("value", namespace = "")
    @XmlElement
    val value: String,
)
