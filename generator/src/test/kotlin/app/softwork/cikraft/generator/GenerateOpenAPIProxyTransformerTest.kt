package app.softwork.cikraft.generator

import app.softwork.cikraft.proxy.BasicAuthentication
import app.softwork.cikraft.proxy.RefValue
import app.softwork.cikraft.proxy.apiProxy
import kotlin.io.path.Path
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals

class GenerateOpenAPIProxyTransformerTest {
    @Test
    fun noApis() {
        val generated = generateOpenAPIProxyTransformer(
            emptyList(),
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin/openapitransformers/empty/APIProxyOpenAPITransformer.kt").readText()
                .drop(35),
            generated.toString(),
        )
    }

    @Test
    fun oidcApi() {
        val generated = generateOpenAPIProxyTransformer(
            listOf(
                Triple(
                    apiProxy(
                        "Foo",
                        "Foo",
                        "Foo",
                    ) {
                        val verifyMyIdp by policies.verifyJWT {
                            publicKeyJWKS = RefValue.Ref("jwks")
                            issuer = RefValue.Value("https://idp.example.com")
                        }
                        proxyEndPoint {
                            basePath = "/proxy"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/v1/",
                        "https://api-qs.example.com/v1/",
                    ),
                    false,
                ),
            ),
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin/openapitransformers/oidc/APIProxyOpenAPITransformer.kt").readText()
                .drop(34),
            generated.toString(),
        )
    }

    @Test
    fun oidcsApi() {
        val generated = generateOpenAPIProxyTransformer(
            listOf(
                Triple(
                    apiProxy(
                        "Foo",
                        "Foo",
                        "Foo",
                    ) {
                        val verifyMyIdp by policies.verifyJWT {
                            publicKeyJWKS = RefValue.Ref("jwks")
                            issuer = RefValue.Value("https://idp.example.com")
                        }
                        proxyEndPoint {
                            basePath = "/proxy"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/v1/",
                        "https://api-qs.example.com/v1/",
                    ),
                    false,
                ),
                Triple(
                    apiProxy(
                        "Foo",
                        "Foo",
                        "Foo",
                    ) {
                        val verifyMyIdp by policies.verifyJWT {
                            publicKeyJWKS = RefValue.Ref("jwks")
                            issuer = RefValue.Value("https://idp.example.com")
                        }
                        proxyEndPoint {
                            basePath = "/bar"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/v1/",
                        "https://api-qs.example.com/v1/",
                    ),
                    false,
                ),
                Triple(
                    apiProxy(
                        "Foo2",
                        "Foo2",
                        "Foo2",
                    ) {
                        val verifyMyIdp2 by policies.verifyJWT {
                            publicKeyJWKS = RefValue.Ref("jwks")
                            issuer = RefValue.Value("https://idp.example.com")
                        }
                        proxyEndPoint {
                            basePath = "/proxy2"
                            preFlow {
                                step(verifyMyIdp2)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/iflow2"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/v2/",
                        "https://api-qs.example.com/v2/",
                    ),
                    false,
                ),
                Triple(
                    apiProxy(
                        "m",
                        "m",
                        "m",
                    ) {
                        val checkBasicAuth by policies.basicAuth {
                            operation = BasicAuthentication.Operation.Decode
                            user = RefValue.Ref("private.user")
                            password = RefValue.Ref("private.password")
                        }
                        proxyEndPoint {
                            basePath = "/basic"
                            preFlow {
                                step(checkBasicAuth)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/v1/",
                        "https://api-qs.example.com/v1/",
                    ),
                    false,
                ),
            ),
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin/openapitransformers/oidcs/APIProxyOpenAPITransformer.kt").readText()
                .drop(35),
            generated.toString(),
        )
    }
}
