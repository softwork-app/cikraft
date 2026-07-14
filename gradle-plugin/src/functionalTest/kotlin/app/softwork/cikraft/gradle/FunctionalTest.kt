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
import kotlin.io.path.div
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
            tokenUrl = "https://8c5e4266trial.authentication.ap21.hana.ondemand.com/oauth/token",
            clientId = "sb-cd8c42c8-1525-4225-8ce2-5fcd70fac8fd!b131200|it-rt-8c5e4266trial!b196",
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
            url("https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com")
        }
        install(HttpCookies)
    }

    @Test
    fun createOpenApi() {
        val id = Uuid.random()
        val projectDir = fixtureDir / "resources" / "deployToSandbox"

        try {
            val result = build(
                projectDir,
                "clean",
                "deploySbxInfrastructure",
                "--stacktrace",
                id = id,
            )

            assertEquals(TaskOutcome.SUCCESS, result.task(":app:deploySbxInfrastructure")?.outcome)

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
                "undeploySbxInfrastructure",
                "--stacktrace",
                id = id,
            )

            assertEquals(TaskOutcome.SUCCESS, result.task(":app:undeploySbxInfrastructure")?.outcome)
        }
    }

    private fun build(
        projectDir: Path,
        vararg tasks: String,
        id: Uuid,
    ): BuildResult = GradleRunner.create()
        .withProjectDir(projectDir.toFile())
        .forwardOutput()
        .withArguments(
            *tasks,
            "--configuration-cache",
            "-Porg.gradle.kotlin.dsl.dcl=true",
            "--info",
            "-PKDGPUsername=${System.getenv("KDGP_USERNAME")}",
            "-PKDGPPassword=${System.getenv("KDGP_PASSWORD")}",
            "-Pversion=1.0.0",
            "-PcikraftSbxUsername=sb-08b6afaa-f349-4ad3-ba76-28dbcfdd62e3!b131200|it!b196",
            "-PcikraftSbxPassword=${System.getenv("SBX_API_CLIENT_SECRET")}",
            "-Psuffix=/$id",
            "-Dorg.gradle.jvmargs=-Xmx4096m",
        )
        .build()
}
