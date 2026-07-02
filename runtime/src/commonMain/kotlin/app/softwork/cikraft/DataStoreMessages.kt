package app.softwork.cikraft

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlId
import nl.adaptivity.xmlutil.serialization.XmlSerialName
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
@SerialName("messages")
public data class DataStoreMessages<T>(
    @XmlSerialName("message")
    val messages: List<DataStoreMessage<T>>,
) : Map<String, T> by messages.associateBy({
    it.id
}, {
    it.content
})

@Serializable
public data class DataStoreMessage<T>(
    @XmlId
    val id: String,
    @XmlValue
    val content: T,
)
