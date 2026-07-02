package app.softwork.cikraft.proxy

import kotlinx.serialization.*
import nl.adaptivity.xmlutil.serialization.*

@Serializable
@XmlSerialName("APIProxy", "http://www.sap.com/apimgmt")
public data class ApiProxy(
    @XmlElement
    val name: String,
    @XmlElement
    val title: String,
    @XmlElement
    val description: String?,
    @XmlElement
    val isVersioned: Boolean,
    @XmlElement
    @XmlSerialName("service_code")
    val serviceCode: ServiceCode,
    @XmlElement
    @XmlSerialName("APIState")
    val apiState: ApiState,
    @XmlSerialName("proxyEndPoints")
    val proxyEndPoints: ProxyEndPoints,
    @XmlSerialName("targetEndPoints")
    val targetEndPoints: TargetEndPoints,
    @XmlSerialName("policies")
    val policies: Policies,
    @XmlSerialName("fileResources")
    val fileResources: FileResources? = null,
) {
    @Serializable
    public data class ProxyEndPoints(val proxyEndPoint: List<ProxyEndPoint>)

    @Serializable
    @XmlSerialName("proxyEndPoint")
    public data class ProxyEndPoint(
        @XmlElement
        val proxyEndPointName: String,
        @XmlElement
        val apiResourceName: String,
    )

    @Serializable
    public data class TargetEndPoints(val targetEndPoint: List<String>)

    @Serializable
    public data class Policies(val policy: List<Policy>)

    @Serializable
    @XmlSerialName("policy")
    public data class Policy(
        val type: String,
        @XmlValue
        val name: String,
    ) {
        @Serializable
        public enum class PolicyType {
            AssignMessage,
            Quota,
            RaiseFault,
            SpikeArrest,
            BasicAuth,
            KeyValueMapOperations,
            PopulateCache,
            LookupCache,
            ServiceCallout,
            VerifyJWT,
            DecodeJWT,
            ExtractVariable,
            Javascript,
        }
    }

    @Serializable
    @XmlSerialName("fileResources")
    public data class FileResources(val fileResources: List<FileResource>)

    @Serializable
    @XmlSerialName("fileResource")
    public data class FileResource(val type: String, @XmlValue val name: String)
}
