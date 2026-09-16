package app.softwork.cikraft.generator

import app.softwork.cikraft.proxy.ApiProxyTransport
import app.softwork.cikraft.proxy.BasicAuthentication
import app.softwork.cikraft.proxy.VerifyJWT
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.MemberName.Companion.member
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.joinToCode

public fun generateOpenAPIProxyTransformer(
    proxies: List<Triple<ApiProxyTransport, String, Boolean>>,
): FileSpec = FileSpec.builder("", "APIProxyOpenAPITransformer").apply {

    val transformerClass = TypeSpec.classBuilder("APIProxyOpenAPITransformer").apply {
        addSuperinterface(ClassName("app.softwork.cikraft.core", "SAPOpenAPITransformer"))

        addFunction(
            FunSpec.builder("convert").apply {
                addModifiers(KModifier.OVERRIDE)
                addParameter(
                    ParameterSpec.builder("openApi", ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi")).build()
                )
                addParameter(
                    ParameterSpec.builder(
                        "infrastructure",
                        ClassName("app.softwork.cikraft.core", "OpenApiInfrastructure")
                    ).build()
                )
                returns(ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi"))

                addStatement("val paths = openApi.paths")
                beginControlFlow(
                    "val apiPaths = %M<%T, %T>",
                    MemberName("kotlin.collections", "buildMap", isExtension = true),
                    STRING,
                    ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "Path")
                )

                val allSecurity = proxies.mapNotNullTo(mutableSetOf()) { (proxy, _, usesMutualTLS) ->
                    proxy.getAuth(usesMutualTLS) {}
                }

                val allApiHosts = proxies.mapTo(mutableSetOf()) { it.second }

                for ((proxy, apiHost, usesMutualTLS) in proxies) {
                    val apiPath = proxy.proxyEndpoints.single().basePath
                    val iFlowPath = proxy.targetEndPoint.single().relativePath ?: continue

                    var scopes = setOf<String>()

                    val auth = proxy.getAuth(usesMutualTLS) {
                        scopes = it
                    }

                    addStatement(
                        "this[%S] = paths[%S]!!.let { it.copy(post = it.post!!.copy(security = %L, servers = %L)) }",
                        apiPath,
                        iFlowPath,
                        if (auth == null) {
                            CodeBlock.of(
                                "%M(%M())",
                                MemberName("kotlin.collections", "listOf", isExtension = true),
                                MemberName("kotlin.collections", "emptyMap", isExtension = true),
                            )
                        } else {
                            CodeBlock.of(
                                "%M(%M(%S to %L))",
                                MemberName("kotlin.collections", "listOf", isExtension = true),
                                MemberName("kotlin.collections", "mapOf", isExtension = true),
                                auth.name,
                                if (scopes.isEmpty()) CodeBlock.of("%M()", emptyList) else {
                                    CodeBlock.of(
                                        "%M(%L)",
                                        MemberName("kotlin.collections", "listOf", isExtension = true),

                                        scopes.map {
                                            CodeBlock.of("%S", it)
                                        }.joinToCode()
                                    )
                                }
                            )
                        },
                        if (allApiHosts.size == 1) CodeBlock.of("%M()", emptyList) else CodeBlock.of(
                            "%M(%T(url = %S))",
                            MemberName("kotlin.collections", "listOf", isExtension = true),
                            ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "Server"),
                            apiHost,
                        )
                    )
                }
                endControlFlow()

                addStatement(
                    "return openApi.copy(servers = %L, paths = apiPaths, components = openApi.components.copy(securitySchemes = %L))",
                    if (allApiHosts.size == 1) {
                        CodeBlock.of(
                            "%M(%T(url = %S))",
                            MemberName("kotlin.collections", "listOf", isExtension = true),
                            ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "Server"),
                            allApiHosts.single(),
                        )
                    } else CodeBlock.of("%M()", emptyList),
                    if (allSecurity.isEmpty()) {
                        CodeBlock.of("%M()", MemberName("kotlin.collections", "emptyMap", isExtension = true))
                    } else {
                        CodeBlock.of(
                            "%M(%L)",
                            MemberName("kotlin.collections", "mapOf", isExtension = true),
                            allSecurity.map {
                                CodeBlock.of(
                                    "%S to %L",
                                    it.name,
                                    when (it) {
                                        HttpBasic -> CodeBlock.of("%T(scheme = %M)",
                                            ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "SecurityScheme", "Http"),
                                            ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "SecurityScheme", "Http", "Scheme").member("Basic"),
                                        )
                                        MutualTLS -> CodeBlock.of("%T()", ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "SecurityScheme", "MutualTLS"))
                                        is OIDC -> CodeBlock.of(
                                            "%T(openIdConnectUrl = %S)",
                                            ClassName("io.github.hfhbd.kfx.openapi.model", "OpenApi", "SecurityScheme", "OpenIdConnect"),
                                            it.wellKnownUrl,
                                        )
                                    }
                                )
                            }.joinToCode()
                        )
                    }
                )
            }.build()
        )


    }.build()
    addType(transformerClass)
}.build()

private sealed interface Auth {
    val name: String
}

private data object HttpBasic : Auth {
    override val name = "basic"
}

private data class OIDC(override val name: String, val wellKnownUrl: String) : Auth
private data object MutualTLS : Auth {
    override val name = "mutualTLS"
}

private fun ApiProxyTransport.getAuth(
    usesMutualTLS: Boolean,
    scopes: (Set<String>) -> Unit,
): Auth? {
    return if (usesMutualTLS) {
        MutualTLS
    } else {
        policies.firstNotNullOfOrNull { (policyName, policy) ->
            when (policy) {
                is BasicAuthentication if policy.operation == BasicAuthentication.Operation.Decode -> HttpBasic

                is VerifyJWT if policy.issuer != null && policy.issuer!!.value != null -> {
                    require(policyName.startsWith("verify")) {
                        "VerifyJWT policy $policyName must start with 'verify'. The suffix will be used as global security id."
                    }
                    val name = policyName.removePrefix("verify")

                    val scopes = policy.additionalClaims.claims.singleOrNull { it.name == "scope" }?.value
                    scopes?.split(" ")?.toSet()?.let { scopes(it) }

                    OIDC(
                        name = name,
                        wellKnownUrl = policy.issuer!!.value!! + "/.well-known/openid-configuration",
                    )
                }

                else -> null
            }
        }
    }
}

private val emptyList = MemberName("kotlin.collections", "emptyList", isExtension = true)
