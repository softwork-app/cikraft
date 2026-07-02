package app.softwork.cikraft.api.proxy

import app.softwork.cikraft.api.Deferred
import app.softwork.cikraft.api.LifeCycle
import app.softwork.cikraft.api.Metadata
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ApiProxy(
    @SerialName("__metadata")
    val metadata: Metadata,
    val isPublished: Boolean = false,
    val isVersioned: Boolean = false,
    @SerialName("life_cycle")
    val lifeCycle: LifeCycle,
    val cfBindingId: String? = null,
    val hasChanges: Boolean,
    val isUnmanaged: Boolean,
    val isCopy: Boolean,
    val name: String,
    val apiVersionGroup: String? = null,
    @SerialName("provider_name")
    val providerName: String?,
    @SerialName("service_code")
    val serviceCode: ServiceCode = ServiceCode.REST,
    val state: State,
    @SerialName("status_code")
    val statusCode: StatusCode,
    val releaseStatus: String,
    val releaseMetadata: String? = null,
    val version: String,
    val title: String,
) {
    @Serializable
    public data class New(
        val name: String,
        val version: String = "1",
        val title: String,
        val releaseStatus: String? = null,
        val releaseMetadata: String? = null,
        val description: String? = null,
        val isPublished: Boolean = false,
        @SerialName("service_code")
        val serviceCode: ServiceCode = ServiceCode.REST,
        @SerialName("provider_name")
        val providerName: String = "NONE",
        @SerialName("status_code")
        val statusCode: StatusCode,
        val state: State,
        // val shortText: String? = null,
        // val policyTemplateNames: String? = null,
        val proxyEndPoints: List<ProxyEndPoint> = emptyList(),
        val targetEndPoints: List<TargetEndPoint> = emptyList(),
        val isVersioned: Boolean? = null,
        val apiProvider: ApiProviderRef? = null,
    ) {
        @Serializable
        public data class ApiProviderRef(val name: String)

        @Serializable
        public data class ProxyEndPoint(
            @SerialName("base_path")
            val basePath: String,
            val name: String = "default",
            val isDefault: Boolean,
            val apiResources: List<String>,
            val conditionalFlows: List<String>,
            val properties: List<String>,
            val routeRules: List<RouteRule>,
            val virtualhosts: List<Virtualhost>,
        ) {
            @Serializable
            public data class RouteRule(val name: String, val targetEndPointName: String, val sequence: Int)

            @Serializable
            public data class Virtualhost(
                @SerialName("__metadata")
                val metadata: Metadata,
            ) {
                @Serializable
                public data class Metadata(val uri: String)
            }
        }

        @Serializable
        public data class TargetEndPoint(
            val name: String = "default",
            val isDefault: Boolean = false,
            val url: String? = null,
            val relativePath: String? = null,
            @SerialName("provider_id")
            val providerId: String?,
            val properties: List<String> = emptyList(),
            val targetAPIProxyName: String? = null,
        )
    }

    @Serializable
    public enum class ServiceCode {
        REST,
        ODATA,
        SOAP,
    }

    @Serializable
    public enum class State {
        DEPLOYED,
        UNDEPLOYED,
        DRAFTLOCAL,
        EXTERNAL,
    }

    @Serializable
    public enum class StatusCode {
        EXTERNAL,
        REGISTERED,
        PUBLISHED,
    }
}

@Serializable
public data class Provider(
    @SerialName("__metadata") val metadata: Metadata,
    val name: String,
    val region: String? = null,
    val title: String? = name,
    @SerialName("rt_auth")
    val rtAuth: RtAuth? = null,
    val url: String? = null,
    val destType: DestType,
    val isOnPremise: Boolean,
    val sslInfo: SslInfo? = null,
    @SerialName("life_cycle")
    val lifeCycle: Lifecycle? = null,
    val description: String? = null,
    val host: String? = null,
    val port: Int? = null,
    val useSSL: Boolean? = null,
    val trustAll: Boolean? = null,
    val userName: String? = null,
    val password: String? = null,
    val clientSecret: String? = null,
    val clientId: String? = null,
    val tokenUrl: String? = null,
    val authType: String? = null,
    val pathPrefix: String? = null,
    val timeout: Int? = null,
    @SerialName("cockpit_url")
    val cockpitUrl: String? = null,
    val cloudConnectorLocation: String? = null,
    val orgSecret: String? = null,
    val userSecret: String? = null,
    val keystorePassword: String? = null,
    val keystoreLocation: String? = null,
    val additionalProperties: Deferred? = null,
    val apiProxies: Deferred? = null,
    val envKVMs: Deferred? = null,
    val resources: Deferred? = null,
    val targetEndPoints: Deferred? = null,
) {
    @Serializable
    public data class Lifecycle(
        @SerialName("__metadata") val metadata: Metadata,
        @SerialName("changed_at") val changedAt: String? = null,
        @SerialName("changed_by") val changedBy: String? = null,
        @SerialName("created_at") val createdAt: String? = null,
        @SerialName("created_by") val createdBy: String? = null,
    )

    @Serializable
    public data class New(
        val description: String? = null,
        val destType: DestType? = null,
        val host: String,
        val name: String,
        val trustAll: Boolean? = null,
        val title: String? = null,
        val authType: AuthType,
        val port: Int,
        val pathPrefix: String? = null,
        val useSSL: Boolean,
        val isOnPremise: Boolean? = null,
        val cloudConnectorLocation: String? = null,
        @SerialName("rt_auth") val rtAuth: RtAuth?,
        val url: String? = null,
        val userName: String? = null,
        val password: String? = null,
        val sslInfo: SslInfo? = null,
        val clientSecret: String? = null,
        val clientId: String? = null,
        val tokenUrl: String? = null,
        val keystoreLocation: String? = null,
        val keystorePassword: String? = null,
    )

    @Serializable
    public data class SslInfo(
        @SerialName("__metadata") val metadata: Metadata? = null,
        val ciphers: String? = null,
        val clientAuthEnabled: Boolean? = null,
        val enabled: Boolean? = null,
        val ignoreValidationErrors: Boolean? = null,
        val keyAlias: String? = null,
        val keyStore: String? = null,
        val protocols: String? = null,
        val trustStore: String? = null,
    )

    @Serializable
    public enum class RtAuth {
        @SerialName("NONE")
        None,

        @SerialName("PRINCIPALPROPAGATION")
        PrincipalPropagation,
    }

    @Serializable
    public enum class AuthType {
        @SerialName("NONE")
        None,

        @SerialName("BASIC")
        Basic,
        OAuth20,
        CLIENTCERTIFICATEAUTHENTICATION,
    }

    @Serializable
    public enum class DestType {
        @SerialName("CPIDISCOVERY")
        CPIDiscovery,
        ODATA,

        @SerialName("CPIRUNTIME")
        CPIRuntime,
    }
}
