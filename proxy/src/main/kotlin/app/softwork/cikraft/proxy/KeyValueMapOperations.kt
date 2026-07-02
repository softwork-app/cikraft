package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("KeyValueMapOperations", "http://www.sap.com/apimgmt")
public data class KeyValueMapOperations(
    val mapIdentifier: String,
    override val async: Boolean = false,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,
    val gets: List<Get>,
) : Policy {
    @Serializable
    public data class Get(val assignTo: String, val index: Int = 1, val key: Key) {
        @Serializable
        public data class Key(
            @XmlElement
            @XmlSerialName("Parameter")
            val parameter: String,
        )
    }
}
