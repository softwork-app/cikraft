package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("TargetEndPoint", "http://www.sap.com/apimgmt")
public data class TargetEndPoint(
    @XmlElement
    val name: String,
    @XmlElement
    val url: String,
    @SerialName("provider_id")
    @XmlElement
    val providerId: String,
    @XmlElement
    val additionalAPIProviders: List<String>,
    @XmlElement
    val isDefault: Boolean,
    @XmlElement
    val relativePath: String?,
    val properties: Properties? = null,
    @XmlElement
    val faultRules: List<FaultRule>,
    @XmlSerialName("defaultFaultRule")
    @XmlElement
    val defaultFaultRule: FaultRule? = null,
    @XmlSerialName("preFlow")
    val preFlow: Flow,
    @XmlSerialName("postFlow")
    val postFlow: Flow,
    @XmlElement
    val conditionalFlows: List<String>,
    @XmlElement
    val loadBalancerConfigurations: LoadBalancerConfigurations,
) {
    @Serializable
    @XmlSerialName("loadBalancerConfigurations")
    public data class LoadBalancerConfigurations(
        @XmlElement
        val isRetry: Boolean,
        @XmlElement
        val healthMonitor: HealthMonitor,
    ) {
        @Serializable
        @XmlSerialName("healthMonitor")
        public data class HealthMonitor(
            @XmlElement
            val isEnabled: Boolean,
        )
    }

    @Serializable
    @XmlSerialName("properties")
    public data class Properties(val properties: List<Property>)

    @Serializable
    @XmlSerialName("property")
    public data class Property(
        @XmlElement
        val name: String,
        @XmlElement
        val value: String,
    )
}
