package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("ServiceCallout", namespace = "http://www.sap.com/apimgmt")
public data class ServiceCallout(
    override val async: Boolean,
    override val continueOnError: Boolean,
    override val enabled: Boolean,
    @SerialName("Request")
    val request: Request? = null,

    @XmlElement
    @XmlSerialName("Response")
    val response: String?,
    @XmlElement
    @XmlSerialName("Timeout")
    val timeout: Long,
    val httpTargetConnection: HTTPTargetConnection,
) : Policy {
    @Serializable
    public data class Request(
        val variable: String,
        val clearPayload: Boolean,
        @XmlSerialName("Set")
        val set: AssignMessage.SetBuilder? = null,
    )

    @Serializable
    public data class HTTPTargetConnection(
        @XmlElement
        @XmlSerialName("URL")
        val url: String,

        @XmlElement
        @XmlSerialName("SSLInfo")
        val sslInfo: SSLInfo? = null,
    ) {
        @Serializable
        public data class SSLInfo(
            @XmlElement @XmlSerialName("Enabled") val enabled: Boolean,
            @XmlElement @XmlSerialName("ClientAuthEnabled") val clientAuthEnabled: Boolean,
            @XmlElement @XmlSerialName("KeyStore") val keyStore: String,
            @XmlElement @XmlSerialName("KeyAlias") val keyAlias: String? = null,
            @XmlElement @XmlSerialName("TrustStore") val trustStore: String? = null,
        )
    }
}
