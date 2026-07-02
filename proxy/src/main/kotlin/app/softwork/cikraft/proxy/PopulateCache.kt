package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("PopulateCache", namespace = "http://www.sap.com/apimgmt")
public data class PopulateCache(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    val cacheKey: CacheKey,
    @XmlSerialName("Scope")
    val scope: CacheScope,
    val expirySettings: ExpirySettings,
    @XmlElement
    @XmlSerialName("Source")
    val source: String,
) : Policy {
    @Serializable
    public data class ExpirySettings(
        @XmlElement
        @XmlSerialName("TimeoutInSeconds")
        val timeoutInSeconds: Long,
    )
}
