package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("Javascript", "http://www.sap.com/apimgmt")
public data class Javascript(
    override val async: Boolean = true,
    override val continueOnError: Boolean = false,
    override val enabled: Boolean = true,

    val timeLimit: Long,
    @SerialName("ResourceURL")
    @XmlElement
    val resourceUrl: String,

    @SerialName("IncludeURL")
    @XmlElement
    val includeUrLs: List<String> = emptyList(),
) : Policy
