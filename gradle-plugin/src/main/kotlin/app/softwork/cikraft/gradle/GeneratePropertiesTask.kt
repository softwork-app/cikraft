package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.*
import app.softwork.cikraft.generator.*
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GeneratePropertiesTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val createdFlows: DirectoryProperty

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val propertiesFiles: DirectoryProperty

    @get:OutputDirectory
    abstract val propertiesFolder: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        val worker = workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }
        val propertiesFiles = propertiesFiles.get().asFile.listFiles()
        for (createdFlow in createdFlows.get().asFile.listFiles()) {
            worker.submit(GenerateProperties::class) {
                this.createdFlow.set(createdFlow)
                this.propertyFile.set(
                    propertiesFiles.single { it.nameWithoutExtension == createdFlow.nameWithoutExtension },
                )
                this.outputDirectory.set(propertiesFolder)
            }
        }
    }

    internal abstract class GenerateProperties : WorkAction<GenerateProperties.Param> {
        interface Param : WorkParameters {
            val createdFlow: RegularFileProperty
            val propertyFile: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            val createdFlowFile = parameters.createdFlow.asFile.get()
            val createdFlow = Json.decodeFromString(
                CreatedFlow.serializer(),
                createdFlowFile.readText(),
            )
            val propertiesFile = parameters.propertyFile.asFile.get()
            val properties = Json.decodeFromString(
                MapSerializer(String.serializer(), Value.serializer()),
                propertiesFile.readText(),
            )

            generateProperties(
                createdFlow,
                properties,
            ).writeTo(parameters.outputDirectory.asFile.get())
        }
    }
}
