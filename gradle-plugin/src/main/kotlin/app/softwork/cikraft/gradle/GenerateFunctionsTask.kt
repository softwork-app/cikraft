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
public abstract class GenerateFunctionsTask : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.NONE)
    public abstract val createdFlows: ConfigurableFileCollection

    @get:Input
    public abstract val functions: SetProperty<String>

    @get:OutputDirectory
    public abstract val functionsFolder: DirectoryProperty

    @get:Inject
    protected abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    protected fun generate() {
        val worker = workerExecutor.classLoaderIsolation {
            it.classpath.from(workerClasspath)
        }
        val functions = functions.get()
        for (createdFlow in createdFlows.asFileTree) {
            if (functions.isEmpty() || createdFlow.nameWithoutExtension in functions) {
                worker.submit(GenerateFunctions::class.java) {
                    it.createdFlow.set(createdFlow)
                    it.outputDirectory.set(functionsFolder)
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
