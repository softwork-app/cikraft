package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("LookupCache", namespace = "http://www.sap.com/apimgmt")
public data class LookupCache(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    val cacheKey: CacheKey,
    @XmlSerialName("Scope")
    val scope: CacheScope,
    @XmlElement
    @XmlSerialName("AssignTo")
    val assignTo: String,
) : Policy
