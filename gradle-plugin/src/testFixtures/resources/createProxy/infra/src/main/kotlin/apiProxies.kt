import app.softwork.cikraft.proxy.builder.ApiProxiesBuilder
import app.softwork.cikraft.proxy.BasicAuthentication
import app.softwork.cikraft.proxy.QuotaBuilder.TimeUnit.Minute
import app.softwork.cikraft.proxy.apiProxy
import app.softwork.cikraft.proxy.RefValue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class)
public fun ApiProxiesBuilder.apiProxies(baseUrl: String, suffix: String): Unit {
    @OptIn(ExperimentalUuidApi::class)
    val uuid = Uuid.random()

    @OptIn(ExperimentalUuidApi::class)
    apiProxy(
        name = "Test_API_FOO_URL_$uuid",
        title = "Test API FOO URL_$uuid",
        description = "Test API Created for $uuid",
        isVersioned = false,
    ) {
        val getCloudIDPParameters by policies.keyValueMapOperations("KVM_FOO") {
            get("CacheName", "idp.cache_name")
            get("JWKS_URI", "idp.jwks_uri")
            get("Issuer", "idp.issuer")
        }
        val readCachedKeys by policies.lookupCache {
            cacheKey = "{idp.cache_name}"
            assignTo(value = "JWTKeys.content")
        }
        val fetchJWTKeys by policies.serviceCallout {
            response = "JWTKeys"
            this.url = "https://{idp.jwks_uri}/"
            async = true
        }
        val cacheJWTKeys by policies.populateCache {
            cacheKey = "{idp.cache_name}"
            expiry = 1.hours
            source = "JWTKeys.content"
        }
        val verifyJWT by policies.verifyJWT {
            publicKeyJWKS = RefValue.Ref("JWTKeys.content")
            issuer = RefValue.Ref("idp.issuer")
        }

        val spike by policies.spikeArrest {
            continueOnError = true
            identifier = "client.ip"
            rate = "5pm"
            useEffectiveCount = true
        }
        val add429 by policies.raiseFault {
            set {
                statusCode = "429"
                reasonPhrase = "Too Many Requests"
                payload = ""
            }
        }
        val rateLimit by policies.quota {
            continueOnError = true
            allowCount = 2
            interval = 1
            distributed = true
            startTime = Instant.parse("2015-02-11T12:00:00Z")
            synchronous = true
            timeUnit = Minute
        }
        val add429WithRetryAfter by policies.raiseFault {
            set {
                headers["Retry-After"] = "60"
                statusCode = "429"
                reasonPhrase = "Too Many Requests"
                payload = ""
            }
        }
        val getCredentials by policies.keyValueMapOperations("FOO_IFlow") {
            get("sbxHttpClientId", "private.user")
            get("sbxHttpClientSecret", "private.password")
        }
        val addCredentialsAsBasic by policies.basicAuth {
            operation = BasicAuthentication.Operation.Encode
            user = RefValue.Ref("private.user")
            password = RefValue.Ref("private.password")
            assignTo("request.header.Authorization", createNew = true)
        }

        proxyEndPoint {
            this.basePath = basePath
            preFlow {
                step(getCloudIDPParameters)
                step(readCachedKeys)
                step(fetchJWTKeys, readCachedKeys.cachehit eq false)
                step(cacheJWTKeys, readCachedKeys.cachehit eq false)
                step(verifyJWT)

                step(spike)
                step(add429, spike.failed eq true)
                step(rateLimit.name)
                step(add429WithRetryAfter.name, rateLimit.failed eq true)
            }
        }
        targetEndPoint {
            this.url = ""
            preFlow {
                step(getCredentials)
                step(addCredentialsAsBasic)
            }
            loadBalancerConfigurations {
                isRetry = false
                healthMonitorIsEnabled = false
            }
        }
    }
}
