package app.softwork.cikraft.integrationflow

import app.softwork.cikraft.integrationflow.CreateArtifact.Companion.definitionsXML
import java.io.*
import kotlin.test.*
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class CreateIFlowTest {
    @Test
    fun flowFile() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream(
                "/FOO/src/main/resources/scenarioflows/integrationflow/FOO.iflw",
            )!!
                .bufferedReader()

        assertEquals(definitionsXML.decodeFromString(Definitions.serializer(), expected.readText()), flow)
    }

    @Test
    fun manifest() {
        val expected = CreateIFlowTest::class.java.getResourceAsStream("/FOO/META-INF/MANIFEST.MF")!!
            .bufferedReader()
        val actual = ByteArrayOutputStream()
        createManifestMF("FOO", "1.0.0", actual)
        assertEquals(expected.readText(), actual.toString("UTF-8"))
    }

    @Test
    fun project() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream("/FOO/.project")!!.bufferedReader()
        assertEquals(expected.readText(), createProject("FOO"))
    }

    @Test
    fun metainfo() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream("/FOO/metainfo.prop")!!.bufferedReader()
        assertEquals(expected.readText(), createMetainfo("some Description"))
    }

    @Test
    fun metainfoWithSource() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream("/everySunday12/metainfo.prop")!!.bufferedReader()
        assertEquals(expected.readText(), createMetainfo("some Description", listOf("Foo", "Bar"), listOf("Baz")))
    }

    @Test
    fun parametersProp() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream("/FOO/src/main/resources/parameters.prop")!!
                .bufferedReader()
        assertEquals(expected.readText(), createParametersProp(parameters))
    }

    private val parameters = mapOf(
        "BUCKET_NAME" to "bkt-foo",
    )

    @Test
    fun parametersPropDef() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream(
                "/FOO/src/main/resources/parameters.propdef",
            )!!.bufferedReader()
        assertEquals(expected.readText(), createParametersPropDef(parameters.keys))
    }

    @Test
    fun zipFile() {
        val expected = "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"
        val actual = CreateArtifact(
            libs = emptyList(),
            scripts = emptyList(),
            name = "FOO",
            version = "1.0.0",
            description = null,
            parameters = parameters,
            integrationFlow = flow,
        )
        assertEquals(expected, actual.toBase64())
        assertEquals(CreateArtifact.fromBase64(expected), actual)
    }

    @Test
    fun parseZipFileWithSourceAndTarget() {
        val actual = CreateArtifact(
            libs = emptyList(),
            scripts = emptyList(),
            name = "FOO",
            version = "1.0.0",
            description = null,
            parameters = parameters,
            integrationFlow = flow,
            source = listOf("Foo", "Bar"),
            target = listOf("Fooooo", "Baaar"),
        )
        assertEquals(actual, CreateArtifact.fromBase64(actual.toBase64()))
    }

    data object SchedulerBase : Config {
        override val baseUrl: String = "/Foo"
        override val allowedHeaders: Set<String> = emptySet()
    }

    @Test
    fun schedulerEvery6Hours() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream(
                "/A/src/main/resources/scenarioflows/integrationflow/A.iflw",
            )!!.bufferedReader()

        assertEquals(
            definitionsXML.decodeFromString(Definitions.serializer(), expected.readText()),
            SchedulerBase.integrationFlow {
                val every6Hours = "0 0 0/6 ? * * *"
                timer(quartzCron = every6Hours) {
                    contentModifier {
                        externalParameter("A")
                    }
                    endMessage()
                }
            },
        )
    }

    @Test
    fun yearInCronIsOptional() {
        SchedulerBase.integrationFlow {
            val every6Hours = "0 0 0/6 ? * *"
            timer(quartzCron = every6Hours) {
                endMessage()
            }
        }
    }

    @Test
    fun variables() {
        val expected =
            CreateIFlowTest::class.java.getResourceAsStream("/Variables.iflw")!!.bufferedReader()

        assertEquals(
            definitionsXML.decodeFromString(Definitions.serializer(), expected.readText()),
            SchedulerBase.integrationFlow {
                dataStore(
                    dataStoreName = "DS_FOO",
                    visibility = DataStoreVisibility.Global,
                    maximumRetryInterval = 60.minutes,
                    exponentialBackoff = true,
                    pollInterval = 300.seconds,
                    retryInterval = 1.minutes,
                    lockTimeout = 10.minutes,
                ) {
                    startMessage()
                    write(
                        name = "Write 1",
                        dataStoreName = "DS_FOO",
                        visibility = DataStoreVisibility.Global,
                        entryID = null,
                        retentionThreshold = 2.days,
                        expirationPeriod = 30.days,
                        encryptStoredMessage = true,
                        overwrite = true,
                        includeMessageHeaders = false,
                    )
                    get(
                        "Get 1",
                        dataStoreName = "DS_FOO",
                        visibility = DataStoreVisibility.IntegrationFlow,
                        entryID = "sdf",
                        deleteOnCompletion = false,
                        throwExceptionOnMissingEntry = true,
                    )
                    select(
                        "Select 1",
                        dataStoreName = "DS_FOO",
                        visibility = DataStoreVisibility.IntegrationFlow,
                        numberOfPolledMessages = 1,
                        deleteOnCompletion = false,
                    )
                    delete(
                        name = "Delete 1",
                        dataStoreName = "DS_FOO",
                        visibility = DataStoreVisibility.IntegrationFlow,
                        entryID = null,
                    )
                    endMessage()
                }
            },
        )
    }
}
