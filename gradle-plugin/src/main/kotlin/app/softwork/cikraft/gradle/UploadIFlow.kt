package app.softwork.cikraft.gradle

import app.softwork.cikraft.api.IntegrationFlow
import app.softwork.cikraft.api.createIntegrationFlow
import app.softwork.cikraft.api.deleteIntegrationFlow
import app.softwork.cikraft.api.getIntegrationFlow
import app.softwork.cikraft.api.setupSapCIApiClient
import app.softwork.cikraft.core.Value
import app.softwork.cikraft.integrationflow.CreateArtifact
import app.softwork.cikraft.integrationflow.CreateArtifact.Companion.definitionsXML
import app.softwork.cikraft.integrationflow.Definitions
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.logging.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import org.gradle.api.logging.Logging as GradleLogger

@UntrackedTask(because = "Uploading a IFlow is a remote operation")
internal abstract class UploadIFlow @Inject constructor(
    @get:Input
    private val stageName: String,
) : DefaultTask() {
    init {
        group = "cikraft"
        val isOffline = project.gradle.startParameter.isOffline
        onlyIf { !isOffline }
    }

    @get:ServiceReference
    @get:Optional
    abstract val lockService: Property<TransportLockBuildService>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val libs: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val scripts: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val flowXmlDefinition: RegularFileProperty

    @get:Input
    internal abstract val apiServer: Property<String>

    @get:Input
    internal abstract val authServer: Property<String>

    @get:Input
    internal val credentials: Provider<PasswordCredentials> = project.providers.credentials(
        PasswordCredentials::class.java,
        SAP_CI_CREDENTIALS + stageName,
    )

    @get:Input
    internal abstract val packageID: Property<String>

    @get:Input
    internal abstract val flowName: Property<String>

    @get:Input
    internal abstract val flowID: Property<String>

    @get:Input
    internal abstract val flowDescription: Property<String>

    @get:Input
    internal abstract val flowSource: ListProperty<String>

    @get:Input
    internal abstract val flowTarget: ListProperty<String>

    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    internal abstract val parametersFile: RegularFileProperty

    @get:Input
    internal abstract val version: Property<String>

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun create() {
        lockService.orNull?.initialize(workerExecutor)

        workerExecutor.classLoaderIsolation {
            it.classpath.from(workerClasspath)
        }.submit(UploadIFlowWorker::class.java) {
            it.apiServer.set(apiServer)
            it.authServer.set(authServer)
            it.clientID.set(credentials.map { it.username })
            it.clientSecret.set(credentials.map { it.password })

            it.packageID.set(packageID)
            it.flowName.set(flowName)
            it.flowID.set(flowID)
            it.flowDescription.set(flowDescription)
            it.flowSource.set(flowSource)
            it.flowTarget.set(flowTarget)

            it.definitions.set(flowXmlDefinition)
            it.parametersFile.set(parametersFile)
            it.libs.from(libs)
            it.scripts.from(scripts)
            it.version.set(version)
        }
    }
}

internal abstract class UploadIFlowWorker : WorkAction<UploadIFlowWorker.Params> {
    interface Params : WorkParameters {
        val apiServer: Property<String>
        val authServer: Property<String>
        val clientID: Property<String>
        val clientSecret: Property<String>

        val packageID: Property<String>
        val flowName: Property<String>
        val flowID: Property<String>
        val flowDescription: Property<String>
        val flowSource: ListProperty<String>
        val flowTarget: ListProperty<String>
        val definitions: RegularFileProperty
        val parametersFile: RegularFileProperty

        val libs: ConfigurableFileCollection
        val scripts: ConfigurableFileCollection

        val version: Property<String>
    }

    override fun execute() {
        val version = parameters.version.get()
        val flowID = parameters.flowID.get()

        HttpClient(CIO) {
            setupSapCIApiClient(
                apiServer = parameters.apiServer.get(),
                authServer = parameters.authServer.get(),
                clientId = parameters.clientID.get(),
                clientSecret = parameters.clientSecret.get(),
            )
            Logging {
                setupGradleLogging(GradleLogger.getLogger(UploadIFlowWorker::class.java))
            }
        }.use { client ->
            runBlocking {
                val artifactContentAsBase64 = CreateArtifact(
                    libs = parameters.libs.filter {
                        it.exists()
                    },
                    scripts = parameters.scripts.filter {
                        it.exists()
                    },
                    name = parameters.flowName.get(),
                    description = parameters.flowDescription.get(),
                    source = parameters.flowSource.get(),
                    target = parameters.flowTarget.get(),
                    integrationFlow = definitionsXML.decodeFromString(
                        Definitions.serializer(),
                        this@UploadIFlowWorker.parameters.definitions.get().asFile.readText(),
                    ),
                    parameters = Json.decodeFromString(
                        MapSerializer(String.serializer(), Value.serializer()),
                        parameters.parametersFile.get().asFile.readText(),
                    ).mapValues {
                        when (val value = it.value) {
                            is Value.BOOLEAN -> value.value.toString()
                            is Value.DOUBLE -> value.value.toString()
                            is Value.FLOAT -> value.value.toString()
                            is Value.INT -> value.value.toString()
                            is Value.STRING -> value.value
                        }
                    },
                    version = version,
                ).toBase64()

                val iFlowExists = client.getIntegrationFlow(flowID, version)
                if (iFlowExists != null) {
                    client.deleteIntegrationFlow(flowID, version)
                }
                client.createIntegrationFlow(
                    IntegrationFlow.New(
                        id = flowID,
                        name = parameters.flowName.get(),
                        packageId = parameters.packageID.get(),
                        artifactContentAsBase64 = artifactContentAsBase64,
                    ),
                )
            }
        }
    }
}
