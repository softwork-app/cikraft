package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.*
import app.softwork.cikraft.generator.*
import kotlinx.serialization.json.*
import org.gradle.api.DefaultTask
import org.gradle.api.file.*
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateFunctionsTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val createdFlows: DirectoryProperty

    @get:Input
    abstract val functions: SetProperty<String>

    @get:OutputDirectory
    abstract val functionsFolder: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        val worker = workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }
        val functions = functions.get()
        for (createdFlow in createdFlows.get().asFile.listFiles()) {
            if (functions.isEmpty() || createdFlow.nameWithoutExtension in functions) {
                worker.submit(GenerateFunctions::class) {
                    this.createdFlow.set(createdFlow)
                    this.outputDirectory.set(functionsFolder)
                }
            }
        }
    }

    internal abstract class GenerateFunctions : WorkAction<GenerateFunctions.Param> {
        interface Param : WorkParameters {
            val createdFlow: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            val createdFlowFile = parameters.createdFlow.asFile.get()
            val createdFlow = Json.decodeFromString(
                CreatedFlow.serializer(),
                createdFlowFile.readText(),
            )
            generateFunction(createdFlow).writeTo(parameters.outputDirectory.asFile.get())
        }
    }
}
