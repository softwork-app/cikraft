package app.softwork.cikraft.proxy

import app.softwork.cikraft.proxy.AssignMessageBuilder.Type.Response
import app.softwork.cikraft.proxy.BasicAuthentication.Operation.*
import app.softwork.cikraft.proxy.QuotaBuilder.TimeUnit.*
import app.softwork.cikraft.proxy.VerifyJWT.Algorithm.*
import kotlin.time.*
import kotlin.time.Duration.Companion.hours

val transport = apiProxy(
    name = "Test_API_FOO_URL2",
    title = "Test API FOO URL2",
    description = null,
    isVersioned = false,
) {
    val MMM by policies.assignMessage {
        set {
            headers["Foo"] = "Bar"
        }
        ignoreUnresolvedVariables = false
        assignTo(createNew = false, type = Response)
    }
    val RateLimit by policies.quota {
        allowCount = 2
        interval = 1
        distributed = true
        startTime = Instant.parse("2015-02-11T12:00:00Z")
        synchronous = true
        timeUnit = Minute
    }
    val Spike by policies.spikeArrest {
        identifier = "client.ip"
        rate = "5pm"
        useEffectiveCount = true
    }
    val Add429 by policies.raiseFault {
        set {
            headers["Retry-After"] = "60"
            statusCode = "429"
            reasonPhrase = "Too Many Requests"
            payload = ""
        }
    }
    val GetCredentials by policies.keyValueMapOperations("FOO_IFlow") {
        get("sbxHttpClientId", assignTo = "private.user")
        get("sbxHttpClientSecret", assignTo = "private.pass")
    }

    val AddCredentials by policies.basicAuth {
        operation = Encode
        user = RefValue.Ref("private.user")
        password = RefValue.Ref("private.pass")
        assignTo(value = "request.header.Authorization")
    }

    val readCachedKeys by policies.lookupCache {
        cacheKey = "{private.idp.cache_name}"
        assignTo(value = "JWTKeys.content")
    }
    val readJWTKeys by policies.serviceCallout {
        response = "JWTKeys"
        url = "https://{private.idp.jwks_uri}/"
        async = true
    }
    val cacheJWTKeys by policies.populateCache {
        cacheKey = "{private.idp.cache_name}"
        expiry = 1.hours
        source = "JWTKeys.content"
    }
    val verifyJWT by policies.verifyJWT {
        algorithm = RS256
        publicKeyJWKS = RefValue.Ref("JWTKeys.content")
        issuer = RefValue.Ref("private.idp.issuer")
        subject = RefValue.Ref("private.idp.subject")
        audience = RefValue.Ref("private.idp.audience")
        additionalClaims["acr"] = "2"
    }

    proxyEndPoint {
        basePath = "/foo/shop2"
        preFlow {
            step(readCachedKeys)
            step(readJWTKeys, readCachedKeys.cachehit eq false)
            step(cacheJWTKeys, readCachedKeys.cachehit eq false)
            step(verifyJWT)

            step(Spike)
            step(RateLimit.name)
            step(Add429, (verifyJWT.valid eq false) or (RateLimit.failed eq true))
        }
        postFlow {
            step(MMM)
        }
    }
    targetEndPoint {
        url = "https://api.predic8.de/shop/v2/products"

        property("request.streaming.enabled", "true")
        property("response.streaming.enabled", "true")

        preFlow {
            step(GetCredentials)
            step(AddCredentials)
        }

        loadBalancerConfigurations {
            isRetry = false
            healthMonitorIsEnabled = false
        }
    }
}
