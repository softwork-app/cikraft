package app.softwork.cikraft.gradle

import app.softwork.cikraft.core.CreatedFlow
import app.softwork.cikraft.generator.generateKtorClientSetup
import kotlinx.serialization.json.Json
import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import org.gradle.work.*
import org.gradle.workers.*
import javax.inject.*

@CacheableTask
abstract class GenerateKtorClientSetup : DefaultTask() {
    init {
        group = "cikraft"
    }

    @get:InputDirectory
    @get:SkipWhenEmpty
    @get:NormalizeLineEndings
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val createdFlows: DirectoryProperty

    @get:OutputDirectory
    abstract val ktorApi: DirectoryProperty

    @get:Inject
    internal abstract val workerExecutor: WorkerExecutor

    @get:Classpath
    internal abstract val workerClasspath: ConfigurableFileCollection

    @TaskAction
    internal fun generate() {
        val worker = workerExecutor.classLoaderIsolation {
            classpath.from(workerClasspath)
        }

        for (createdFlow in createdFlows.get().asFile.listFiles()) {
            worker.submit(GenerateKtorClientSetupAction::class) {
                this.createdFlow.set(createdFlow)
                this.outputDirectory.set(ktorApi)
            }
        }
    }

    internal abstract class GenerateKtorClientSetupAction : WorkAction<GenerateKtorClientSetupAction.Param> {
        interface Param : WorkParameters {
            val createdFlow: RegularFileProperty
            val outputDirectory: DirectoryProperty
        }

        override fun execute() {
            val createdFlow: CreatedFlow = Json.decodeFromString(parameters.createdFlow.asFile.get().readText())
            generateKtorClientSetup(createdFlow)?.writeTo(parameters.outputDirectory.asFile.get())
        }
    }
}
