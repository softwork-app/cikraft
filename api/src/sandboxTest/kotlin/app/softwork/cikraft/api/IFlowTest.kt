package app.softwork.cikraft.api

import SchedulerBase
import alwaysFails
import apiClient
import app.softwork.cikraft.DataStoreMessage
import app.softwork.cikraft.DataStoreMessages
import app.softwork.cikraft.integrationflow.CreateArtifact
import app.softwork.cikraft.integrationflow.integrationFlow
import consumerClient
import delete
import get
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import person
import select
import write
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.fail
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
class IFlowTest {
    @Test
    fun httpPost(): Unit = runBlocking {
        val uuid = Uuid.random()
        val artifactContent = person(uuid)
        apiClient.deploy(
            id = artifactContent.name,
            artifactContent = artifactContent.toBase64(),
        ) {
            val s = consumerClient.post("http/Foo/foo/$uuid") {
                header("x-correlation-id", "ASDFFF")
                contentType(ContentType.Application.Json)
                setBody("""{"foo":"bar"}""")
            }
            assertEquals(200, s.status.value)
            assertEquals(
                "Marie ACCEPT application/json,application/xml Content-Type application/json null POST ",
                s.bodyAsText(),
            )
        }
    }

    @Test
    fun httpGet(): Unit = runBlocking {
        val uuid = Uuid.random()
        val artifactContent = person(uuid, "/*")
        apiClient.deploy(
            id = artifactContent.name,
            artifactContent = artifactContent.toBase64(),
        ) {
            val s = consumerClient.get("http/Foo/foo/$uuid/some?foo=bar&b=c") {
                header("x-correlation-id", "ASDFFF")
                contentType(ContentType.Application.Json)
                setBody("""{"foo":"bar"}""")
            }
            assertEquals(200, s.status.value)
            assertEquals(
                "Marie ACCEPT application/json,application/xml Content-Type application/json foo=bar&b=c GET some",
                s.bodyAsText(),
            )
        }
    }

    @Test
    fun alwaysFails(): Unit = runBlocking {
        val uuid = Uuid.random()
        val artifactContent = alwaysFails(uuid)

        apiClient.deploy(
            id = artifactContent.name,
            artifactContent = artifactContent.toBase64(),
        ) {
            val fooException = consumerClient.get("http/Foo/foo/$uuid")
            assertEquals(444, fooException.status.value)
            assertEquals(
                "Foo",
                fooException.bodyAsText(),
            )

            val s = consumerClient.get("http/Foo/foo/$uuid") {
                contentType(ContentType.Application.Json)
                setBody("""{"foo":"bar"}""")
            }
            assertEquals(500, s.status.value)
            assertTrue(
                s.bodyAsText().startsWith("An internal server error occured: The MPL ID for the failed message is"),
            )
        }
    }

    @Test
    fun datastore(): Unit = runBlocking {
        val uuid = Uuid.random()
        val artifactContent = write(uuid)
        apiClient.deploy(
            id = artifactContent.name,
            artifactContent = artifactContent.toBase64(),
        ) {
            val artifactContent = select(uuid)
            apiClient.deploy(
                id = artifactContent.name,
                artifactContent = artifactContent.toBase64(),
            ) {
                val artifactContent = get(uuid)
                apiClient.deploy(
                    id = artifactContent.name,
                    artifactContent = artifactContent.toBase64(),
                ) {
                    val artifactContent = delete(uuid)
                    apiClient.deploy(
                        id = artifactContent.name,
                        artifactContent = artifactContent.toBase64(),
                    ) {
                        val notFound = consumerClient.get("http/Foo/get/$uuid")
                        assertEquals(404, notFound.status.value)
                        assertEquals("", notFound.bodyAsText())

                        val deletedFailed = consumerClient.delete("http/Foo/delete/$uuid")
                        assertEquals(204, deletedFailed.status.value)

                        val write = consumerClient.post("http/Foo/write/$uuid") {
                            contentType(ContentType.Application.Xml)
                            setBody(Pair("foo", "bar"))
                        }
                        assertEquals(201, write.status.value)
                        val location = write.headers["Location"]
                        assertNotNull(location)
                        assertEquals(
                            Url(
                                "https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com/http/Foo/get/$uuid",
                            ),
                            URLBuilder(
                                "https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com/http/Foo/write/$uuid",
                            )
                                .takeFrom(location)
                                .build(),
                        )
                        assertEquals("", write.bodyAsText())
                        delay(3.seconds)

                        val get = consumerClient.get("http/Foo/get/$uuid")
                        assertEquals(200, get.status.value)
                        assertEquals(
                            Pair("foo", "bar"),
                            get.body(),
                        )

                        val select = consumerClient.post("http/Foo/select/$uuid")
                        assertEquals(200, select.status.value)
                        assertEquals(
                            DataStoreMessages(
                                listOf(
                                    DataStoreMessage(
                                        id = "FOO_$uuid",
                                        content = Pair("foo", "bar"),
                                    ),
                                ),
                            ),
                            select.body(),
                        )

                        val autoDeleted = consumerClient.get("http/Foo/get/$uuid")
                        assertEquals(404, autoDeleted.status.value)

                        val writeAgain = consumerClient.post("http/Foo/write/$uuid") {
                            contentType(ContentType.Application.Xml)
                            setBody(Pair("foo", "baz"))
                        }
                        assertEquals(201, writeAgain.status.value)
                        assertEquals(
                            "https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com/http/Foo/get/$uuid",
                            writeAgain.headers["Location"],
                        )
                        assertEquals("", writeAgain.bodyAsText())
                        delay(3.seconds)
                        val getAgain = consumerClient.get("http/Foo/get/$uuid")
                        assertEquals(200, getAgain.status.value)
                        assertEquals(
                            Pair("foo", "baz"),
                            getAgain.body(),
                        )

                        val deleted = consumerClient.delete("http/Foo/delete/$uuid")
                        assertEquals(204, deleted.status.value)
                        val deletedNotFound = consumerClient.get("http/Foo/get/$uuid")
                        assertEquals(404, deletedNotFound.status.value)
                    }
                }
            }
        }
    }

    @Test
    fun timer(): Unit = runBlocking {
        val id = "IF0100TestPWScheduler${Uuid.random()}"
        apiClient.deploy(
            id = id,
            artifactContent = CreateArtifact(
                name = id,
                version = "1.0.0",
                description = "Test Scheduler IFlow",
                integrationFlow = SchedulerBase.integrationFlow {
                    timer(
                        "0 0/5 * ? * *",
                    ) {
                        contentModifier {
                        }
                        endMessage()
                    }
                },
            ).toBase64(),
        )
    }
}

suspend fun HttpClient.deploy(
    id: String,
    artifactContent: String,
    test: suspend () -> Unit = {},
) {
    val newIP = IntegrationPackage.New(
        "IP0100TestPW",
        "IP_0100_Test_PW2",
        shortText = "API-Test",
    )
    try {
        if (getIntegrationPackage(newIP.id) == null) {
            createIntegrationPackage(newIP)
        }

        if (getIntegrationFlow(id) != null) {
            deleteIntegrationFlow(id)
        }
        createIntegrationFlow(
            IntegrationFlow.New(
                name = id,
                id = id,
                packageId = newIP.id,
                artifactContentAsBase64 = artifactContent,
            ),
        )

        val taskId = deployIntegrationFlow(id)
        assertNotNull(taskId)

        while (true) {
            delay(3.seconds)
            val status = getBuildAndDeployStatus(taskId)
            when (status.status) {
                BuildAndDeployStatus.Status.Deploying -> continue
                BuildAndDeployStatus.Status.Fail -> fail("Deployment of $id failed")
                BuildAndDeployStatus.Status.Success -> break
            }
        }
        test()
    } finally {
        deleteIntegrationFlow(id)
        undeployIntegrationFlow(id)
    }
}
