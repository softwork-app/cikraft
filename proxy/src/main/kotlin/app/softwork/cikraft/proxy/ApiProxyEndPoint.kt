package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("ProxyEndPoint", namespace = "http://www.sap.com/apimgmt")
public data class ApiProxyEndPoint(
    val default: Boolean,
    @XmlElement
    val name: String,
    @SerialName("base_path")
    @XmlElement
    val basePath: String,
    val properties: Properties? = null,
    val routeRules: RouteRules,
    val faultRules: List<RouteRule> = emptyList(),
    @XmlSerialName("defaultFaultRule")
    @XmlElement
    val defaultFaultRule: FaultRule? = null,
    @XmlSerialName("preFlow")
    val preFlow: Flow,
    @XmlSerialName("postFlow")
    val postFlow: Flow,
    val conditionalFlows: List<String> = emptyList(),
) {
    @Serializable
    @SerialName("routeRules")
    public data class RouteRules(val routes: List<RouteRule>)

    @Serializable
    @SerialName("routeRule")
    public data class RouteRule(
        @XmlElement
        val name: String,
        @XmlElement
        val targetEndPointName: String,
        @XmlElement
        val sequence: Int,
        @XmlElement
        val faultRules: List<RouteRule> = emptyList(),
    )

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
