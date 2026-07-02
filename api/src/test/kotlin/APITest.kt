import app.softwork.cikraft.api.BuildAndDeployStatus
import app.softwork.cikraft.api.BuildAndDeployStatus.Status.Success
import app.softwork.cikraft.api.IntegrationFlow
import app.softwork.cikraft.api.IntegrationPackage
import app.softwork.cikraft.api.createIntegrationFlow
import app.softwork.cikraft.api.createIntegrationPackage
import app.softwork.cikraft.api.deployIntegrationFlow
import app.softwork.cikraft.api.getBuildAndDeployStatus
import app.softwork.cikraft.api.getIntegrationPackage
import app.softwork.cikraft.api.sapciSerialization
import io.ktor.client.plugins.*
import io.ktor.client.plugins.logging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class APITest {
    @Test
    fun deployIFlow() = testApplication {
        application {
            routing {
                route("api/v1") {
                    install(ContentNegotiation) {
                        sapciSerialization()
                    }

                    getCSRFToken {
                        "FOO"
                    }

                    getIntegrationPackage { id ->
                        when (id) {
                            "IPFOO" -> IntegrationPackage(
                                id = "IPFOO",
                                name = "IP_FOO",
                                description = "Some description",
                                shortText = "Some description",
                                version = "1.0.0",
                            )

                            else -> null
                        }
                    }

                    createIntegrationPackage { new ->
                        IntegrationPackage(
                            id = new.id,
                            name = new.name,
                            shortText = new.shortText,
                            version = new.version,
                            description = new.description,
                        )
                    }

                    createIntegrationFlow {
                        IntegrationFlow(
                            id = it.id,
                            version = "1.0.0",
                            packageId = it.packageId,
                            name = it.name,
                            description = "Some description",
                        )
                    }

                    deployIntegrationFlow {
                        "foo"
                    }
                    getBuildAndDeployStatus { taskId ->
                        assertEquals("foo", taskId)
                        BuildAndDeployStatus(
                            taskId = "foo",
                            status = Success,
                        )
                    }
                }
            }
        }

        val client = createClient {
            defaultRequest {
                url("/api/v1/")
            }
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                sapciSerialization()
            }
            install(Logging) {
                level = LogLevel.ALL
                logger = Logger.SIMPLE
            }
        }

        client.createIntegrationPackage(
            IntegrationPackage.New(
                name = "IP_FOO",
                id = "IPFOO",
                description = "Some description",
                shortText = "Some description",
            ),
        )

        val integrationPackage = client.getIntegrationPackage("IPFOO")
        assertNotNull(integrationPackage)

        client.createIntegrationFlow(
            IntegrationFlow.New(
                name = "IF_FOO",
                id = "IFFOO",
                packageId = "IPFOO",
                artifactContentAsBase64 = "",
            ),
        )
        val taskId = client.deployIntegrationFlow("IFFOO")
        assertNotNull(taskId)
        client.getBuildAndDeployStatus(taskId)
    }
}
