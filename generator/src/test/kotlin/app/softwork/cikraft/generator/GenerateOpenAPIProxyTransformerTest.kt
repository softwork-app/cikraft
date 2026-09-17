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
            "/foo",
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
                            basePath = "/http/foo/proxy"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/http/foo/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/http",
                        "https://api-qs.example.com/http",
                    ),
                    false,
                ),
            ),
            "/foo",
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
                            basePath = "/http/foo/proxy"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/http/foo/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/http",
                        "https://api-qs.example.com/http",
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
                            basePath = "/http/foo/bar"
                            preFlow {
                                step(verifyMyIdp)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/http/foo/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/http",
                        "https://api-qs.example.com/http",
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
                            basePath = "/http/foo/proxy2"
                            preFlow {
                                step(verifyMyIdp2)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/http/foo/iflow2"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.bar.com/http",
                        "https://api-qs.bar.com/http",
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
                            basePath = "/http/foo/basic"
                            preFlow {
                                step(checkBasicAuth)
                            }
                        }
                        targetEndPoint {
                            relativePath = "/http/foo/iflow"
                            providerId = "Provider"
                            loadBalancerConfigurations {
                            }
                        }
                    },
                    setOf(
                        "https://api.example.com/http",
                        "https://api-qs.example.com/http",
                    ),
                    false,
                ),
            ),
            "/foo",
        )

        assertEquals(
            (Path("src/testFixtures") / "kotlin/openapitransformers/oidcs/APIProxyOpenAPITransformer.kt").readText()
                .drop(35),
            generated.toString(),
        )
    }
}
