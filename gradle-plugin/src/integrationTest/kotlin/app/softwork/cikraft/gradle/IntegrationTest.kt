package app.softwork.cikraft.gradle

import com.example.*
import app.softwork.cikraft.api.BuildAndDeployStatus
import app.softwork.cikraft.api.BuildAndDeployStatus.Status.Success
import app.softwork.cikraft.api.IntegrationFlow
import app.softwork.cikraft.api.IntegrationPackage
import app.softwork.cikraft.api.sapciSerialization
import app.softwork.cikraft.core.*
import app.softwork.cikraft.core.Script.*
import createIntegrationFlow
import createIntegrationPackage
import deployIntegrationFlow
import fooScript
import fooSuspendScript
import fooWildcardScript
import getBuildAndDeployStatus
import getCSRFToken
import getIntegrationPackage
import io.github.hfhbd.kfx.codegen.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.*
import org.gradle.testkit.runner.*
import java.nio.file.*
import kotlin.io.path.*
import kotlin.test.*

@ExperimentalPathApi
class IntegrationTest {
    private val fixtureDir = Path(System.getenv("fixtureDir"))

    @Test
    fun applyPluginWithoutSourcesDoesNotCreateJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithoutSourcesDoesNotCreateJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertFalse((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").exists())
    }

    @Test
    fun applyPluginWithSourcesCreatesJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithSourcesCreatesJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertEquals(
            expected = listOf(fooScript),
            actual = Json.decodeFromString((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    @Test
    fun applyPluginWithSuspendSourcesCreatesJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithSuspendSourcesCreatesJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertEquals(
            listOf(fooSuspendScript),
            Json.decodeFromString((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    @Test
    fun applyPluginWithWildcardSourcesCreatesJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithWildcardSourcesCreatesJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertEquals(
            listOf(fooWildcardScript),
            Json.decodeFromString((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    @Test
    fun applyPluginWithSerializedSourcesCreatesJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithSerializedSourcesCreatesJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertEquals(
            listOf(serializedScript),
            Json.decodeFromString((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    @Test
    fun applyPluginWithTypedSourcesCreatesJsonFile() {
        val projectDir = fixtureDir / "resources" / "applyPluginWithTypedSourcesCreatesJsonFile"
        createRunner(projectDir, ":assemble").build()

        assertEquals(
            listOf(typedScript),
            Json.decodeFromString((projectDir / "build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    @Test
    fun applyInfrastructurePluginWithoutDependencyDoesNotGenerateAccessors() {
        val projectDir = fixtureDir / "resources" / "applyInfrastructurePluginWithoutDependency"
        val s = createRunner(projectDir, "assemble").build()
        assertEquals(null, s.task(":infra:r8JarIF_Bar"))

        assertEquals(null, (projectDir / "infra/build/cikraft/IF_Bar/libs").toFile().list())

        assertFalse(
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Bar.kt").exists(),
            "without a configuration of the IFlow there are no accessors",
        )
    }

    @Test
    fun applyInfrastructurePluginWithAppDependency() = runBlocking {
        val server = embeddedServer(CIO, port = 0) {
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
        try {
            server.start(wait = false)
            val port = server.engine.resolvedConnectors().single().port

            val projectDir = fixtureDir / "resources" / "applyInfrastructurePluginWithAppDependency"
            val assembleResult = createRunner(projectDir, "assemble", "generateOpenApi").build()
            assertEquals(null, assembleResult.task(":app:r8JarIF_Baz")?.outcome)
            assertEquals(null, assembleResult.task(":app:generateIF_BazKotlinEntrypoints")?.outcome)

            assertEquals(
                (fixtureDir / "kotlin/deps.kt").readText().drop(63),
                (projectDir / "app/build/cikraft/typedFlows/kotlin/IF_Baz.kt").readText()
                    .replaceProjectDir(projectDir),
            )

            val result = createRunner(
                projectDir,
                "deployDevInfrastructure",
                "-PcikraftDevUsername=foo",
                "-PcikraftDevPassword=bar",
                "--stacktrace",
                "-PsapCIPort=$port",
            ).buildAndFail()

            assertEquals(TaskOutcome.SUCCESS, result.task(":app:r8JarIF_Baz")?.outcome)
            assertEquals(TaskOutcome.SUCCESS, result.task(":app:generateIF_BazKotlinEntrypoints")?.outcome)

            val r8Jar = projectDir / "app/build/cikraft/IF_Baz/libs/r8.jar"
            assertTrue(r8Jar.exists())
            assertTrue(
                r8Jar.fileSize() in 100_000L..200_000L,
                "The size of an empty R8Jar is about 15KB, and 100-200 KB is a normal size with Kotlin + Json",
            )
        } finally {
            server.stop()
        }
    }

    private fun String.replaceProjectDir(projectDir: Path): String {
        return replace(projectDir.toRealPath().toString() + "/", "")
    }

    @Test
    fun applyInfrastructurePluginWithAppDependencyAndPr() {
        val projectDir = fixtureDir / "resources" / "applyInfrastructurePluginWithAppDependencyAndPr"
        val result = createRunner(projectDir, ":infra:deployDevInfrastructure",
            "-PcikraftDevUsername=foo",
            "-PcikraftDevPassword=bar",
            "--offline",
            "-Psuffix=/pr/42",
        ).build()

        assertEquals(
            (fixtureDir / "kotlin/pr.kt").readText().drop(63),
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Ba.kt").readText()
                .replaceProjectDir(projectDir),
        )
    }

    @Test
    fun createInfrastructurePluginWithAppDependencyAndPr() {
        val projectDir = fixtureDir / "resources" / "createInfrastructurePluginWithAppDependencyAndPr"

        createRunner(
            projectDir,
            ":infra:createInfrastructureDryRun",
            "-Psuffix=/pr/42",
            "-PsapCIDevUsername=foo",
            "-PsapCIDevPassword=bar",
        ).build()

        assertEquals(
            (fixtureDir / "kotlin/pr.kt").readText().drop(63),
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Ba.kt").readText()
                .replaceProjectDir(projectDir),
        )
    }

    @Test
    fun multiple() {
        val projectDir = fixtureDir / "resources" / "multiple"

        createRunner(
            projectDir,
            ":infra:createInfrastructureDryRun",
            "-Psuffix=/pr/42",
            "-PsapCIDevUsername=foo",
            "-PsapCIDevPassword=bar",
        ).build()

        assertEquals(
            (fixtureDir / "kotlin/multiple.kt").readText().drop(63),
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Multiple.kt").readText()
                .replaceProjectDir(projectDir),
        )
    }

    @Test
    fun createOpenApi() {
        val projectDir = fixtureDir / "resources" / "createOpenApi"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/typed.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createOpenApiWithMap() {
        val projectDir = fixtureDir / "resources" / "createOpenApiWithMap"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/map.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createProxy() {
        val projectDir = fixtureDir / "resources" / "createProxy"

        val result = createRunner(projectDir, ":infra:compileApiProxiesKotlin", "--stacktrace").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":infra:compileApiProxiesKotlin")?.outcome)
    }

    @Test
    fun createOpenApiList() {
        val projectDir = fixtureDir / "resources" / "createOpenApiList"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/lists.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createOpenApiSealedList() {
        val projectDir = fixtureDir / "resources" / "createOpenApiSealedList"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/sealed.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createOpenApiWithSerialName() {
        val projectDir = fixtureDir / "resources" / "createOpenApiWithSerialName"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/typed.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createOpenApiWithEnum() {
        val projectDir = fixtureDir / "resources" / "createOpenApiWithEnum"

        createRunner(projectDir, ":consumer:myCustomTask", "--stacktrace").build()

        assertEquals(
            IntegrationTest::class.java.getResourceAsStream("/enum.json")!!.bufferedReader().readText(),
            (projectDir / "infra/build/cikraft/openapi.json").readText(),
        )
    }

    @Test
    fun createInfrastructureGeneratesKtorClientApi() {
        val projectDir = fixtureDir / "resources" / "createInfrastructureGeneratesKtorClientApi"

        createRunner(
            projectDir,
            ":client:generateKtorResources",
            "-Psuffix=/pr/42",
            "--stacktrace",
        ).build()

        assertEquals(
            (fixtureDir / "kotlin/pr.kt").readText().drop(63),
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Ba.kt").readText()
                .replaceProjectDir(projectDir),
        )

        assertEquals(
            """package ip.foo

import io.ktor.resources.Resource

/**
 * Ba test
 */
@Resource(path = "foo/bar/baz")
public data object IFBa
""",
            (projectDir / "client/build/cikraft/ktor/api/resources/ip/foo/IFBa.kt").readText(),
        )
    }

    @Ignore("app.softwork.cikraft.core.Fault@jsonError.hasBackingField is wrongly true")
    @Test
    fun createInfrastructureGeneratesMultiplatformKtorServerApi() {
        val projectDir = fixtureDir / "resources" / "createInfrastructureGeneratesMultiplatformKtorServerApi"

        createRunner(
            projectDir,
            "assemble",
            "-Psuffix=/pr/42",
            "--stacktrace",
        ).build()
    }

    @Test
    fun createInfrastructureGeneratesKtorServerApi() {
        val projectDir = fixtureDir / "resources" / "createInfrastructureGeneratesKtorServerApi"

        createRunner(
            projectDir,
            "assemble",
            "-Psuffix=/pr/42",
            "--stacktrace",
        ).build()

        assertEquals(
            (fixtureDir / "kotlin/pr.kt").readText().drop(63),
            (projectDir / "infra/build/cikraft/typedFlows/kotlin/IF_Ba.kt").readText()
                .replaceProjectDir(projectDir),
        )

        assertEquals(
            """package ip.foo

import io.ktor.resources.Resource

/**
 * Ba test
 */
@Resource(path = "foo/bar/baz")
public data object IFBa
""",
            (projectDir / "server/build/cikraft/ktor/api/resources/ip/foo/IFBa.kt").readText(),
        )

        assertEquals(
            (fixtureDir / "kotlin/ip/foo/server.kt").readText(),
            (projectDir / "server/build/cikraft/api/ktor/server/ip/foo/IFBa.kt").readText(),
        )
    }

    @Test
    fun useKotlinToolchain() {
        val projectDir = fixtureDir / "resources" / "useKotlinToolchain"

        createRunner(
            projectDir,
            "createInfrastructureDryRun",
            "--stacktrace",
        ).build()
    }

    @Test
    fun rawMessage() {
        val projectDir = fixtureDir / "resources" / "rawMessage"

        createRunner(
            projectDir,
            "createInfrastructureDryRun",
            "--stacktrace",
        ).build()

        val testGroovyAccessor = fixtureDir / "resources/rawMessage/infra/build/cikraft/typedFlows/kotlin/test.kt"
        assertEquals(
            """
                import app.softwork.cikraft.integrationflow.StepBuilder
                import app.softwork.cikraft.integrationflow.builder.CreatedFlowConfig

                public interface test : CreatedFlowConfig

                context(config: test)
                public fun StepBuilder.test() {
                  groovyScript(name = "test", file = "test.groovy")
                }

            """.trimIndent(),
            testGroovyAccessor.readText()
        )

        assertEquals(
            listOf(
                Script(
                    name = "raw",
                    jvmFunction = "com.example.FooKt.raw",
                    isSuspend = false,
                    inputs = listOf(
                        None(
                            "message",
                            klass = CodeGenTree.NormalClass(
                                "com.sap.gateway.ip.core.customdev.util",
                                listOf("Message"),
                            ),
                            nullable = false,
                            documentation = null,
                            hasDefault = false,
                        ),
                    ),
                    outputJvmName = "kotlin.Unit",
                    outputs = emptySet(),
                    error = null,
                    outputIsNullable = false,
                ),
            ),
            Json.decodeFromString((projectDir / "app/build/generated/ksp/main/resources/cikraft/entrypoint.json").readText()),
        )
    }

    private fun createRunner(projectDir: Path, vararg tasks: String): GradleRunner {
        val isDebug = System.getenv("DEBUGGER_ENABLED") == "true"
        return GradleRunner.create()
            .withProjectDir(projectDir.toFile())
            .withDebug(isDebug)
            .forwardOutput()
            .withArguments(
                buildList {
                    add("clean")
                    addAll(tasks)
                    add("-Porg.gradle.kotlin.dsl.dcl=true")

                    if (!isDebug) {
                        add("--configuration-cache")
                    }

                    val isOffline = System.getenv("offlineMode") == "true"
                    if (isOffline) {
                        add("--offline")
                    }
                    add("-Dorg.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=2g")
                    add("--stacktrace")
                    add("--info")
                    add("-PKDGPUsername=${System.getenv("KDGP_USERNAME")}")
                    add("-PKDGPPassword=${System.getenv("KDGP_PASSWORD")}")
                },
            )
    }
}
