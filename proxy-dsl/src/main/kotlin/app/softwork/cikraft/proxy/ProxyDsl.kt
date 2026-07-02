package app.softwork.cikraft.proxy

import app.softwork.cikraft.proxy.CacheScope.*
import app.softwork.cikraft.proxy.VerifyJWT.Algorithm.*
import kotlinx.io.bytestring.ByteString
import org.intellij.lang.annotations.Language
import kotlin.properties.*
import kotlin.time.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@DslMarker
public annotation class ProxyDSL

public fun apiProxy(
    name: String,
    title: String,
    description: String? = null,
    isVersioned: Boolean = false,
    serviceCode: ServiceCode = REST,
    apiState: ApiState = ApiState.Active,
    builder: ApiProxyBuilder.() -> Unit,
): ApiProxyTransport {
    val builder = ApiProxyBuilder(
        name = name,
        title = title,
        description = description,
        isVersioned = isVersioned,
        serviceCode = serviceCode,
        apiState = apiState,
    ).apply(builder)

    return builder.toTransport()
}

@ProxyDSL
public class ApiProxyBuilder internal constructor(
    private val name: String,
    private val title: String,
    private val description: String?,
    private val isVersioned: Boolean,
    private val serviceCode: ServiceCode,
    private val apiState: ApiState,
) {
    private val resources: MutableMap<String, ByteString> = mutableMapOf()

    public fun resource(name: String, content: ByteString) {
        resources[name] = content
    }

    internal fun toTransport(): ApiProxyTransport = ApiProxyTransport(
        apiProxy = ApiProxy(
            name = name,
            title = title,
            description = description,
            isVersioned = isVersioned,
            serviceCode = serviceCode,
            apiState = apiState,
            policies = ApiProxy.Policies(
                policies.policies.map { (name, type) ->
                    ApiProxy.Policy(
                        type = when (type) {
                            is AssignMessageBuilder -> ApiProxy.Policy.PolicyType.AssignMessage
                            is QuotaBuilder -> ApiProxy.Policy.PolicyType.Quota
                            is SpikeArrestBuilder -> ApiProxy.Policy.PolicyType.SpikeArrest
                            is RaiseFaultBuilder -> ApiProxy.Policy.PolicyType.RaiseFault
                            is BasicAuthenticationBuilder -> ApiProxy.Policy.PolicyType.BasicAuth
                            is KeyValueMapOperationsBuilder -> ApiProxy.Policy.PolicyType.KeyValueMapOperations
                            is PopulateCacheBuilder -> ApiProxy.Policy.PolicyType.PopulateCache
                            is LookupCacheBuilder -> ApiProxy.Policy.PolicyType.LookupCache
                            is ServiceCalloutBuilder -> ApiProxy.Policy.PolicyType.ServiceCallout
                            is VerifyJWTBuilder -> ApiProxy.Policy.PolicyType.VerifyJWT
                            is DecodeJWTBuilder -> ApiProxy.Policy.PolicyType.DecodeJWT
                            is ExtractVariablesBuilder -> ApiProxy.Policy.PolicyType.ExtractVariable
                            is JavaScriptBuilder -> ApiProxy.Policy.PolicyType.Javascript
                        }.name,
                        name = name,
                    )
                },
            ),
            proxyEndPoints = ApiProxy.ProxyEndPoints(
                proxyEndpoints.map {
                    ApiProxy.ProxyEndPoint(
                        proxyEndPointName = it.name,
                        apiResourceName = "SWAGGER_JSON",
                    )
                },
            ),
            targetEndPoints = ApiProxy.TargetEndPoints(
                listOf(
                    "default",
                ),
            ),
            fileResources = if (resources.isNotEmpty()) {
                ApiProxy.FileResources(
                    resources.map {
                        val (name, type) = it.key.split(".")
                        ApiProxy.FileResource(
                            name = name,
                            type = type,
                        )
                    },
                )
            } else {
                null
            },
        ),
        policies = policies.policies.mapValues { (_, builder) ->
            when (builder) {
                is AssignMessageBuilder -> builder.build()
                is QuotaBuilder -> builder.build()
                is SpikeArrestBuilder -> builder.build()
                is RaiseFaultBuilder -> builder.build()
                is KeyValueMapOperationsBuilder -> builder.build()
                is BasicAuthenticationBuilder -> builder.build()
                is PopulateCacheBuilder -> builder.build()
                is LookupCacheBuilder -> builder.build()
                is ServiceCalloutBuilder -> builder.build()
                is VerifyJWTBuilder -> builder.build()
                is DecodeJWTBuilder -> builder.build()
                is ExtractVariablesBuilder -> builder.build()
                is JavaScriptBuilder -> builder.build()
            }
        },
        proxyEndpoints = proxyEndpoints,
        targetEndPoint = targetEndpoints,
        resources = resources,
    )

    public val policies: PoliciesDelegate = PoliciesDelegate()

    private val proxyEndpoints = mutableListOf<ApiProxyEndPoint>()

    public fun proxyEndPoint(
        name: String = "default",
        isDefault: Boolean = true,
        builder: ProxyEndpointBuilder.() -> Unit,
    ) {
        val endpointBuilder = ProxyEndpointBuilder().apply(builder)
        proxyEndpoints.add(endpointBuilder.build(name, isDefault))
    }

    private val targetEndpoints = mutableListOf<TargetEndPoint>()

    public fun targetEndPoint(
        name: String = "default",
        isDefault: Boolean = true,
        builder: TargetEndpointBuilder.() -> Unit,
    ) {
        val endpointBuilder = TargetEndpointBuilder().apply(builder)
        targetEndpoints.add(endpointBuilder.build(name, isDefault))
    }
}

@ProxyDSL
public class PoliciesDelegate internal constructor() {
    internal val policies = mutableMapOf<String, PolicyBuilder>()

    public fun quota(
        builder: QuotaBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, QuotaBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = QuotaBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun assignMessage(
        builder: AssignMessageBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, AssignMessageBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = AssignMessageBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun spikeArrest(
        builder: SpikeArrestBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, SpikeArrestBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = SpikeArrestBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun raiseFault(
        builder: RaiseFaultBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, RaiseFaultBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = RaiseFaultBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun keyValueMapOperations(
        mapIdentifier: String,
        builder: KeyValueMapOperationsBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, KeyValueMapOperationsBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = KeyValueMapOperationsBuilder(property.name, mapIdentifier).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun basicAuth(
        builder: BasicAuthenticationBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, BasicAuthenticationBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = BasicAuthenticationBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun populateCache(
        builder: PopulateCacheBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, PopulateCacheBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = PopulateCacheBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun lookupCache(
        builder: LookupCacheBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, LookupCacheBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = LookupCacheBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun verifyJWT(
        builder: VerifyJWTBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, VerifyJWTBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = VerifyJWTBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }
    public fun decodeJWT(
        builder: DecodeJWTBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, DecodeJWTBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = DecodeJWTBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }

    public fun serviceCallout(
        builder: ServiceCalloutBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, ServiceCalloutBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = ServiceCalloutBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }
    public fun extractVariables(
        builder: ExtractVariablesBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, ExtractVariablesBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = ExtractVariablesBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }
    public fun javaScript(
        builder: JavaScriptBuilder.() -> Unit,
    ): ReadOnlyProperty<Nothing?, JavaScriptBuilder> = ReadOnlyProperty { _, property ->
        val policyBuilder = JavaScriptBuilder(property.name).apply(builder)
        policies[property.name] = policyBuilder
        policyBuilder
    }
}

@ProxyDSL
public class ProxyEndpointBuilder internal constructor() {
    internal fun build(name: String, isDefault: Boolean): ApiProxyEndPoint = ApiProxyEndPoint(
        name = name,
        default = isDefault,
        basePath = basePath,
        properties = if (properties.isEmpty()) null else ApiProxyEndPoint.Properties(properties),
        routeRules = ApiProxyEndPoint.RouteRules(
            listOf(
                ApiProxyEndPoint.RouteRule(
                    name = "default",
                    targetEndPointName = "default",
                    sequence = 1,
                ),
            ),
        ),
        faultRules = emptyList(),
        defaultFaultRule = defaultFaultRule,
        preFlow = preFlow ?: Flow("PreFlow"),
        postFlow = postFlow ?: Flow("PostFlow"),
        conditionalFlows = emptyList(),
    )

    private val properties = mutableListOf<ApiProxyEndPoint.Property>()

    public fun property(name: String, value: String) {
        properties.add(ApiProxyEndPoint.Property(name, value))
    }

    public var basePath: String = ""

    private var preFlow: Flow? = null
    private var postFlow: Flow? = null
    private var defaultFaultRule: FaultRule? = null

    public fun preFlow(builder: FlowBuilder.() -> Unit) {
        val flowBuilder = FlowBuilder().apply(builder)
        preFlow = Flow(
            name = "PreFlow",
            request = Request(
                isRequest = true,
                steps = Steps(flowBuilder.steps),
            ),
        )
    }

    public fun postFlow(builder: FlowBuilder.() -> Unit) {
        val flowBuilder = FlowBuilder().apply(builder)
        postFlow = Flow(
            name = "PostFlow",
            response = Response(
                isRequest = false,
                steps = Steps(flowBuilder.steps),
            ),
        )
    }

    public fun defaultFaultRule(builder: FlowBuilder.() -> Unit) {
        defaultFaultRule = FaultRule(
            name = "defaultfaultRule",
            alwaysEnforce = true,
            condition = null,
            steps = Steps(FlowBuilder().apply(builder).steps),
        )
    }
}

@ProxyDSL
public class TargetEndpointBuilder internal constructor() {
    internal fun build(name: String, isDefault: Boolean): TargetEndPoint = TargetEndPoint(
        name = name,
        url = url,
        providerId = providerId,
        relativePath = relativePath,
        additionalAPIProviders = emptyList(),
        isDefault = isDefault,
        properties = if (properties.isEmpty()) null else TargetEndPoint.Properties(properties),
        faultRules = emptyList(),
        defaultFaultRule = defaultFaultRule,
        preFlow = preFlow ?: Flow("PreFlow"),
        postFlow = postFlow ?: Flow("PostFlow"),
        conditionalFlows = emptyList(),
        loadBalancerConfigurations = requireNotNull(loadBalancerConfigurations),
    )

    private val properties = mutableListOf<TargetEndPoint.Property>()

    public fun property(name: String, value: String) {
        properties.add(TargetEndPoint.Property(name, value))
    }

    public var url: String = ""
    public var relativePath: String? = null
    public var providerId: String = "NONE"
    private var loadBalancerConfigurations: TargetEndPoint.LoadBalancerConfigurations? = null

    public fun loadBalancerConfigurations(builder: LoadBalancerConfigurationsBuilder.() -> Unit) {
        loadBalancerConfigurations = LoadBalancerConfigurationsBuilder().apply(builder).build()
    }

    private var preFlow: Flow? = null
    private var postFlow: Flow? = null
    private var defaultFaultRule: FaultRule? = null

    public fun preFlow(builder: FlowBuilder.() -> Unit) {
        val flowBuilder = FlowBuilder().apply(builder)
        preFlow = Flow(
            name = "PreFlow",
            request = Request(
                isRequest = true,
                steps = Steps(flowBuilder.steps),
            ),
        )
    }

    public fun postFlow(builder: FlowBuilder.() -> Unit) {
        val flowBuilder = FlowBuilder().apply(builder)
        postFlow = Flow(
            name = "PostFlow",
            response = Response(
                isRequest = false,
                steps = Steps(flowBuilder.steps),
            ),
        )
    }

    public fun defaultFaultRule(builder: FlowBuilder.() -> Unit) {
        defaultFaultRule = FaultRule(
            name = "defaultfaultRule",
            alwaysEnforce = true,
            condition = null,
            steps = Steps(FlowBuilder().apply(builder).steps),
        )
    }
}

@ProxyDSL
public class LoadBalancerConfigurationsBuilder internal constructor() {
    public var isRetry: Boolean = false
    public var healthMonitorIsEnabled: Boolean = false

    internal fun build(): TargetEndPoint.LoadBalancerConfigurations = TargetEndPoint.LoadBalancerConfigurations(
        isRetry = isRetry,
        healthMonitor = TargetEndPoint.LoadBalancerConfigurations.HealthMonitor(
            isEnabled = healthMonitorIsEnabled,
        ),
    )
}

@ProxyDSL
public class FlowBuilder internal constructor() {
    internal val steps = mutableListOf<Steps.Step>()

    public fun step(policyName: String, condition: String? = null) {
        steps.add(Steps.Step(policyName, condition, steps.size + 1))
    }

    public fun step(policyBuilder: PolicyBuilder, condition: String? = null) {
        step(policyBuilder.name, condition)
    }

    public val PolicyBuilder.failed: String get() = "$prefix.$name.failed"

    public infix fun String.eq(other: String): String = "$this == $other"

    public infix fun String.neq(other: String): String = "$this != $other"

    public infix fun String.eq(other: Boolean): String = "$this == \"$other\""

    public infix fun String.neq(other: Boolean): String = "$this != \"$other\""

    public infix fun String.or(other: String): String = "($this) OR ($other)"

    public infix fun String.and(other: String): String = "($this) AND ($other)"
}

@ProxyDSL
public class AssignMessageBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "assignmessage"

    override var async: Boolean = false
    override var continueOnError: Boolean = false
    override var enabled: Boolean = true

    private val set = SetBuilder()

    public fun set(builder: SetBuilder.() -> Unit) {
        set.apply(builder)
    }

    public val remove: MutableSet<String> = mutableSetOf()

    public fun removeHeader(vararg headers: String) {
        remove.addAll(headers)
    }

    public var ignoreUnresolvedVariables: Boolean = false

    private var createNew: Boolean = false
    private var type: Type = Type.Request

    public fun assignTo(createNew: Boolean = false, type: Type = Type.Request) {
        this.createNew = createNew
        this.type = type
    }

    private var assignVariable: AssignMessage.AssignVariable? = null

    public fun assignVariable(name: String, ref: String) {
        this.assignVariable = AssignMessage.AssignVariable(name, ref)
    }

    internal fun build(): AssignMessage = AssignMessage(
        set = set.toBuilder(),
        remove = remove.toBuilder(),
        assignTo = AssignTo(
            createNew = createNew,
            type = when (type) {
                Type.Request -> "request"
                Type.Response -> "response"
            },
            value = when (type) {
                Type.Request -> "request"
                Type.Response -> "response"
            },
        ),
        assignVariable = assignVariable,
        ignoreUnresolvedVariables = ignoreUnresolvedVariables,
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
    )

    public enum class Type {
        Request,
        Response,
    }
}

public class SetBuilder internal constructor() {
    public val headers: MutableMap<String, String> = mutableMapOf()
    public var statusCode: String? = null
    public var reasonPhrase: String? = null
    public var payload: String? = null
}

@ProxyDSL
public class ExtractVariablesBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "extractvariables"

    override var async: Boolean = false
    override var continueOnError: Boolean = false
    override var enabled: Boolean = true

    public lateinit var source: String
    public lateinit var variablePrefix: String

    private lateinit var jsonPayload: ExtractVariables.JsonPayload

    public fun jsonPayload(builder: JsonPayloadBuilder.() -> Unit) {
        jsonPayload = JsonPayloadBuilder().apply(builder).build()
    }

    internal fun build(): ExtractVariables = ExtractVariables(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        source = source,
        variablePrefix = variablePrefix,
        jsonPayload = jsonPayload,
    )

    @ProxyDSL
    public class JsonPayloadBuilder internal constructor() {
        private val variables = mutableMapOf<String, String>()

        internal fun build(): ExtractVariables.JsonPayload = ExtractVariables.JsonPayload(
            variables.map { (name, jsonPath) ->
                ExtractVariables.Variable(
                    name = name,
                    jsonPath = jsonPath,
                )
            },
        )

        public fun variable(name: String, @Language("jsonpath") jsonPath: String) {
            variables[name] = jsonPath
        }
    }
}

private fun Set<String>.toBuilder() = if (isEmpty()) {
    null
} else {
    AssignMessage.RemoveBuilder(
        headers = AssignMessage.Headers(
            map {
                AssignMessage.Header(it)
            },
        ),
    )
}

private fun SetBuilder.toBuilder() = if (
    headers.isEmpty() && statusCode == null && reasonPhrase == null && payload == null
) {
    null
} else {
    AssignMessage.SetBuilder(
        headers = AssignMessage.Headers(
            headers.map { (name, value) ->
                AssignMessage.Header(name, value)
            },
        ),
        statusCode = statusCode,
        reasonPhrase = reasonPhrase,
        payload = payload,
    )
}

@ProxyDSL
public class SpikeArrestBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "ratelimit"

    override var async: Boolean = false
    override var continueOnError: Boolean = false
    override var enabled: Boolean = true

    public var identifier: String? = null
    public var rate: String = ""
    public var useEffectiveCount: Boolean? = null
    public var messageWeight: String? = null

    internal fun build(): SpikeArrest = SpikeArrest(
        identifier = identifier?.let { SpikeArrest.Identifier(it) },
        rate = rate,
        useEffectiveCount = useEffectiveCount,
        enabled = enabled,
        continueOnError = continueOnError,
        messageWeight = messageWeight?.let { SpikeArrest.MessageWeight(it) },
    )
}

@ProxyDSL
public class RaiseFaultBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "raisefault"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    private val set = SetBuilder()

    public fun set(builder: SetBuilder.() -> Unit) {
        set.apply(builder)
    }

    public val remove: MutableSet<String> = mutableSetOf()

    public fun removeHeader(vararg headers: String) {
        remove.addAll(headers)
    }

    internal fun build() = RaiseFault(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        faultResponse = RaiseFault.FaultResponse(
            set = set.toBuilder(),
            remove = remove.toBuilder(),
        ),
    )
}

@ProxyDSL
public class KeyValueMapOperationsBuilder internal constructor(
    public override val name: String,
    public val mapIdentifier: String,
) : PolicyBuilder {
    override val prefix: String get() = error("Unknown")

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    private val gets = mutableListOf<KeyValueMapOperations.Get>()

    public fun get(name: String, assignTo: String, index: Int = 1) {
        gets.add(
            KeyValueMapOperations.Get(
                assignTo = assignTo,
                index = index,
                KeyValueMapOperations.Get.Key(name),
            ),
        )
    }

    internal fun build() = KeyValueMapOperations(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        gets = gets,
        mapIdentifier = mapIdentifier,
    )
}

@ProxyDSL
public class PopulateCacheBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "populatecache"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public var cacheKey: String? = null
    public var scope: CacheScope = Exclusive
    public var source: String? = null

    public var expiry: Duration? = null

    internal fun build() = PopulateCache(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        scope = scope,
        cacheKey = CacheKey(keyFragment = requireNotNull(cacheKey)),
        source = requireNotNull(source),
        expirySettings = PopulateCache.ExpirySettings(timeoutInSeconds = requireNotNull(expiry).inWholeSeconds),
    )
}

@ProxyDSL
public class LookupCacheBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "lookupcache"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public var cacheKey: String? = null
    public var scope: CacheScope = Exclusive
    private var assignTo: String? = null

    public fun assignTo(value: String) {
        this.assignTo = value
    }

    internal fun build() = LookupCache(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        scope = scope,
        cacheKey = CacheKey(keyFragment = requireNotNull(cacheKey)),
        assignTo = requireNotNull(assignTo),
    )

    public val cachehit: String get() = "${this@LookupCacheBuilder.prefix}.${this@LookupCacheBuilder.name}.cachehit"
}

@ProxyDSL
public class VerifyJWTBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "jwt"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public var algorithm: VerifyJWT.Algorithm = RS256
    public var issuer: RefValue? = null
    public var subject: RefValue? = null
    public var audience: RefValue? = null
    public lateinit var publicKeyJWKS: RefValue

    public val additionalClaims: MutableMap<String, String> = mutableMapOf()

    internal fun build() = VerifyJWT(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        algorithm = algorithm,
        issuer = issuer?.build(),
        subject = subject?.build(),
        audience = audience?.build(),
        publicKey = VerifyJWT.PublicKey(jwks = publicKeyJWKS.build()),
        additionalClaims = VerifyJWT.AdditionalClaims(
            additionalClaims.map {
                VerifyJWT.AdditionalClaims.Claim(it.key, it.value)
            },
        ),
    )

    public val valid: String get() = "${this@VerifyJWTBuilder.prefix}.${this@VerifyJWTBuilder.name}.valid"
}

public sealed interface RefValue {
    public data class Value(val value: String) : RefValue
    public data class Ref(val ref: String) : RefValue
}

internal fun RefValue.build(): RefOrValue = when (this) {
    is RefValue.Ref -> RefOrValue(ref = ref, value = null)
    is RefValue.Value -> RefOrValue(ref = null, value = value)
}

@ProxyDSL
public class DecodeJWTBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "decoded"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public lateinit var source: String

    internal fun build() = DecodeJWT(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        source = source,
    )
}

@ProxyDSL
public class ServiceCalloutBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "servicecallout"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public var response: String? = null
    public var timeout: Duration = 55.seconds
    public var url: String? = null
    private var sslInfo: ServiceCallout.HTTPTargetConnection.SSLInfo? = null

    public fun sslInfo(builder: SslInfoBuilder.() -> Unit) {
        sslInfo = SslInfoBuilder().apply(builder).build()
    }

    private var request: ServiceCallout.Request? = null

    public fun request(variable: String, builder: RequestBuilder.() -> Unit) {
        request = RequestBuilder(variable).apply(builder).build()
    }

    internal fun build() = ServiceCallout(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        response = response,
        timeout = timeout.inWholeMilliseconds,
        httpTargetConnection = ServiceCallout.HTTPTargetConnection(
            url = requireNotNull(url),
            sslInfo = sslInfo,
        ),
        request = request,
    )

    @ProxyDSL
    public class RequestBuilder internal constructor(private val variable: String) {
        public var clearPayload: Boolean = true

        private val set = SetBuilder()

        public fun set(builder: SetBuilder.() -> Unit) {
            set.apply(builder)
        }

        internal fun build(): ServiceCallout.Request = ServiceCallout.Request(
            variable = variable,
            clearPayload = clearPayload,
            set = set.toBuilder(),
        )
    }

    @ProxyDSL
    public class SslInfoBuilder internal constructor() {
        public var enabled: Boolean = true
        public var clientAuthEnabled: Boolean = false
        public lateinit var keyStore: String
        public var keyAlias: String? = null
        public var trustStore: String? = null

        internal fun build() = ServiceCallout.HTTPTargetConnection.SSLInfo(
            enabled = enabled,
            clientAuthEnabled = clientAuthEnabled,
            keyStore = keyStore,
            keyAlias = keyAlias,
            trustStore = trustStore,
        )
    }
}

@ProxyDSL
public class JavaScriptBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "javascript"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false

    public lateinit var resourceUrl: String
    public fun includeUrl(url: String) {
        includeUrls.add(url)
    }
    private val includeUrls: MutableList<String> = mutableListOf()
    public var timeLimit: Duration = 1.minutes

    internal fun build() = Javascript(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        timeLimit = timeLimit.inWholeMilliseconds,
        resourceUrl = resourceUrl,
        includeUrLs = includeUrls,
    )
}

@ProxyDSL
public class BasicAuthenticationBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "BasicAuthentication"

    override var enabled: Boolean = true
    override var continueOnError: Boolean = false
    override var async: Boolean = false
    public val ignoreUnresolvedVariables: Boolean = false

    public lateinit var user: RefValue.Ref
    public lateinit var password: RefValue.Ref
    public var operation: BasicAuthentication.Operation? = null

    private var createNew: Boolean = false
    private var assignTo: String? = null

    public fun assignTo(value: String, createNew: Boolean = false) {
        this.createNew = createNew
        this.assignTo = value
    }

    public var source: String? = null

    internal fun build() = BasicAuthentication(
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
        operation = requireNotNull(operation),
        ignoreUnresolvedVariables = ignoreUnresolvedVariables,
        user = user.build(),
        password = password.build(),
        assignTo = assignTo?.let { AssignTo(createNew = createNew, value = it) },
        source = source,
    )
}

@ProxyDSL
public sealed interface PolicyBuilder {
    public val name: String
    public val prefix: String
    public var async: Boolean
    public var continueOnError: Boolean
    public var enabled: Boolean
}

@ProxyDSL
public class QuotaBuilder internal constructor(public override val name: String) : PolicyBuilder {
    override val prefix: String = "ratelimit"

    override var async: Boolean = false
    override var continueOnError: Boolean = false
    override var enabled: Boolean = true

    /**
     * specifies the number of requests allowed for the API Proxy
     */
    public var allowCount: Int = 2

    public var interval: Int = 1
    public var distributed: Boolean = true
    public var startTime: Instant = Instant.fromEpochMilliseconds(0)
    public var synchronous: Boolean = true
    public var timeUnit: TimeUnit = Minute

    public enum class TimeUnit {
        Second,
        Minute,
        Hour,
        Day,
        Month,
    }

    internal fun build(): Quota = Quota(
        allow = Quota.Allow(count = allowCount),
        interval = Quota.Interval(interval),
        distributed = Quota.Distributed(distributed),
        startTime = startTime.toString().replace("T", " ").replace("Z", ""),
        synchronous = synchronous,
        timeUnit = when (timeUnit) {
            Second -> Quota.TimeUnit.Second
            Minute -> Quota.TimeUnit.Minute
            Hour -> Quota.TimeUnit.Hour
            Day -> Quota.TimeUnit.Day
            Month -> Quota.TimeUnit.Month
        },
        enabled = enabled,
        continueOnError = continueOnError,
        async = async,
    )
}
