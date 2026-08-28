package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Value
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.submit
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.workers.WorkerExecutor
import javax.inject.Inject
import kotlin.time.Duration

@CacheableTask
abstract class GenerateDockerComposeFileTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val propertyFiles: ConfigurableFileCollection

    @get:Input
    abstract val projectName: Property<String>

    @get:Input
    abstract val image: Property<String>

    @get:Input
    abstract val serviceName: Property<String>

    @get:Input
    abstract val volumes: ListProperty<String>

    @get:Input
    abstract val healthCheckTest: ListProperty<String>

    @get:Input
    abstract val healthCheckInterval: Property<String>

    @get:Input
    abstract val healthCheckTimeout: Property<String>

    @get:Input
    abstract val healthCheckRetries: Property<Int>

    @get:OutputFile
    abstract val output: RegularFileProperty

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @TaskAction
    protected fun generate() {
        workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }.submit(GenerateDockerComposeFileWorker::class) {
            output.set(this@GenerateDockerComposeFileTask.output)
            projectName.set(this@GenerateDockerComposeFileTask.projectName)
            serviceName.set(this@GenerateDockerComposeFileTask.serviceName)
            image.set(this@GenerateDockerComposeFileTask.image)
            volumes.set(this@GenerateDockerComposeFileTask.volumes)
            healthCheckTest.set(this@GenerateDockerComposeFileTask.healthCheckTest)
            healthCheckInterval.set(this@GenerateDockerComposeFileTask.healthCheckInterval)
            healthCheckTimeout.set(this@GenerateDockerComposeFileTask.healthCheckTimeout)
            healthCheckRetries.set(this@GenerateDockerComposeFileTask.healthCheckRetries)
            propertyFiles.from(this@GenerateDockerComposeFileTask.propertyFiles.asFileTree)
        }
    }
}

internal abstract class GenerateDockerComposeFileWorker : WorkAction<GenerateDockerComposeFileWorker.Parameters> {
    interface Parameters : WorkParameters {
        val output: RegularFileProperty
        val projectName: Property<String>
        val serviceName: Property<String>
        val image: Property<String>
        val volumes: ListProperty<String>
        val propertyFiles: ConfigurableFileCollection
        val healthCheckTest: ListProperty<String>
        val healthCheckInterval: Property<String>
        val healthCheckTimeout: Property<String>
        val healthCheckRetries: Property<Int>
    }

    override fun execute() {
        val output = parameters.output.get().asFile

        val environmentService = buildList {

        }.takeUnless { it.isEmpty() }?.joinToString(
            prefix = "environment:\n",
            separator = "\n",
            postfix = "\n",
        ) {
            "      $it"
        } ?: ""

        val volumes = parameters.volumes.get()
        val volumesService: String = if (volumes.isEmpty()) {
            ""
        } else {
            volumes.joinToString(
                prefix = "volumes:\n",
                separator = "\n",
                postfix = "\n",
            ) {
                "      - $it:/$it"
            }
        }
        val volumesOption = if (volumes.isEmpty()) {
            ""
        } else {
            volumes.joinToString(
                prefix = "volumes:\n",
                separator = "\n",
                postfix = "\n",
            ) {
                "  $it:"
            }
        }

        val healthCheck = parameters.healthCheckTest.orNull?.let {
            "healthcheck:\n$it"
        } ?: ""

        output.writeText(
            Json.encodeToString(
                DockerCompose.serializer(),
                DockerCompose(
                    name = parameters.projectName.get(),
                    services = mapOf(
                        parameters.serviceName.get() to DockerCompose.Service(
                            image = parameters.image.get(),
                            healthcheck =,
                            environment = parameters.propertyFiles.map { propertyFile ->
                                val iFlowName = propertyFile.nameWithoutExtension
                                val values = propertyFile.readText().let {
                                    Json.decodeFromString(MapSerializer(String.serializer(), Value.serializer()), it)
                                }
                            }.map { (propertyName, value) ->
                                for ( in values) {
                                    val name = "${iFlowName}_$propertyName".uppercase()
                                    val stringValue = when (value) {
                                        is Value.BOOLEAN -> value.value.toString()
                                        is Value.DOUBLE -> value.value.toString()
                                        is Value.FLOAT -> value.value.toString()
                                        is Value.INT -> value.value.toString()
                                        is Value.STRING -> value.value
                                    }
                                    add("$name: $stringValue")
                                }
                            },
                            volumes = parameters.volumes.get().map {
                                "$it:/$it"
                            },
                            )
                    ),
                    volumes = volumes.associateWith { },
                )
            )
        )
    }

    @Serializable
    data class DockerCompose(
        val name: String,
        val services: Map<String, Service>,
        val volumes: Map<String, Unit> = emptyMap(),
    ) {
        @Serializable
        data class Service(
            val image: String,
            val healthcheck: HealthCheck? = null,
            val environment: Map<String, String> = emptyMap(),
            val volumes: List<String> = emptyList(),
        ) {
            @Serializable
            data class HealthCheck(
                val test: List<String>,
                val interval: Duration,
                val timeout: Duration,
                val retries: Int,
            )
        }
    }
}
