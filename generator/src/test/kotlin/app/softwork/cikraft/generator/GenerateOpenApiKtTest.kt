package app.softwork.cikraft.generator

import app.softwork.cikraft.core.OpenApiInfrastructure
import app.softwork.cikraft.core.Script.Body.ContentNegotiation
import createdSealedFlow
import createdTypedEnumFlow
import createdTypedFlow
import createdTypedListFlow
import createdTypedMapFlow
import fooFlow
import io.github.hfhbd.kfx.codegen.CodeGenTree.NormalClass
import kotlinx.serialization.json.*
import kotlinxIoFlow
import twoFlow
import kotlin.test.*

class GenerateOpenApiKtTest {
    private val json = Json {
        prettyPrint = true
    }

    @Test
    fun validContentTypeParameters() {
        assertEquals(
            "text/csv; charset=utf-8;headers=absent",
            ContentNegotiation(
                factoryKlass = NormalClass("com.example", listOf("CsvFactory")),
                contentType = "text/csv",
                parameters = mapOf("charset" to "utf-8", "headers" to "absent"),
            ).contentType(),
        )
    }

    @Test
    fun simple() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/a.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            fooFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "1.0.0",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun kotlinxIO() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/kotlinxIO.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            kotlinxIoFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "1.0.0",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun typed() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/typed.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            createdTypedFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "unspecified",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun list() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/lists.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            createdTypedListFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "unspecified",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun map() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/map.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            createdTypedMapFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "unspecified",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun sealed() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/sealed.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            createdSealedFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "unspecified",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun two() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/two.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            twoFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "1.0.0",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                            "https://localhost2/foo" to "Some Description",
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }

    @Test
    fun enum() {
        assertEquals(
            GenerateOpenApiKtTest::class.java.getResourceAsStream("/enum.json")!!.bufferedReader().readText(),
            json.encodeToString(
                generateOpenApi(
                    OpenApiInfrastructure(
                        apis = listOf(
                            createdTypedEnumFlow,
                        ),
                        name = "New IP",
                        description = "IP Description",
                        version = "unspecified",
                        servers = mapOf(
                            "https://localhost/foo" to null,
                        ),
                        tags = mapOf("Com_Example_Ktor_Resources" to "A Description"),
                        packages = emptySet(),
                    ),
                ),
            ) + "\n",
        )
    }
}
