package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.Value
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
    }

    override fun execute() {
        val output = parameters.output.get().asFile

        val environmentService = buildList {
            for (propertyFile in parameters.propertyFiles) {
                val iFlowName = propertyFile.nameWithoutExtension
                val values = propertyFile.readText().let {
                    Json.decodeFromString(MapSerializer(String.serializer(), Value.serializer()), it)
                }
                for ((propertyName, value) in values) {
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
            }
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

        output.writeText(
// language=yaml
            """name: ${parameters.projectName.get()}

services:
  ${parameters.serviceName.get()}:
    image: ${parameters.image.get()}
    $environmentService
    $volumesService
$volumesOption""",
        )
    }
}
