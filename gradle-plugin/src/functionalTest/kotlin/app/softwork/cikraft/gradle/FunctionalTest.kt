package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.setupRuntimeAuth
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.div
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
@ExperimentalPathApi
class FunctionalTest {
    private val fixtureDir = Path(System.getenv("fixtureDir"))

    val consumerClient = HttpClient(CIO) {
        setupRuntimeAuth(
            tokenUrl = "https://b3d0decftrial.authentication.us10.hana.ondemand.com/oauth/token",
            clientId = "sb-07e739da-e974-4299-a3e6-86777fa2309a!b657590|it-rt-b3d0decftrial!b26655",
            clientSecret = System.getenv("SBX_RT_CLIENT_SECRET")
        )
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }
        install(ContentNegotiation) {
            jsonIo(Json)
        }
        defaultRequest {
            url("https://b3d0decftrial.it-cpitrial05-rt.cfapps.us10-001.hana.ondemand.com")
        }
        install(HttpCookies)
    }

    @Test
    fun createOpenApi() {
        val id = Uuid.random()
        val projectDir = fixtureDir / "resources" / "deployToSandbox"
        val gradlePropertiesFile = projectDir / "gradle.properties"
        gradlePropertiesFile.writeText(
            """|version=1.0.0
               |cikraftSbxUsername=sb-d765e19b-8e9a-4bd0-af14-d5149b54d539!b657590|it!b26655
               |cikraftSbxPassword=${System.getenv("SBX_API_CLIENT_SECRET")}
               |suffix=/$id
               |
            """.trimMargin(),
        )
        try {
            val result = build(
                projectDir,
                "clean",
                ":infra:deploySbxInfrastructure",
                "--stacktrace",
            )

            assertEquals(TaskOutcome.SUCCESS, result.task(":infra:deploySbxInfrastructure")?.outcome)

            runBlocking {
                val csrfToken = consumerClient.head("/http/foo/$id/auto-test") {
                    header("X-CSRF-Token", "FETCH")
                    expectSuccess = true
                }.headers["X-CSRF-Token"]!!
                val iFlowResponse = consumerClient.post("/http/foo/$id/auto-test") {
                    header("X-CSRF-Token", csrfToken)
                    header("B", "some Header")
                    setBody(
                        // language=json
                        """{ "x": 42 }""",
                    )
                    contentType(ContentType.Application.Json)
                }
                assertEquals(500, iFlowResponse.status.value)
                assertTrue(
                    "An internal server error occured: An operation is not implemented: Not yet implemented." in iFlowResponse.bodyAsText(),
                    iFlowResponse.bodyAsText(),
                )
                val exceptionResponse = consumerClient.post("/http/foo/$id/auto-test-exception") {
                    header("X-CSRF-Token", csrfToken)
                    header("B", "some Header")
                    setBody(
                        // language=json
                        """{ "x": 42 }""",
                    )
                    contentType(ContentType.Application.Json)
                }
                assertEquals(444, exceptionResponse.status.value)
                assertEquals("adsfasdf", exceptionResponse.bodyAsText())
            }
        } finally {
            val result = build(
                projectDir,
                ":infra:undeploySbxInfrastructure",
                "--stacktrace",
            )

            assertEquals(TaskOutcome.SUCCESS, result.task(":infra:undeploySbxInfrastructure")?.outcome)

            gradlePropertiesFile.deleteIfExists()
        }
    }

    private fun build(projectDir: Path, vararg tasks: String): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .forwardOutput()
        .withArguments(
            *tasks,
            "--configuration-cache",
            "-Porg.gradle.kotlin.dsl.dcl=true",
        )
        .build()
}
