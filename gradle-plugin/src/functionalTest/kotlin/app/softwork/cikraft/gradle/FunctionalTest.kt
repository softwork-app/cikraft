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
import kotlin.uuid.Uuid

@ExperimentalPathApi
class FunctionalTest {
    private val fixtureDir = Path(System.getenv("fixtureDir"))

    val consumerClient = HttpClient(CIO) {
        setupRuntimeAuth(
            tokenUrl = "${System.getenv("TRIAL_AUTH_SERVER")}/oauth/token",
            clientId = System.getenv("TRIAL_RT_CLIENT_ID"),
            clientSecret = System.getenv("TRIAL_RT_CLIENT_SECRET")
        )
        install(Logging) {
            level = LogLevel.ALL
            logger = Logger.SIMPLE
        }
        install(ContentNegotiation) {
            jsonIo(Json)
        }
        defaultRequest {
            url(System.getenv("TRIAL_HTTP_SERVER"))
        }
        install(HttpCookies)
    }

    @Test
    fun deployToSandbox() {
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
            "-PGitHubPackagesUsername=${System.getenv("KDGP_USERNAME")}",
            "-PGitHubPackagesPassword=${System.getenv("KDGP_PASSWORD")}",
            "-Pversion=1.0.0",
            "-PcikraftSbxUsername=${System.getenv("TRIAL_API_CLIENT_ID")}",
            "-PcikraftSbxPassword=${System.getenv("TRIAL_API_CLIENT_SECRET")}",
            "-PtrialWeb=${System.getenv("TRIAL_WEB")}",
            "-PtrialApiServer=${System.getenv("TRIAL_API_SERVER")}",
            "-PtrialAuthServer=${System.getenv("TRIAL_AUTH_SERVER")}",
            "-PtrialHttpServer=${System.getenv("TRIAL_HTTP_SERVER")}",
            "-Psuffix=/$id",
            "-Dorg.gradle.jvmargs=-Xmx4096m",
        )
        .build()
}
