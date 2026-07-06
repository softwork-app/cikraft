import app.softwork.cikraft.api.setupRuntimeAuth
import app.softwork.cikraft.api.setupSapCIApiClient
import app.softwork.cikraft.integrationflow.Config
import app.softwork.cikraft.integrationflow.CreateArtifact
import app.softwork.cikraft.integrationflow.integrationFlow
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import nl.adaptivity.xmlutil.serialization.XML
import java.io.File
import kotlin.time.Duration.Companion.days
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ExperimentalUuidApi
fun person(
    uuid: Uuid,
    suffix: String? = null,
): CreateArtifact {
    val groovyFile = File("src/sandboxTest/groovy/Person.groovy")
    return CreateArtifact(
        libs = listOf(),
        scripts = listOf(groovyFile),
        name = "IF0100TestPW$uuid",
        version = "1.0.0",
        description = "Some Test IFlow",
        parameters = mapOf("A" to "fff"),
        integrationFlow = SchedulerBase.integrationFlow {
            sender = "Sender"
            https(
                url = "/foo/$uuid${suffix ?: ""}",
                userRole = "ESBMessaging.send",
                clientCertificates = true,
                returnExceptionsToSender = false,
            ) {
                startMessage("Start")
                groovyScript(function = "client", file = groovyFile.name)
                contentModifier {
                    property("person", "_RESULT_")
                }
                groovyScript(function = "logic", file = groovyFile.name)
                endMessage("End")

                exceptionSubprocess {
                    startErrorEvent("Error Start")
                    contentModifier {

                    }
                    errorEndEvent("Error End")
                }
            }
            receiver = "Receiver"
        },
    )
}

@ExperimentalUuidApi
fun alwaysFails(
    uuid: Uuid,
): CreateArtifact {
    val groovyFile = File("src/sandboxTest/groovy/alwaysFails.groovy")
    return CreateArtifact(
        libs = listOf(),
        scripts = listOf(groovyFile),
        name = "IF0100TestPW$uuid",
        version = "1.0.0",
        description = "Some Failing Test IFlow",
        parameters = mapOf("A" to "fff"),
        integrationFlow = SchedulerBase.integrationFlow {
            sender = "FooSender"
            https(
                url = "/foo/$uuid",
                userRole = "ESBMessaging.send",
                clientCertificates = true,
                returnExceptionsToSender = false,
            ) {
                startMessage("CustomStart")
                groovyScript(file = groovyFile.name)
                endMessage("CustomEnd")

                exceptionSubprocess {
                    startErrorEvent("Error Start")
                    groovyScript("errorHandling", function = "errorHandling", file = groovyFile.name)
                    endMessage("Error End")
                }
            }
        },
    )
}

@ExperimentalUuidApi
fun write(
    uuid: Uuid,
): CreateArtifact {
    val groovyFile = File("src/sandboxTest/groovy/deleteBody.groovy")
    return CreateArtifact(
        scripts = listOf(groovyFile),
        name = "IF0100TestPWWrite$uuid",
        version = "1.0.0",
        description = "Some Test IFlow",
        integrationFlow = SchedulerBase.integrationFlow {
            https(
                url = "/write/$uuid",
                userRole = "ESBMessaging.send",
            ) {
                startMessage()
                write(
                    name = "Write 1",
                    dataStoreName = "DS_FOO$uuid".take(40),
                    entryID = "FOO_$uuid",
                    retentionThreshold = 1.days,
                    expirationPeriod = 2.days,
                )
                groovyScript(file = groovyFile.name)
                endMessage()
            }
        },
    )
}

@ExperimentalUuidApi
fun select(
    uuid: Uuid,
): CreateArtifact {
    return CreateArtifact(
        name = "IF0100TestPWSelect$uuid",
        version = "1.0.0",
        description = "Some Test IFlow",
        integrationFlow = SchedulerBase.integrationFlow {
            https(
                url = "/select/$uuid",
                userRole = "ESBMessaging.send",
            ) {
                startMessage()
                contentModifier {
                    constant("dsName", "DS_FOO$uuid".take(40))
                }
                select(
                    name = "Select 1",
                    dataStoreName = $$"${property.dsName}",
                    deleteOnCompletion = true,
                    numberOfPolledMessages = Int.MAX_VALUE,
                )
                contentModifier {
                    addHeader("Content-Type", "application/xml")
                }
                endMessage()
            }
        },
    )
}

@ExperimentalUuidApi
fun get(
    uuid: Uuid,
): CreateArtifact {
    val groovyFile = File("src/sandboxTest/groovy/getDataStoreSetOutgoingHeaders.groovy")
    return CreateArtifact(
        scripts = listOf(groovyFile),
        name = "IF0100TestPWGet$uuid",
        version = "1.0.0",
        description = "Some Test IFlow",
        integrationFlow = SchedulerBase.integrationFlow {
            https(
                url = "/get/$uuid",
                userRole = "ESBMessaging.send",
            ) {
                startMessage()
                contentModifier {
                    constant("dsName", "DS_FOO$uuid".take(40))
                }
                get(
                    name = "Get 1",
                    dataStoreName = $$"${property.dsName}",
                    throwExceptionOnMissingEntry = false,
                    entryID = "FOO_$uuid",
                )
                groovyScript(file = groovyFile.name)
                endMessage()
            }
        },
    )
}

@ExperimentalUuidApi
fun delete(
    uuid: Uuid,
): CreateArtifact = CreateArtifact(
    name = "IF0100TestPWDelete$uuid",
    version = "1.0.0",
    description = "Some Test IFlow",
    integrationFlow = SchedulerBase.integrationFlow {
        https(
            url = "/delete/$uuid",
            userRole = "ESBMessaging.send",
        ) {
            startMessage()
            contentModifier {
                constant("dsName", "DS_FOO$uuid".take(40))
            }
            delete(
                name = "Delete 1",
                dataStoreName = $$"${property.dsName}",
                entryID = "FOO_$uuid",
            )
            contentModifier {
                addHeader("CamelHttpResponseCode", "204")
            }
            endMessage()
        }
    },
)

data object SchedulerBase : Config {
    override val baseUrl: String = "/Foo"
    override val allowedHeaders = setOf("X-Correlation-ID", "Accept", "Content-Type")
}

val apiClient = HttpClient(CIO) {
    install(Logging) {
        level = LogLevel.ALL
        logger = Logger.SIMPLE
    }
    setupSapCIApiClient(
        clientId = "sb-08b6afaa-f349-4ad3-ba76-28dbcfdd62e3!b131200|it!b196",
        clientSecret = System.getenv("SBX_API_CLIENT_SECRET"),
        apiServer = "https://8c5e4266trial.it-cpitrial03.cfapps.ap21.hana.ondemand.com",
        authServer = "https://8c5e4266trial.authentication.ap21.hana.ondemand.com",
    )
}

val consumerClient = HttpClient(CIO) {
    setupRuntimeAuth(
        "https://8c5e4266trial.authentication.ap21.hana.ondemand.com/oauth/token",
        clientId = "sb-cd8c42c8-1525-4225-8ce2-5fcd70fac8fd!b131200|it-rt-8c5e4266trial!b196",
        clientSecret = System.getenv("SBX_RT_CLIENT_SECRET"),
    )
    install(Logging) {
        level = LogLevel.ALL
        logger = Logger.SIMPLE
    }
    install(ContentNegotiation) {
        jsonIo(Json)
        serialization(ContentType.Application.Xml, XML)
    }
    defaultRequest {
        url("https://8c5e4266trial.it-cpitrial03-rt.cfapps.ap21.hana.ondemand.com")
    }
}
